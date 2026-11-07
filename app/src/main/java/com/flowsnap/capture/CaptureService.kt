package com.flowsnap.capture

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.res.ResourcesCompat
import com.flowsnap.R
import com.flowsnap.data.ScreenshotStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/** Owns exactly one user-approved MediaProjection session and its draggable capture control. */
class CaptureService : Service() {
    private lateinit var projection: MediaProjection
    private lateinit var reader: ImageReader
    private var virtualDisplay: VirtualDisplay? = null
    private lateinit var windowManager: WindowManager
    private var bubble: View? = null
    private var flowId = ""
    private var sessionStarted = false
    private var captureRequested = false
    private val stopping = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val captureScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var saveJob: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopCapture(); return START_NOT_STICKY }
        if (intent?.action == ACTION_RESUME) {
            captureRequested = false
            if (sessionStarted && Settings.canDrawOverlays(this)) {
                // The previous bubble is removed as the review screen opens.
                // Always attach a fresh, single overlay here instead of trying to
                // reuse a view that WindowManager may already have detached.
                showBubble()
            }
            return START_NOT_STICKY
        }
        // A foreground service may receive repeated start requests; one projection gets one overlay.
        if (sessionStarted) return START_NOT_STICKY
        stopping.set(false)
        sessionStarted = true
        flowId = intent?.getStringExtra(EXTRA_FLOW_ID).orEmpty()
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: 0
        @Suppress("DEPRECATION") val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        if (flowId.isBlank() || resultData == null) { stopSelf(); return START_NOT_STICKY }
        startNotification()
        projection = getSystemService(MediaProjectionManager::class.java).getMediaProjection(resultCode, resultData)
        projection.registerCallback(object : MediaProjection.Callback() { override fun onStop() = stopCapture() }, null)
        val metrics = resources.displayMetrics
        reader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2)
        virtualDisplay = projection.createVirtualDisplay("FlowSnap capture", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.surface, null, null)
        if (Settings.canDrawOverlays(this)) {
            showBubble()
            // Put FlowSnap in the background after its overlay has been attached.
            // The TYPE_APPLICATION_OVERLAY button remains above Home and other apps.
            openSystemHome()
        } else openReview(null, 0f, 0f)
        return START_NOT_STICKY
    }

    private fun startNotification() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL_ID, "FlowSnap capture", NotificationManager.IMPORTANCE_LOW))
        val stop = PendingIntent.getService(this, 1, Intent(this, CaptureService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_menu_camera).setContentTitle("FlowSnap is capturing").setContentText("Use the floating button to capture a screen.").setOngoing(true).addAction(0, "Stop", stop).build()
        ServiceCompat.startForeground(this, 7, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
    }

    private fun showBubble() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        // WindowManager dimensions are pixels, unlike Compose dp. Convert the
        // visual spec explicitly so text has the same breathing room on every
        // screen density.
        val density = resources.displayMetrics.density
        val widgetSize = (60 * density).toInt()
        val innerInset = (5 * density).toInt()
        val params = WindowManager.LayoutParams(
            widgetSize,
            widgetSize,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            // Use START so x is always a normal left-origin screen coordinate.
            // END gravity reverses x semantics and made dragging appear stuck.
            gravity = Gravity.TOP or Gravity.START
            // Start every new capture session at the left edge. The drag listener
            // still permits the full 0..right-edge range after it is shown.
            x = (16 * density).toInt()
            y = (160 * density).toInt()
        }
        bubble?.let { runCatching { windowManager.removeView(it) } }
        val outerRing = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(android.graphics.Color.WHITE)
            setStroke(density.toInt().coerceAtLeast(1), android.graphics.Color.rgb(36, 81, 155))
        }
        val blueCenter = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(android.graphics.Color.rgb(36, 81, 155))
        }
        bubble = TextView(this).apply {
            text = "Drag\n& tap"
            textSize = 8f
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            typeface = Typeface.create(ResourcesCompat.getFont(this@CaptureService, R.font.urbanist_medium), Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            includeFontPadding = false
            setLineSpacing(0f, 0.9f)
            background = LayerDrawable(arrayOf(outerRing, blueCenter)).apply {
                setLayerInset(1, innerInset, innerInset, innerInset, innerInset)
            }
            elevation = 12f
            setOnTouchListener(DragCaptureListener(params))
        }
        windowManager.addView(bubble, params)
    }

    private fun openSystemHome() {
        mainHandler.post {
            startActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private inner class DragCaptureListener(private val params: WindowManager.LayoutParams) : View.OnTouchListener {
        private val touchSlop = ViewConfiguration.get(this@CaptureService).scaledTouchSlop
        private var downX = 0f; private var downY = 0f; private var initialX = 0; private var initialY = 0
        override fun onTouch(v: View, event: MotionEvent): Boolean = when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val centerX = v.width / 2f
                val centerY = v.height / 2f
                val radius = minOf(v.width, v.height) / 2f
                val distanceX = event.x - centerX
                val distanceY = event.y - centerY
                if (distanceX * distanceX + distanceY * distanceY > radius * radius) {
                    false
                } else {
                    downX = event.rawX; downY = event.rawY; initialX = params.x; initialY = params.y; true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val display = resources.displayMetrics
                val maxX = (display.widthPixels - v.width).coerceAtLeast(0)
                val maxY = (display.heightPixels - v.height).coerceAtLeast(0)
                params.x = (initialX + (event.rawX - downX).toInt()).coerceIn(0, maxX)
                params.y = (initialY + (event.rawY - downY).toInt()).coerceIn(0, maxY)
                windowManager.updateViewLayout(v, params)
                true
            }
            MotionEvent.ACTION_UP -> { if (kotlin.math.abs(event.rawX - downX) < touchSlop && kotlin.math.abs(event.rawY - downY) < touchSlop) capture(params); true }
            else -> false
        }
    }

    private fun capture(params: WindowManager.LayoutParams, attempt: Int = 0) {
        if (captureRequested && attempt == 0) return
        captureRequested = true
        // Remove our own overlay before asking ImageReader for the next projected frame.
        if (attempt == 0) {
            // Keep an attached (but completely transparent) overlay while the
            // screen frame is acquired and the review activity is launched.
            // Android 15/16 require a visible overlay window for this background
            // transition; removing it here can cause the review activity launch
            // to be silently blocked. Alpha zero keeps FlowSnap out of the bitmap.
            bubble?.alpha = 0f
            mainHandler.postDelayed({ capture(params, 1) }, 180)
            return
        }
        val image = reader.acquireLatestImage()
        if (image == null) {
            if (attempt < 8) { mainHandler.postDelayed({ capture(params, attempt + 1) }, 100); return }
            captureRequested = false; Toast.makeText(this, "Capture not ready. Try again.", Toast.LENGTH_SHORT).show(); return
        }
        val markerX = (params.x + params.width / 2f) / resources.displayMetrics.widthPixels
        val markerY = (params.y + params.height / 2f) / resources.displayMetrics.heightPixels
        // Copying a full display bitmap and PNG encoding are expensive. Keep the
        // service main thread free so FlowSnap remains responsive during capture.
        val imageClosed = AtomicBoolean(false)
        saveJob = captureScope.launch {
            val result = runCatching { saveCaptureImage(image, imageClosed) }
            withContext(Dispatchers.Main.immediate) {
                result.onSuccess { path ->
                    openReview(path, markerX, markerY)
                }.onFailure {
                    captureRequested = false
                    bubble?.alpha = 1f
                    Toast.makeText(this@CaptureService, "Could not save screenshot. Try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
        // If the service is stopped before this coroutine begins, release the
        // acquired ImageReader frame rather than keeping one of its two slots.
        saveJob?.invokeOnCompletion {
            if (imageClosed.compareAndSet(false, true)) runCatching { image.close() }
        }
    }

    /** Converts one ImageReader frame into an app-private PNG on Dispatchers.IO. */
    private fun saveCaptureImage(image: android.media.Image, imageClosed: AtomicBoolean): String {
        var paddedBitmap: Bitmap? = null
        var bitmap: Bitmap? = null
        try {
            val plane = image.planes[0]
            val width = image.width
            val height = image.height
            val rowPadding = plane.rowStride - plane.pixelStride * width
            val padded = Bitmap.createBitmap(
                width + rowPadding / plane.pixelStride,
                height,
                Bitmap.Config.ARGB_8888
            )
            paddedBitmap = padded
            padded.copyPixelsFromBuffer(plane.buffer)
            val captureBitmap = if (padded.width == width) {
                padded
            } else {
                Bitmap.createBitmap(padded, 0, 0, width, height)
            }
            bitmap = captureBitmap
            val output = ScreenshotStorage.newCaptureFile(this, flowId)
            try {
                FileOutputStream(output).use { stream ->
                    check(captureBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                        "PNG encoding failed"
                    }
                }
            } catch (error: Throwable) {
                output.delete()
                throw error
            }
            return output.absolutePath
        } finally {
            if (imageClosed.compareAndSet(false, true)) image.close()
            bitmap?.takeIf { !it.isRecycled }?.recycle()
            paddedBitmap?.takeIf { it !== bitmap && !it.isRecycled }?.recycle()
        }
    }

    private fun openReview(path: String?, markerX: Float, markerY: Float) {
        // Delivery is decoupled from UI navigation: the activity owns routing the capture-ready event.
        sendBroadcast(Intent(ACTION_CAPTURE_READY)
            .setPackage(packageName)
            .putExtra(EXTRA_FLOW_ID, flowId)
            .putExtra(EXTRA_PATH, path)
            .putExtra(EXTRA_MARKER_X, markerX)
            .putExtra(EXTRA_MARKER_Y, markerY))
        // The transparent anchor is no longer needed after the launch request.
        // Keep a reference to the *old* overlay.  A user can press Keep + Next
        // before this delayed callback runs; in that case ACTION_RESUME has
        // already created a new button.  Removing the mutable `bubble` field
        // here used to remove that new button, leaving the next capture stuck.
        val reviewBubble = bubble
        mainHandler.postDelayed({
            if (bubble === reviewBubble) {
                reviewBubble?.let { runCatching { windowManager.removeView(it) } }
                bubble = null
            }
        }, 500)
    }

    private fun stopCapture() {
        if (!stopping.compareAndSet(false, true)) return
        mainHandler.removeCallbacksAndMessages(null)
        saveJob?.cancel()
        saveJob = null
        bubble?.let { runCatching { windowManager.removeView(it) } }
        bubble = null
        sessionStarted = false
        captureRequested = false
        runCatching { virtualDisplay?.release() }
        virtualDisplay = null
        runCatching { reader.close() }
        runCatching { projection.stop() }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    override fun onDestroy() {
        stopCapture()
        captureScope.cancel()
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object { const val CHANNEL_ID = "capture"; const val ACTION_STOP = "com.flowsnap.STOP"; const val ACTION_RESUME = "com.flowsnap.RESUME"; const val ACTION_CAPTURE_READY = "com.flowsnap.CAPTURE_READY"; const val EXTRA_FLOW_ID = "flowId"; const val EXTRA_RESULT_CODE = "resultCode"; const val EXTRA_RESULT_DATA = "resultData"; const val EXTRA_PATH = "path"; const val EXTRA_MARKER_X = "markerX"; const val EXTRA_MARKER_Y = "markerY" }
}
