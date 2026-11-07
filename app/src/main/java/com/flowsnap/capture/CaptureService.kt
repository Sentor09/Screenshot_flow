package com.flowsnap.capture

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
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
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.res.ResourcesCompat
import com.flowsnap.MainActivity
import com.flowsnap.R
import com.flowsnap.data.ScreenshotStorage
import java.io.FileOutputStream

/** Owns exactly one user-approved MediaProjection session and its draggable capture control. */
class CaptureService : Service() {
    private lateinit var projection: MediaProjection
    private lateinit var reader: ImageReader
    private lateinit var windowManager: WindowManager
    private var bubble: View? = null
    private var flowId = ""
    private var sessionStarted = false
    private var captureRequested = false

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
        projection.createVirtualDisplay("FlowSnap capture", metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.surface, null, null)
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
        val params = WindowManager.LayoutParams(128, 128, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START; x = 48; y = 180 }
        bubble?.let { runCatching { windowManager.removeView(it) } }
        bubble = TextView(this).apply {
            text = "●\nCAPTURE"
            textSize = 11f
            gravity = Gravity.CENTER
            typeface = ResourcesCompat.getFont(this@CaptureService, R.font.urbanist_medium)
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.rgb(38, 78, 150))
            elevation = 12f
            setOnTouchListener(DragCaptureListener(params))
        }
        windowManager.addView(bubble, params)
    }

    private fun openSystemHome() {
        Handler(Looper.getMainLooper()).post {
            startActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private inner class DragCaptureListener(private val params: WindowManager.LayoutParams) : View.OnTouchListener {
        private var downX = 0f; private var downY = 0f; private var initialX = 0; private var initialY = 0
        override fun onTouch(v: View, event: MotionEvent): Boolean = when (event.action) {
            MotionEvent.ACTION_DOWN -> { downX = event.rawX; downY = event.rawY; initialX = params.x; initialY = params.y; true }
            MotionEvent.ACTION_MOVE -> { params.x = initialX + (event.rawX - downX).toInt(); params.y = initialY + (event.rawY - downY).toInt(); windowManager.updateViewLayout(v, params); true }
            MotionEvent.ACTION_UP -> { if (kotlin.math.abs(event.rawX - downX) < 12 && kotlin.math.abs(event.rawY - downY) < 12) capture(params); true }
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
            Handler(Looper.getMainLooper()).postDelayed({ capture(params, 1) }, 180)
            return
        }
        val image = reader.acquireLatestImage()
        if (image == null) {
            if (attempt < 8) { Handler(Looper.getMainLooper()).postDelayed({ capture(params, attempt + 1) }, 100); return }
            captureRequested = false; Toast.makeText(this, "Capture not ready. Try again.", Toast.LENGTH_SHORT).show(); return
        }
        val plane = image.planes[0]; val width = image.width; val height = image.height; val rowPadding = plane.rowStride - plane.pixelStride * width
        val bitmap = Bitmap.createBitmap(width + rowPadding / plane.pixelStride, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(plane.buffer); image.close()
        // Store the full-quality PNG in FlowSnap's app-private internal
        // storage. Room records this file path when the user keeps it.
        val output = ScreenshotStorage.newCaptureFile(this, flowId)
        FileOutputStream(output).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
        openReview(output.absolutePath, (params.x + 64f) / resources.displayMetrics.widthPixels, (params.y + 64f) / resources.displayMetrics.heightPixels)
    }

    private fun openReview(path: String?, markerX: Float, markerY: Float) {
        val reviewIntent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(EXTRA_FLOW_ID, flowId)
            .putExtra(EXTRA_PATH, path)
            .putExtra(EXTRA_MARKER_X, markerX)
            .putExtra(EXTRA_MARKER_Y, markerY)

        // Route the user-initiated overlay tap through a PendingIntent with the
        // Android 14+ background-activity-launch opt-in. This keeps the review
        // screen reliable when FlowSnap is behind Home or another app.
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val creatorOptions = if (android.os.Build.VERSION.SDK_INT >= 35) {
            ActivityOptions.makeBasic().apply {
                setPendingIntentCreatorBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                )
            }.toBundle()
        } else null
        val reviewPendingIntent = PendingIntent.getActivity(this, REVIEW_REQUEST_CODE, reviewIntent, flags, creatorOptions)
        runCatching {
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                val sendOptions = ActivityOptions.makeBasic().apply {
                    pendingIntentBackgroundActivityStartMode = ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                }
                reviewPendingIntent.send(this, 0, null, null, null, null, sendOptions.toBundle())
            } else {
                reviewPendingIntent.send()
            }
        }.onFailure {
            // Keep the old direct launch as a fallback for vendor builds that
            // reject BAL options despite an active overlay window.
            startActivity(reviewIntent)
        }
        // The transparent anchor is no longer needed after the launch request.
        // Keep a reference to the *old* overlay.  A user can press Keep + Next
        // before this delayed callback runs; in that case ACTION_RESUME has
        // already created a new button.  Removing the mutable `bubble` field
        // here used to remove that new button, leaving the next capture stuck.
        val reviewBubble = bubble
        Handler(Looper.getMainLooper()).postDelayed({
            if (bubble === reviewBubble) {
                reviewBubble?.let { runCatching { windowManager.removeView(it) } }
                bubble = null
            }
        }, 500)
    }

    private fun stopCapture() { bubble?.let { runCatching { windowManager.removeView(it) } }; bubble = null; sessionStarted = false; captureRequested = false; runCatching { reader.close() }; runCatching { projection.stop() }; stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object { const val CHANNEL_ID = "capture"; const val ACTION_STOP = "com.flowsnap.STOP"; const val ACTION_RESUME = "com.flowsnap.RESUME"; const val EXTRA_FLOW_ID = "flowId"; const val EXTRA_RESULT_CODE = "resultCode"; const val EXTRA_RESULT_DATA = "resultData"; const val EXTRA_PATH = "path"; const val EXTRA_MARKER_X = "markerX"; const val EXTRA_MARKER_Y = "markerY"; private const val REVIEW_REQUEST_CODE = 92 }
}
