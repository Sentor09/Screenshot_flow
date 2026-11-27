@file:Suppress("SpellCheckingInspection", "GrazieInspection")

package com.flowsnap

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.Intent
import android.content.ContentValues
import android.content.ClipData
import android.graphics.pdf.PdfDocument
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.Environment
import android.media.projection.MediaProjectionConfig
import android.provider.Settings
import android.provider.MediaStore
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.LocalActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.core.graphics.scale
import androidx.core.graphics.drawable.toDrawable
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.flowsnap.canvas.InitialCanvasLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.flowsnap.capture.CaptureService
import com.flowsnap.data.FlowDatabase
import com.flowsnap.data.FlowEntity
import com.flowsnap.data.ScreenEntity
import com.flowsnap.data.ScreenshotStorage
import com.flowsnap.data.TransitionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import java.util.UUID
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class CaptureReview(val flowId: String, val path: String, val markerX: Float, val markerY: Float)
data class CaptureCrop(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    companion object { val Full = CaptureCrop(0f, 0f, 1f, 1f) }
    fun valid(): CaptureCrop {
        val l = left.coerceIn(0f, .92f); val t = top.coerceIn(0f, .92f)
        return copy(left = l, top = t, right = right.coerceIn(l + .08f, 1f), bottom = bottom.coerceIn(t + .08f, 1f))
    }
}

@Composable private fun SmallCanvasDrawableControl(@androidx.annotation.DrawableRes iconRes: Int, label: String, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = UiColor.primaryContainer,
        modifier = Modifier.size(40.dp).clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = UiColor.primary,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

private enum class ExportProgressKind { JPEG, PNG, PDF, PDF_AND_SVG, SVG }

private val Urbanist = FontFamily(
    Font(R.font.urbanist_regular, FontWeight.Normal),
    Font(R.font.urbanist_medium, FontWeight.Medium),
    Font(R.font.urbanist_bold, FontWeight.Bold)
)

private val FlowSnapTypography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = Urbanist),
        displayMedium = base.displayMedium.copy(fontFamily = Urbanist),
        displaySmall = base.displaySmall.copy(fontFamily = Urbanist),
        headlineLarge = base.headlineLarge.copy(fontFamily = Urbanist),
        headlineMedium = base.headlineMedium.copy(fontFamily = Urbanist),
        headlineSmall = base.headlineSmall.copy(fontFamily = Urbanist),
        titleLarge = base.titleLarge.copy(fontFamily = Urbanist),
        titleMedium = base.titleMedium.copy(fontFamily = Urbanist),
        titleSmall = base.titleSmall.copy(fontFamily = Urbanist),
        bodyLarge = base.bodyLarge.copy(fontFamily = Urbanist),
        bodyMedium = base.bodyMedium.copy(fontFamily = Urbanist),
        bodySmall = base.bodySmall.copy(fontFamily = Urbanist),
        labelLarge = base.labelLarge.copy(fontFamily = Urbanist),
        labelMedium = base.labelMedium.copy(fontFamily = Urbanist),
        labelSmall = base.labelSmall.copy(fontFamily = Urbanist)
    )
}

class MainActivity : ComponentActivity() {
    private var review by mutableStateOf<CaptureReview?>(null)
    private val captureReadyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            acceptReviewIntent(intent)
        }
    }
    @Suppress("DEPRECATION") // Fallback colors for devices with opaque system-bar regions.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(FlowSnapLightColorScheme.background.toArgb().toDrawable())
        enableEdgeToEdge()
        // enableEdgeToEdge establishes transparent system bars and keeps the
        // content layout stable while individual overlays are shown.
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        restoreReview(savedInstanceState)
        acceptReviewIntent(intent)
        ContextCompat.registerReceiver(this, captureReadyReceiver, IntentFilter(CaptureService.ACTION_CAPTURE_READY), ContextCompat.RECEIVER_NOT_EXPORTED)
        setContent {
            val colorScheme = FlowSnapLightColorScheme
            val extraColors = FlowSnapLightExtraColors
            val onboardingPreferences = remember { getSharedPreferences(APP_SETTINGS, MODE_PRIVATE) }
            var onboardingComplete by remember { mutableStateOf(onboardingPreferences.getBoolean("onboarding_complete", false)) }
            var showSplash by remember { mutableStateOf(savedInstanceState == null && review == null) }
            androidx.compose.runtime.LaunchedEffect(showSplash) {
                if (showSplash) {
                    delay(2000.milliseconds)
                    showSplash = false
                }
            }
            val splashVisible = showSplash && review == null
            SideEffect {
                window.statusBarColor = (if (splashVisible) Color(0xFF254D72) else colorScheme.background).toArgb()
                window.navigationBarColor = (if (splashVisible) Color(0xFF37265F) else colorScheme.background).toArgb()
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !splashVisible
                WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !splashVisible
            }
            MaterialTheme(colorScheme = colorScheme, typography = FlowSnapTypography) {
                CompositionLocalProvider(LocalFlowSnapExtraColors provides extraColors) {
                    if (splashVisible) {
                        FlowSnapSplash()
                    } else if (!onboardingComplete && review == null) {
                        FlowSnapOnboarding { startGuide ->
                            onboardingPreferences.edit {
                                putBoolean("onboarding_complete", true)
                                putString("capture_guide_stage", if (startGuide) "create" else "")
                            }
                            onboardingComplete = true
                        }
                    } else {
                        FlowSnapApp(
                            review = review,
                            dismissReview = { review = null }
                        )
                    }
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); acceptReviewIntent(intent) }
    private fun acceptReviewIntent(intent: Intent) { val path = intent.getStringExtra(CaptureService.EXTRA_PATH) ?: return; review = CaptureReview(intent.getStringExtra(CaptureService.EXTRA_FLOW_ID).orEmpty(), path, intent.getFloatExtra(CaptureService.EXTRA_MARKER_X, .5f), intent.getFloatExtra(CaptureService.EXTRA_MARKER_Y, .5f)) }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        review?.let {
            outState.putString("review_flow_id", it.flowId)
            outState.putString("review_path", it.path)
            outState.putFloat("review_marker_x", it.markerX)
            outState.putFloat("review_marker_y", it.markerY)
        }
    }
    private fun restoreReview(state: Bundle?) {
        val path = state?.getString("review_path") ?: return
        review = CaptureReview(state.getString("review_flow_id").orEmpty(), path, state.getFloat("review_marker_x", .5f), state.getFloat("review_marker_y", .5f))
    }
    override fun onDestroy() {
        unregisterReceiver(captureReadyReceiver)
        super.onDestroy()
    }
}

@Composable private fun FlowSnapOnboarding(onFinish: (Boolean) -> Unit) {
    val titles = listOf("Capture the journey", "Build your flow", "Share your work")
    val descriptions = listOf(
        "Capture screenshots as you move through an app. Mark the spot that leads to the next step.",
        "Turn your screenshots into a clear diagram. Adjust the layout, labels, and connector colors.",
        "Export your diagram as an image, PDF, or editable SVG when you're ready to share."
    )
    val icons = listOf(Icons.Default.PhoneAndroid, Icons.Default.Apps, Icons.Default.PictureAsPdf)
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { titles.size })
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val page = pagerState.currentPage
    Column(
        modifier = Modifier.fillMaxSize().background(UiColor.background)
            .windowInsetsPadding(WindowInsets.systemBars).padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("FlowSnap", color = UiColor.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { onFinish(false) }) { Text("Skip", color = UiColor.onSurfaceVariant, fontSize = 14.sp) }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) { pageIndex ->
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(180.dp).background(UiColor.surfaceVariant, RoundedCornerShape(42.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(Modifier.size(112.dp).background(UiColor.surface, RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center) {
                        Icon(icons[pageIndex], contentDescription = null, tint = UiColor.primary, modifier = Modifier.size(58.dp))
                    }
                }
                Spacer(Modifier.height(36.dp))
                Text(titles[pageIndex], color = UiColor.onSurface, fontSize = 27.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(descriptions[pageIndex], color = UiColor.onSurfaceVariant, fontSize = 16.sp, lineHeight = 23.sp, textAlign = TextAlign.Center)
            }
        }
        val indicatorColor = UiColor.primary
        val inactiveIndicatorColor = UiColor.outline
        Canvas(Modifier.size(width = 72.dp, height = 8.dp)) {
            val step = 24.dp.toPx()
            val dotRadius = 4.dp.toPx()
            repeat(titles.size) { index ->
                drawCircle(inactiveIndicatorColor, dotRadius, Offset(step / 2f + index * step, size.height / 2f))
            }
            val position = (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .coerceIn(0f, titles.lastIndex.toFloat())
            drawRoundRect(
                color = indicatorColor,
                topLeft = Offset(position * step, 0f),
                size = androidx.compose.ui.geometry.Size(step, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
            )
        }
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = { if (page == titles.lastIndex) onFinish(true) else scope.launch { pagerState.animateScrollToPage(page + 1) } },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UiColor.primary, contentColor = UiColor.onPrimary)
        ) { Text(if (page == titles.lastIndex) "Get started" else "Next", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        if (page > 0) {
            TextButton(onClick = { scope.launch { pagerState.animateScrollToPage(page - 1) } }, modifier = Modifier.height(48.dp)) {
                Text("Back", color = UiColor.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable private fun FlowSnapSplash() {
    val iconProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    val wordmarkProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        // Give Compose a frame to display the initial state before animating.
        androidx.compose.runtime.withFrameNanos { }
        launch { iconProgress.animateTo(1f, animationSpec = tween(durationMillis = 850)) }
        delay(300.milliseconds)
        wordmarkProgress.animateTo(1f, animationSpec = tween(durationMillis = 850))
    }
    Box(
        modifier = Modifier.fillMaxSize().drawBehind {
            drawRect(Brush.linearGradient(
                colors = listOf(Color(0xFF254D72), Color(0xFF253E82), Color(0xFF303277), Color(0xFF37265F)),
                start = Offset.Zero,
                end = Offset(size.width * .8f, size.height)
            ))
            drawRect(Brush.radialGradient(
                colors = listOf(Color(0xFF64418F).copy(alpha = .32f), Color.Transparent),
                center = Offset(size.width * .9f, size.height * .75f),
                radius = size.maxDimension * .65f
            ))
        },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.splash_app_icon),
                contentDescription = null,
                modifier = Modifier.size(88.dp).clip(RoundedCornerShape(32.dp)).graphicsLayer {
                    alpha = iconProgress.value
                    scaleX = .65f + .35f * iconProgress.value
                    scaleY = scaleX
                    translationY = (1f - iconProgress.value) * (-24).dp.toPx()
                }.drawWithContent {
                    clipRect(right = size.width * iconProgress.value) { this@drawWithContent.drawContent() }
                }
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "FlowSnap",
                modifier = Modifier.graphicsLayer {
                    alpha = wordmarkProgress.value
                    translationY = (1f - wordmarkProgress.value) * 24.dp.toPx()
                }.drawWithContent {
                    clipRect(right = size.width * wordmarkProgress.value) { this@drawWithContent.drawContent() }
                },
                color = Color.White,
                fontFamily = FontFamily.SansSerif,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = .5.sp
            )
        }
    }
}

class FlowViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val dao = FlowDatabase.create(application).flowDao()
    // Keep the gallery query alive for the app lifetime. This prevents a
    // transient empty Home grid when returning from a completed capture.
    val flows = dao.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun createFlow(open: (FlowEntity) -> Unit) = viewModelScope.launch {
        val preferences = getApplication<android.app.Application>()
            .getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val flow = FlowEntity(
            id = UUID.randomUUID().toString(),
            name = preferences.getString(PREF_DEFAULT_FLOW_NAME, "My new app flow")
                ?.trim().orEmpty().ifBlank { "My new app flow" },
            createdAt = now,
            updatedAt = now,
            showMobileStencils = true,
            connectorColor = preferences.getLong(PREF_CONNECTOR_COLOR, ConnectorPalette.Green)
        )
        dao.insertFlow(flow)
        open(flow)
    }
    fun updateFlowMetadata(
        flow: FlowEntity,
        title: String,
        version: String,
        createdBy: String,
        showTitle: Boolean,
        showVersion: Boolean,
        showCreatedAt: Boolean,
        showCreatedBy: Boolean,
        showAppVersion: Boolean,
        onSaved: (FlowEntity) -> Unit
    ) = viewModelScope.launch {
        val updated = flow.copy(
            name = title.trim().ifBlank { flow.name },
            diagramVersion = version.trim().ifBlank { flow.diagramVersion },
            createdBy = createdBy.trim().ifBlank { flow.createdBy },
            showTitle = showTitle,
            showVersion = showVersion,
            showCreatedAt = showCreatedAt,
            showCreatedBy = showCreatedBy,
            showAppVersion = showAppVersion,
            updatedAt = System.currentTimeMillis()
        )
        dao.updateFlow(updated)
        onSaved(updated)
    }
    fun updateDiagramAppearance(
        flow: FlowEntity,
        showMobileStencils: Boolean,
        connectorColor: Long,
        preferredColumns: Int,
        onSaved: (FlowEntity) -> Unit
    ) = viewModelScope.launch {
        val updated = flow.copy(
            showMobileStencils = showMobileStencils,
            connectorColor = connectorColor,
            preferredColumns = preferredColumns.coerceIn(1, 9),
            updatedAt = System.currentTimeMillis()
        )
        dao.updateFlow(updated)
        // The column setting controls both the preview and automatic canvas
        // layout. Preserve manual positions unless the grid setting changed.
        if (updated.preferredColumns != flow.preferredColumns) {
            val positions = dao.screensForFlow(flow.id).associate { screen ->
                screen.id to InitialCanvasLayout.positionFor(screen.position, updated.preferredColumns)
            }
            dao.updateCanvasLayout(flow.id, positions.mapValues { it.value.x to it.value.y })
        }
        onSaved(updated)
    }
    private suspend fun persistCapture(review: CaptureReview) = dao.persistCapture(
        flowId = review.flowId,
        imagePath = review.path,
        markerX = review.markerX,
        markerY = review.markerY,
        now = System.currentTimeMillis()
    )
    fun keepCapture(review: CaptureReview, onComplete: (Boolean) -> Unit) = viewModelScope.launch {
        val saved = runCatching { persistCapture(review) }.getOrDefault(false)
        if (!saved) ScreenshotStorage.delete(getApplication(), review.path)
        onComplete(saved)
    }
    fun keepCaptureAndOpenDiagram(review: CaptureReview, onSaved: (FlowEntity?) -> Unit) = viewModelScope.launch {
        val saved = runCatching { persistCapture(review) }.getOrDefault(false)
        if (!saved) ScreenshotStorage.delete(getApplication(), review.path)
        onSaved(if (saved) dao.flow(review.flowId) else null)
    }
    fun cropCapture(review: CaptureReview, crop: CaptureCrop, done: (Boolean) -> Unit) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val ok = runCatching {
            val bitmap = android.graphics.BitmapFactory.decodeFile(review.path) ?: error("Unable to load capture")
            try {
                val c = crop.valid(); val x = (bitmap.width * c.left).toInt(); val y = (bitmap.height * c.top).toInt()
                val w = (bitmap.width * (c.right - c.left)).toInt().coerceAtLeast(1); val h = (bitmap.height * (c.bottom - c.top)).toInt().coerceAtLeast(1)
                val cut = android.graphics.Bitmap.createBitmap(bitmap, x, y, w.coerceAtMost(bitmap.width - x), h.coerceAtMost(bitmap.height - y))
                try { java.io.File(review.path).outputStream().use { check(cut.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)) } } finally { cut.recycle() }
                invalidateScreenshotBitmap(review.path)
            } finally { bitmap.recycle() }
        }.isSuccess
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { done(ok) }
    }
    fun deleteFlow(flow: FlowEntity, onDeleted: () -> Unit) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        // Commit the cascaded Room delete first. Any failed file deletes are removed by startup reconciliation.
        val paths = dao.deleteFlowAndGetImagePaths(flow.id)
        paths.forEach { path -> runCatching { ScreenshotStorage.delete(getApplication(), path) } }
        ScreenshotStorage.deleteFlowFolder(getApplication(), flow.id)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onDeleted() }
    }
    fun deleteScreen(screen: ScreenEntity, onDeleted: () -> Unit) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        dao.deleteScreenAndGetImagePath(screen.id)?.let { path -> runCatching { ScreenshotStorage.delete(getApplication(), path) } }
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onDeleted() }
    }
    fun updateScreenPointer(screen: ScreenEntity, markerX: Float, markerY: Float, onSaved: () -> Unit) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        dao.updateScreenPointer(screen, markerX, markerY)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onSaved() }
    }
    fun replaceScreenImage(screen: ScreenEntity, source: Uri, onComplete: (Boolean) -> Unit) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        var replacement: java.io.File? = null
        val result = runCatching {
            val newReplacement = ScreenshotStorage.newCaptureFile(getApplication(), screen.flowId)
            replacement = newReplacement
            getApplication<android.app.Application>().contentResolver.openInputStream(source)?.use { input ->
                newReplacement.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Unable to read selected image")
            dao.updateScreen(screen.copy(imagePath = newReplacement.absolutePath))
            dao.touchFlow(screen.flowId, System.currentTimeMillis())
            runCatching { ScreenshotStorage.delete(getApplication(), screen.imagePath) }
        }
        if (result.isFailure) replacement?.let { ScreenshotStorage.delete(getApplication(), it.absolutePath) }
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onComplete(result.isSuccess) }
    }
    fun screens(flowId: String) = dao.observeScreens(flowId)
    fun transitions(flowId: String) = dao.observeTransitions(flowId)
    fun flow(flowId: String) = dao.observeFlow(flowId)
    fun cleanupOrphanedCaptures(pendingReviewPath: String?) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        ScreenshotStorage.deleteUnreferencedFiles(getApplication(), dao.referencedImagePaths().toSet() + listOfNotNull(pendingReviewPath))
    }

    private fun <T> launchExport(work: () -> Result<T>, onResult: (Result<T>) -> Unit) =
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val result = try {
                work()
            } catch (error: Throwable) {
                Result.failure(error)
            }
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(result) }
        }

    /** Creates a temporary PDF and SVG for Android's native share sheet. */
    fun exportForSharing(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<ArrayList<Uri>>) -> Unit) =
        launchExport({
            runCatching {
                val context = getApplication<android.app.Application>()
                val exportDirectory = java.io.File(context.cacheDir, "shared_exports").apply { mkdirs() }
                val safeName = flow.name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "flowsnap" }
                val exportName = "$safeName-${System.currentTimeMillis()}"
                val pdfFile = java.io.File(exportDirectory, "$exportName.pdf")
                val svgFile = java.io.File(exportDirectory, "$exportName.svg")
                try {
                    writeFlowPdf(context, Uri.fromFile(pdfFile), screens, transitions, flow).getOrThrow()
                    writeFlowSvg(context, screens, transitions, flow, destination = svgFile).getOrThrow()
                    arrayListOf(
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile),
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", svgFile)
                    )
                } catch (error: Throwable) {
                    pdfFile.delete()
                    svgFile.delete()
                    throw error
                }
            }
        }, onResult)
    fun exportJpeg(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Uri>) -> Unit) =
        launchExport({ writeFlowJpeg(getApplication(), screens, transitions, flow) }, onResult)
    fun exportPng(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Uri>) -> Unit) =
        launchExport({ writeFlowPng(getApplication(), screens, transitions, flow) }, onResult)
    fun exportPdf(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Uri>) -> Unit) =
        launchExport({ writeFlowPdfToDownloads(getApplication(), screens, transitions, flow) }, onResult)
    @Suppress("unused")
    fun exportDrawio(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Uri>) -> Unit) =
        launchExport({ writeFlowDrawio(getApplication(), screens, transitions, flow) }, onResult)
    fun exportSvg(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Uri>) -> Unit) =
        launchExport({ writeFlowSvg(getApplication(), screens, transitions, flow) }, onResult)
    @Suppress("unused")
    fun exportSketch(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Uri>) -> Unit) =
        launchExport({ writeFlowSketch(getApplication(), screens, transitions, flow) }, onResult)

}

private const val APP_SETTINGS = "flowsnap_settings"
private const val PREF_DEFAULT_FLOW_NAME = "default_flow_name"
private const val PREF_CONNECTOR_COLOR = "connector_color"

/** A high-contrast selection mark that remains legible on every connector color. */
@Composable private fun ConnectorSelectionMark(color: Color, modifier: Modifier = Modifier) {
    Icon(
        Icons.Default.Check,
        contentDescription = "Selected",
        tint = if (color.luminance() > .18f) Color.Black else Color.White,
        modifier = modifier
    )
}

@Suppress("DEPRECATION") // Fallback colors for devices with opaque system-bar regions.
@Composable private fun FlowSnapApp(
    review: CaptureReview?,
    dismissReview: () -> Unit,
    vm: FlowViewModel = viewModel()
) {
    Box(Modifier.fillMaxSize().background(UiColor.surface)) {
        var selectedFlowId by rememberSaveable { mutableStateOf<String?>(null) }
        var lightboxOpen by remember { mutableStateOf(false) }
        val workspaceOpen = selectedFlowId != null && review == null
        val hostActivity = LocalActivity.current
        val appBackground = UiColor.background
        val workspaceStatusBar = UiColor.extra.statusBar
        val workspaceBackground = UiColor.extra.canvasBackground
        val darkAppTheme = appBackground.luminance() <= .5f
        // The diagram workspace is intentionally charcoal in either app
        // theme, including the edge-to-edge system-bar areas.
        SideEffect {
            hostActivity?.window?.let { window ->
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                if (workspaceOpen && darkAppTheme) {
                    window.statusBarColor = workspaceStatusBar.toArgb()
                    window.navigationBarColor = workspaceBackground.toArgb()
                    controller.isAppearanceLightStatusBars = false
                    controller.isAppearanceLightNavigationBars = false
                } else {
                    window.statusBarColor = appBackground.toArgb()
                    window.navigationBarColor = appBackground.toArgb()
                    val useLightIcons = appBackground.luminance() <= .5f
                    controller.isAppearanceLightStatusBars = !useLightIcons
                    controller.isAppearanceLightNavigationBars = !useLightIcons
                }
            }
        }
        androidx.compose.runtime.LaunchedEffect(review?.path) { vm.cleanupOrphanedCaptures(review?.path) }
        if (review != null) CaptureReviewScreen(review, vm, dismissReview, onFinish = { savedFlow ->
            if (savedFlow != null) selectedFlowId = savedFlow.id
            dismissReview()
        }) else if (selectedFlowId == null) {
            HomeScreen(vm) { selectedFlowId = it.id }
        } else {
            // The preceding null branch guarantees this route has an id.
            val flowId = requireNotNull(selectedFlowId)
            DiagramScreen(
                flowId = flowId,
                vm = vm,
                goBack = { selectedFlowId = null },
                onLightboxVisibilityChange = { lightboxOpen = it }
            )
        }
        // Paint the edge-to-edge inset to match its current destination.
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(if ((workspaceOpen && darkAppTheme) || lightboxOpen) UiColor.extra.statusBar else UiColor.surface)
                .zIndex(10f)
        )
    }
}

@Composable private fun HomeScreen(
    vm: FlowViewModel,
    openFlow: (FlowEntity) -> Unit
) {
    val flows by vm.flows.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val guidePreferences = remember { context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE) }
    val showCreateGuide = remember { guidePreferences.getString("capture_guide_stage", "") == "create" }
    // Keep gallery cards above the Android gesture/three-button navigation area.
    Column(Modifier.fillMaxSize().background(UiColor.background).windowInsetsPadding(WindowInsets.systemBars)) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .zIndex(1f)
                .shadow(
                    elevation = 6.dp,
                    ambientColor = UiColor.transparent,
                    spotColor = UiColor.onSurface.copy(alpha = .30f)
                )
                .background(UiColor.surface)
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Flow", color = UiColor.secondary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Light, maxLines = 1)
            Text("Snap", color = UiColor.primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Light, maxLines = 1)
            Spacer(Modifier.weight(1f))
            Text(
                text = "${flows.size} ${if (flows.size == 1) "diagram" else "diagrams"}",
                color = UiColor.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
        if (showCreateGuide) {
            GuideHint("1 of 4", "Tap Create diagram to start your first flow.", Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp))
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CreateFlowTile(Modifier.fillMaxWidth().aspectRatio(.72f)) {
                    vm.createFlow { flow ->
                        if (showCreateGuide) guidePreferences.edit { putString("capture_guide_stage", "capture") }
                        openFlow(flow)
                    }
                }
            }
            items(flows, key = { it.id }) { flow ->
                FlowTile(flow, vm) { openFlow(flow) }
            }
        }
    }
}

@Composable private fun GuideHint(step: String, message: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = UiColor.primaryContainer) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(step, color = UiColor.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(message, color = UiColor.onSurface, fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable private fun CreateFlowTile(modifier: Modifier, onClick: () -> Unit) = Card(
    onClick = onClick,
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    colors = CardDefaults.cardColors(containerColor = UiColor.surface)
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(12.dp),
                color = UiColor.primary,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_flow_add), "Create diagram", tint = UiColor.onPrimary, modifier = Modifier.size(42.dp))
                }
            }
        }
        Text("Create diagram", modifier = Modifier.fillMaxWidth(), color = UiColor.primary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable private fun FlowTile(flow: FlowEntity, vm: FlowViewModel, onClick: () -> Unit) {
    val screens by vm.screens(flow.id).collectAsStateWithLifecycle(initialValue = emptyList())
    // Keep gallery previews readable: one recognizable cover screen plus a
    // small stack communicates a flow without shrinking every screen.
    val previewScreens = screens.take(3)
    val bitmaps = rememberScreenBitmaps(previewScreens, maxDimension = 360)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(.72f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = UiColor.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(Modifier.padding(12.dp).fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Flow preview", color = UiColor.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp)
                Spacer(Modifier.weight(1f))
                Surface(color = UiColor.primaryContainer, shape = RoundedCornerShape(20.dp)) {
                    Text("${screens.size} ${if (screens.size == 1) "screen" else "screens"}", Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = UiColor.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(9.dp))
            Box(
                Modifier.weight(1f).fillMaxWidth()
                    .background(UiColor.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(7.dp)
            ) {
                if (previewScreens.isNotEmpty()) {
                    GalleryFlowPreview(previewScreens, bitmaps)
                } else {
                    Text("Your captured flow\nwill appear here", Modifier.align(Alignment.Center), color = UiColor.onSurfaceVariant.copy(alpha = .65f), fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                }
            }
            Text(
                flow.name,
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                color = UiColor.secondary,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text("Updated ${java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(java.util.Date(flow.updatedAt))}", Modifier.padding(top = 0.dp), color = UiColor.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

/** A readable gallery cover: the entry screen is prominent and later steps form a small stack. */
@Composable private fun GalleryFlowPreview(
    screens: List<ScreenEntity>,
    bitmaps: Map<String, androidx.compose.ui.graphics.ImageBitmap>
) {
    Box(Modifier.fillMaxSize()) {
        GalleryPreviewImage(
            screen = screens.first(),
            bitmaps = bitmaps,
            modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight(.96f).fillMaxWidth(if (screens.size == 1) .72f else .58f)
        )
        screens.getOrNull(1)?.let { screen ->
            GalleryPreviewImage(
                screen = screen,
                bitmaps = bitmaps,
                modifier = Modifier.align(Alignment.TopEnd).fillMaxWidth(.34f).fillMaxHeight(.45f)
            )
        }
        screens.getOrNull(2)?.let { screen ->
            GalleryPreviewImage(
                screen = screen,
                bitmaps = bitmaps,
                modifier = Modifier.align(Alignment.BottomEnd).fillMaxWidth(.34f).fillMaxHeight(.45f)
            )
        }
    }
}

@Composable private fun GalleryPreviewImage(
    screen: ScreenEntity,
    bitmaps: Map<String, androidx.compose.ui.graphics.ImageBitmap>,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(7.dp),
        color = UiColor.extra.screenshotPlaceholder,
        border = BorderStroke(1.dp, UiColor.onPrimary),
        shadowElevation = 1.dp
    ) {
        val bitmap = bitmaps[screen.id]
        if (bitmap != null) {
            androidx.compose.foundation.Image(
                bitmap = bitmap,
                contentDescription = "Flow screen preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable private fun CaptureReviewScreen(review: CaptureReview, vm: FlowViewModel, dismiss: () -> Unit, onFinish: (FlowEntity?) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val guidePreferences = remember { context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE) }
    val guideStage = remember(review) { guidePreferences.getString("capture_guide_stage", "").orEmpty() }
    val hostActivity = LocalActivity.current
    val returnToCapturedApp = {
        // Re-attach the overlay before leaving FlowSnap. ACTION_RESUME never
        // opens HOME: Android then exposes the task that was behind this
        // review activity (the exact app/screen the user was capturing).
        context.startService(Intent(context, CaptureService::class.java).setAction(CaptureService.ACTION_RESUME))
        dismiss()
        // Let Compose remove the review destination first. Moving the task on
        // the next UI frame avoids a race that can otherwise leave FlowSnap in
        // front of the previous external app on newer Android releases.
        hostActivity?.window?.decorView?.postDelayed({
            hostActivity.moveTaskToBack(true)
        }, 180L)
    }
    var croppedReview by remember(review) { mutableStateOf(review) }
    var cropRevision by remember(review) { mutableIntStateOf(0) }
    val bitmapKey = "review-$cropRevision"
    val bitmap = rememberScreenBitmaps(listOf(ScreenEntity(bitmapKey, croppedReview.flowId, croppedReview.path, 0f, 0f, 0, 0L)), maxDimension = 1600)[bitmapKey]
    var draggedMarker by remember { mutableStateOf<Offset?>(null) }
    var reviewCanvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    var showCropEditor by remember { mutableStateOf(false) }
    androidx.compose.runtime.DisposableEffect(showCropEditor, hostActivity) {
        if (!showCropEditor || hostActivity == null) return@DisposableEffect onDispose { }
        val controller = WindowCompat.getInsetsController(hostActivity.window, hostActivity.window.decorView)
        val previousLightStatusIcons = controller.isAppearanceLightStatusBars
        val previousLightNavigationIcons = controller.isAppearanceLightNavigationBars
        // This controls only system-bar presentation; it never changes the
        // activity's decor/inset policy or the Review Capture layout.
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        // The crop editor is a full-page workspace, so hide the OEM-controlled
        // status-bar surface instead of trying to recolor it. This removes the
        // remaining white band while preserving the host layout underneath.
        controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.statusBars())
        onDispose {
            controller.show(WindowInsetsCompat.Type.statusBars())
            controller.isAppearanceLightStatusBars = previousLightStatusIcons
            controller.isAppearanceLightNavigationBars = previousLightNavigationIcons
        }
    }
    // A user can reposition the marker on this review screen. Persist that
    // final normalized location, rather than the original overlay location.
    val reviewedCapture = draggedMarker?.let { marker ->
        if (reviewCanvasSize.width > 0 && reviewCanvasSize.height > 0) {
            val bounds = bitmap?.let {
                fittedScreenshotBounds(reviewCanvasSize.width.toFloat(), reviewCanvasSize.height.toFloat(), it.width, it.height)
            } ?: ScreenshotBounds(0f, 0f, reviewCanvasSize.width.toFloat(), reviewCanvasSize.height.toFloat())
            val (markerX, markerY) = bounds.normalize(marker.x, marker.y)
            croppedReview.copy(
                markerX = markerX,
                markerY = markerY
            )
        } else croppedReview
    } ?: croppedReview
    // Keep the review actions above Android's gesture/navigation area.
    val primaryBlue = UiColor.primary
    val pageBackground = UiColor.background
    val markerHalo = UiColor.extra.onDarkSurface
    val markerInk = UiColor.extra.marker
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().background(pageBackground).windowInsetsPadding(WindowInsets.systemBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = UiColor.surface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Review capture", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = UiColor.onSurface)
                    Text("Drag the marker to set the next step", fontSize = 14.sp, color = UiColor.onSurfaceVariant)
                }
                OutlinedButton(
                    onClick = { showCropEditor = true }, enabled = bitmap != null,
                    shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, primaryBlue),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) { Text("Crop", color = primaryBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp, vertical = 18.dp), contentAlignment = Alignment.Center) {
            Surface(
                // Keep the capture, marker, and connector together in the same
                // review canvas; only its surrounding presentation has changed.
                modifier = Modifier.fillMaxSize(),
                color = UiColor.surface,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 10.dp
            ) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(1f).padding(12.dp)) {
                        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp)).background(UiColor.surfaceVariant)) {
                            if (bitmap != null) androidx.compose.foundation.Image(bitmap, "Captured screen", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                            Canvas(Modifier.fillMaxSize().onSizeChanged { reviewCanvasSize = it }.pointerInput(bitmap) { detectDragGestures(onDragStart = { draggedMarker = it }) { change, amount ->
                                change.consume()
                                val bounds = bitmap?.let { fittedScreenshotBounds(size.width.toFloat(), size.height.toFloat(), it.width, it.height) }
                                    ?: ScreenshotBounds(0f, 0f, size.width.toFloat(), size.height.toFloat())
                                val moved = (draggedMarker ?: change.position) + amount
                                draggedMarker = Offset(
                                    moved.x.coerceIn(bounds.left, bounds.left + bounds.width),
                                    moved.y.coerceIn(bounds.top, bounds.top + bounds.height)
                                )
                            } }) {
                                val bounds = bitmap?.let { fittedScreenshotBounds(size.width, size.height, it.width, it.height) }
                                    ?: ScreenshotBounds(0f, 0f, size.width, size.height)
                                val (markerX, markerY) = bounds.pointFor(croppedReview.markerX, croppedReview.markerY)
                                val marker = draggedMarker ?: Offset(markerX, markerY)
                                // Keep the transition marker deliberately minimal: no label or colored halo.
                                drawCircle(markerHalo, 26f, marker)
                                drawCircle(markerInk, 9f, marker)
                                drawLine(markerInk, marker, Offset(size.width, marker.y), 6f)
                            }
                        }
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = UiColor.surface,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            shadowElevation = 12.dp
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
                if (guideStage == "capture" || guideStage == "finish") {
                    GuideHint(
                        if (guideStage == "capture") "3 of 4" else "4 of 4",
                        if (guideStage == "capture") "Adjust the marker, then tap Keep + next to capture another screen."
                        else "Review your second capture, then tap Finish to complete your flow."
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                OutlinedButton(
                    onClick = { ScreenshotStorage.delete(context, review.path); returnToCapturedApp() },
                    modifier = Modifier.weight(.75f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, UiColor.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = UiColor.error)
                ) { Text("Discard", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                Button(
                    onClick = {
                        vm.keepCapture(reviewedCapture) { saved ->
                            if (saved) {
                                if (guideStage == "capture") {
                                    guidePreferences.edit { putString("capture_guide_stage", "finish") }
                                    android.widget.Toast.makeText(context, "Move the bubble to the next step and tap it to capture again", android.widget.Toast.LENGTH_LONG).show()
                                }
                                returnToCapturedApp()
                            }
                            else android.widget.Toast.makeText(context, "This diagram was deleted; the capture was discarded", android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.weight(1.25f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = UiColor.onPrimary)
                ) { Text("Keep + next", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                Button(
                    onClick = {
                        context.startService(Intent(context, CaptureService::class.java).setAction(CaptureService.ACTION_STOP))
                        vm.keepCaptureAndOpenDiagram(reviewedCapture) { savedFlow ->
                            if (savedFlow != null && guideStage in listOf("capture", "finish")) {
                                guidePreferences.edit { remove("capture_guide_stage") }
                            }
                            onFinish(savedFlow)
                        }
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UiColor.secondary, contentColor = UiColor.onPrimary)
                ) { Text("Finish", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                }
            }
        }
        }
        if (showCropEditor && bitmap != null) {
        ReviewCaptureCropOverlay(
            bitmap = bitmap,
            onCancel = { showCropEditor = false },
            onSave = { crop ->
                vm.cropCapture(croppedReview, crop) { saved ->
                    if (saved) {
                        val normalized = crop.valid()
                        croppedReview = croppedReview.copy(
                            markerX = ((croppedReview.markerX - normalized.left) / (normalized.right - normalized.left)).coerceIn(0f, 1f),
                            markerY = ((croppedReview.markerY - normalized.top) / (normalized.bottom - normalized.top)).coerceIn(0f, 1f)
                        )
                        draggedMarker = null
                        cropRevision++
                    }
                    showCropEditor = false
                    android.widget.Toast.makeText(
                        context,
                        if (saved) "Crop applied" else "Couldn't crop this capture",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        }
    }
}

/** A full-page in-screen crop editor: no platform dialog or secondary window. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ReviewCaptureCropOverlay(
    bitmap: androidx.compose.ui.graphics.ImageBitmap,
    onCancel: () -> Unit,
    onSave: (CaptureCrop) -> Unit
) {
    var crop by remember(bitmap) { mutableStateOf(CaptureCrop.Full) }
    var previousCrop by remember(bitmap) { mutableStateOf<CaptureCrop?>(null) }
    var draggingCorner by remember { mutableStateOf<Int?>(null) }
    val latestCrop by rememberUpdatedState(crop)
    val previewInset = 16.dp
    val cropPage = UiColor.extra.statusBar
    val cropSurface = UiColor.extra.darkSurfaceVariant
    val cropOutline = UiColor.extra.darkOutline
    val cropOnSurface = UiColor.extra.onDarkSurface
    val cropMuted = UiColor.extra.darkOnSurfaceMuted
    val accent = UiColor.extra.onDarkSurface
    val cropDim = UiColor.scrim.copy(alpha = .58f)
    val cropGrid = cropOnSurface.copy(alpha = .60f)
    val applyContainer = UiColor.primary
    val applyContent = UiColor.onPrimary
    val overlayInteractionSource = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    var edgeHapticMask by remember { mutableIntStateOf(0) }

    Box(
            Modifier
                .fillMaxSize()
                .background(cropPage)
                .clickable(interactionSource = overlayInteractionSource, indication = null, onClick = {}),
            contentAlignment = Alignment.Center
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    // Reserve the system-bar area while the crop editor owns
                    // the full-screen layout.
                    .windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Crop capture", color = cropOnSurface, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text("Drag a corner to choose what to keep", color = cropMuted, fontSize = 14.sp)
                    }
                    IconButton(onClick = onCancel, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close crop editor", tint = cropOnSurface)
                    }
                }
                Box(
                    Modifier.fillMaxWidth().weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(cropSurface)
                        .border(1.dp, cropOutline, RoundedCornerShape(20.dp))
                ) {
                    androidx.compose.foundation.Image(
                        bitmap,
                        "Crop preview",
                        Modifier.fillMaxSize().padding(previewInset),
                        contentScale = ContentScale.Fit
                    )
                    Canvas(
                        Modifier.fillMaxSize().pointerInput(bitmap) {
                            detectDragGestures(
                                onDragStart = { start ->
                                    val rect = fittedScreenshotRectInPadding(size.width.toFloat(), size.height.toFloat(), bitmap.width, bitmap.height, previewInset.toPx())
                                    val c = latestCrop.valid()
                                    val corners = listOf(
                                        Offset(rect.left + rect.width * c.left, rect.top + rect.height * c.top),
                                        Offset(rect.left + rect.width * c.right, rect.top + rect.height * c.top),
                                        Offset(rect.left + rect.width * c.right, rect.top + rect.height * c.bottom),
                                        Offset(rect.left + rect.width * c.left, rect.top + rect.height * c.bottom)
                                    )
                                    val closest = corners.indices.minByOrNull { (corners[it] - start).getDistance() }
                                    draggingCorner = closest?.takeIf { (corners[it] - start).getDistance() <= 42.dp.toPx() }
                                    if (draggingCorner != null) {
                                        previousCrop = c
                                        edgeHapticMask = 0
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                },
                                onDragEnd = { draggingCorner = null; edgeHapticMask = 0 },
                                onDragCancel = { draggingCorner = null; edgeHapticMask = 0 }
                            ) { change, _ ->
                                val corner = draggingCorner ?: return@detectDragGestures
                                change.consume()
                                val rect = fittedScreenshotRectInPadding(size.width.toFloat(), size.height.toFloat(), bitmap.width, bitmap.height, previewInset.toPx())
                                val x = ((change.position.x - rect.left) / rect.width).coerceIn(0f, 1f)
                                val y = ((change.position.y - rect.top) / rect.height).coerceIn(0f, 1f)
                                val current = latestCrop
                                val updated = when (corner) {
                                    0 -> current.copy(left = x, top = y)
                                    1 -> current.copy(right = x, top = y)
                                    2 -> current.copy(right = x, bottom = y)
                                    else -> current.copy(left = x, bottom = y)
                                }.valid()
                                crop = updated
                                val edgeMask = when (corner) {
                                    0 -> (if (updated.left <= .001f) 1 else 0) or (if (updated.top <= .001f) 2 else 0)
                                    1 -> (if (updated.right >= .999f) 1 else 0) or (if (updated.top <= .001f) 2 else 0)
                                    2 -> (if (updated.right >= .999f) 1 else 0) or (if (updated.bottom >= .999f) 2 else 0)
                                    else -> (if (updated.left <= .001f) 1 else 0) or (if (updated.bottom >= .999f) 2 else 0)
                                }
                                if (edgeMask != 0 && edgeMask != edgeHapticMask) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                edgeHapticMask = edgeMask
                            }
                        }
                    ) {
                        val rect = fittedScreenshotRectInPadding(size.width, size.height, bitmap.width, bitmap.height, previewInset.toPx())
                        val c = crop.valid()
                        val left = rect.left + rect.width * c.left
                        val top = rect.top + rect.height * c.top
                        val right = rect.left + rect.width * c.right
                        val bottom = rect.top + rect.height * c.bottom
                        drawRect(cropDim, topLeft = Offset(rect.left, rect.top), size = androidx.compose.ui.geometry.Size(rect.width, top - rect.top))
                        drawRect(cropDim, topLeft = Offset(rect.left, bottom), size = androidx.compose.ui.geometry.Size(rect.width, rect.top + rect.height - bottom))
                        drawRect(cropDim, topLeft = Offset(rect.left, top), size = androidx.compose.ui.geometry.Size(left - rect.left, bottom - top))
                        drawRect(cropDim, topLeft = Offset(right, top), size = androidx.compose.ui.geometry.Size(rect.left + rect.width - right, bottom - top))
                        val handleInset = 6.dp.toPx()
                        drawRect(
                            accent.copy(alpha = .95f),
                            topLeft = Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                            style = Stroke(1.5.dp.toPx())
                        )
                        // Fixed rule-of-thirds guides make it easier to align
                        // the crop with screen edges and visible UI elements.
                        val gridColor = cropGrid
                        val gridStroke = 1.dp.toPx()
                        for (fraction in listOf(1f / 3f, 2f / 3f)) {
                            val x = left + (right - left) * fraction
                            val y = top + (bottom - top) * fraction
                            drawLine(gridColor, Offset(x, top), Offset(x, bottom), gridStroke)
                            drawLine(gridColor, Offset(left, y), Offset(right, y), gridStroke)
                        }
                        listOf(Offset(left, top), Offset(right, top), Offset(right, bottom), Offset(left, bottom)).forEach { point ->
                            // A full-image crop has its border on the edge. Keep the visual handle
                            // inside the canvas so it remains complete and easy to grab.
                            val handle = Offset(
                                point.x.coerceIn(handleInset, size.width - handleInset),
                                point.y.coerceIn(handleInset, size.height - handleInset)
                            )
                            drawCircle(accent, 6.dp.toPx(), handle)
                            drawCircle(cropPage, 2.5.dp.toPx(), handle)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { previousCrop?.let { crop = it } },
                        enabled = previousCrop != null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            cropOnSurface.copy(alpha = if (previousCrop != null) .68f else .28f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = cropOnSurface,
                            disabledContentColor = cropOnSurface.copy(alpha = .42f),
                            disabledContainerColor = UiColor.transparent
                        )
                    ) { Text("Undo", fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = { onSave(crop.valid()) },
                        modifier = Modifier.weight(1.35f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = applyContainer,
                            contentColor = applyContent
                        )
                    ) { Text("Apply crop", fontWeight = FontWeight.Bold) }
                }
            }
        }
}

@Composable private fun DiagramScreen(
    flowId: String,
    vm: FlowViewModel,
    goBack: () -> Unit,
    onLightboxVisibilityChange: (Boolean) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tipPreferences = remember { context.getSharedPreferences("flowsnap", Context.MODE_PRIVATE) }
    val projectionManager = remember { context.getSystemService(MediaProjectionManager::class.java) }
    val currentFlow by vm.flow(flowId).collectAsStateWithLifecycle(initialValue = null)
    val screens by vm.screens(flowId).collectAsStateWithLifecycle(initialValue = emptyList())
    val transitions by vm.transitions(flowId).collectAsStateWithLifecycle(initialValue = emptyList())
    // Room emits the supplied null initial value before its first query result.
    // Navigating Home from that transient state made recent-diagram taps flash.
    if (currentFlow == null) {
        Box(Modifier.fillMaxSize().background(UiColor.background))
        return
    }
    val flow = currentFlow!!
    var pendingResult by remember { mutableStateOf<androidx.activity.result.ActivityResult?>(null) }
    var showTips by remember { mutableStateOf(false) }
    var captureLaunchInProgress by remember { mutableStateOf(false) }
    var resumeCaptureAfterOverlayGrant by rememberSaveable { mutableStateOf(false) }
    var launchCaptureAfterNotificationRequest by rememberSaveable { mutableStateOf(false) }
    var exportJpegAfterPermission by rememberSaveable { mutableStateOf(false) }
    var exportPngAfterPermission by rememberSaveable { mutableStateOf(false) }
    var exportPdfAfterPermission by rememberSaveable { mutableStateOf(false) }
    var exportSketchAfterPermission by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showDiagramSettings by remember { mutableStateOf(false) }
    var showExportFormats by remember { mutableStateOf(false) }
    var showShareGuide by remember { mutableStateOf(false) }
    var activeExportKind by remember { mutableStateOf<ExportProgressKind?>(null) }
    var replacementTarget by remember { mutableStateOf<ScreenEntity?>(null) }
    val replaceImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val target = replacementTarget
        replacementTarget = null
        if (uri != null && target != null) {
            vm.replaceScreenImage(target, uri) { replaced ->
                android.widget.Toast.makeText(context, if (replaced) "Screenshot replaced" else "Unable to replace screenshot", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    val startShareExport: () -> Unit = {
        activeExportKind = ExportProgressKind.PDF_AND_SVG
        vm.exportForSharing(screens, transitions, flow) { result ->
            activeExportKind = null
            result.onSuccess { files ->
                val clipData = ClipData.newUri(context.contentResolver, "FlowSnap PDF", files.first())
                files.drop(1).forEach { clipData.addItem(ClipData.Item(it)) }
                val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "application/octet-stream"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
                    this.clipData = clipData
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share FlowSnap PDF and SVG"))
            }.onFailure { error ->
                android.util.Log.e("FlowSnapShare", "Share export failed", error)
                android.widget.Toast.makeText(context, "Could not create PDF and SVG: ${error.message ?: "unknown error"}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    val startJpegExport = {
        activeExportKind = ExportProgressKind.JPEG
        vm.exportJpeg(screens, transitions, flow) { result ->
            activeExportKind = null
            result.onSuccess {
                android.widget.Toast.makeText(context, "JPEG saved to Pictures/FlowSnap", android.widget.Toast.LENGTH_LONG).show()
            }.onFailure { error ->
                android.util.Log.e("FlowSnapJpeg", "JPEG export failed", error)
                android.widget.Toast.makeText(context, "Could not save JPEG: ${error.message ?: "unknown error"}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    val jpegStoragePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) exportJpegAfterPermission = true
        else android.widget.Toast.makeText(context, "Storage permission is required to save JPEG files", android.widget.Toast.LENGTH_LONG).show()
    }
    val startPngExport = {
        activeExportKind = ExportProgressKind.PNG
        vm.exportPng(screens, transitions, flow) { result ->
            activeExportKind = null
            result.onSuccess {
                android.widget.Toast.makeText(context, "PNG saved to Pictures/FlowSnap", android.widget.Toast.LENGTH_LONG).show()
            }.onFailure { error ->
                android.util.Log.e("FlowSnapPng", "PNG export failed", error)
                android.widget.Toast.makeText(context, "Could not save PNG: ${error.message ?: "unknown error"}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    val pngStoragePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) exportPngAfterPermission = true
        else android.widget.Toast.makeText(context, "Storage permission is required to save PNG files", android.widget.Toast.LENGTH_LONG).show()
    }
    val startPdfExport = {
        activeExportKind = ExportProgressKind.PDF
        vm.exportPdf(screens, transitions, flow) { result ->
            activeExportKind = null
            result.onSuccess {
                android.widget.Toast.makeText(context, "PDF saved to Downloads/FlowSnap", android.widget.Toast.LENGTH_LONG).show()
            }.onFailure { error ->
                android.util.Log.e("FlowSnapPdf", "PDF export failed", error)
                android.widget.Toast.makeText(context, "Could not save PDF: ${error.message ?: "unknown error"}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    val pdfStoragePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) exportPdfAfterPermission = true
        else android.widget.Toast.makeText(context, "Storage permission is required to save PDF files", android.widget.Toast.LENGTH_LONG).show()
    }
    val startSketchExport = {
        activeExportKind = ExportProgressKind.SVG
        vm.exportSvg(screens, transitions, flow) { result ->
            activeExportKind = null
            result.onSuccess {
                android.widget.Toast.makeText(context, "SVG saved. Open in Figma.", android.widget.Toast.LENGTH_LONG).show()
            }.onFailure { error ->
                android.util.Log.e("FlowSnapSvg", "SVG export failed", error)
                android.widget.Toast.makeText(context, "Could not save SVG: ${error.message ?: "unknown error"}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
    val sketchStoragePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) exportSketchAfterPermission = true
        else android.widget.Toast.makeText(context, "Storage permission is required to save SVG files", android.widget.Toast.LENGTH_LONG).show()
    }
    androidx.compose.runtime.LaunchedEffect(exportJpegAfterPermission) {
        if (exportJpegAfterPermission) {
            exportJpegAfterPermission = false
            startJpegExport()
        }
    }
    androidx.compose.runtime.LaunchedEffect(exportPngAfterPermission) {
        if (exportPngAfterPermission) {
            exportPngAfterPermission = false
            startPngExport()
        }
    }
    androidx.compose.runtime.LaunchedEffect(exportPdfAfterPermission) {
        if (exportPdfAfterPermission) {
            exportPdfAfterPermission = false
            startPdfExport()
        }
    }
    androidx.compose.runtime.LaunchedEffect(exportSketchAfterPermission) {
        if (exportSketchAfterPermission) {
            exportSketchAfterPermission = false
            startSketchExport()
        }
    }
    val captureConsent = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        captureLaunchInProgress = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            pendingResult = result
            if (tipPreferences.getBoolean("hide_capture_tip", false)) ContextCompat.startForegroundService(context, Intent(context, CaptureService::class.java).putExtra(CaptureService.EXTRA_FLOW_ID, flow.id).putExtra(CaptureService.EXTRA_RESULT_CODE, result.resultCode).putExtra(CaptureService.EXTRA_RESULT_DATA, result.data))
            else showTips = true
        } else {
            android.widget.Toast.makeText(context, "Screen capture permission was not granted", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    val launchProjection = {
        captureLaunchInProgress = true
        captureConsent.launch(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                projectionManager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())
            } else projectionManager.createScreenCaptureIntent()
        )
    }
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Foreground capture remains available when the user declines; Android still requires the service notification.
        launchCaptureAfterNotificationRequest = true
    }
    androidx.compose.runtime.LaunchedEffect(launchCaptureAfterNotificationRequest) {
        if (launchCaptureAfterNotificationRequest) {
            launchCaptureAfterNotificationRequest = false
            launchProjection()
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && resumeCaptureAfterOverlayGrant) {
                resumeCaptureAfterOverlayGrant = false
                if (Settings.canDrawOverlays(context)) {
                    launchProjection()
                } else {
                    android.widget.Toast.makeText(context, "Display over other apps permission is required to capture", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val beginCapture = {
        when {
            captureLaunchInProgress -> Unit
            !Settings.canDrawOverlays(context) -> {
                // Do not launch projection consent until the user has returned
                // from overlay permission settings. Launching both at once can
                // leave the activity looking frozen on some devices.
                resumeCaptureAfterOverlayGrant = true
                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri()))
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED -> notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            else -> launchProjection()
        }
    }
    val hasScreens = screens.isNotEmpty()
    val showCaptureGuide = remember(flowId) {
        context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
            .getString("capture_guide_stage", "") == "capture"
    }
    // The bottom controls must stay inside the safe area on both gesture and
    // three-button navigation devices.
    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars).diagramGrid()) {
        // The diagram is one tall document: screenshots flow downward in five
        // columns and this area scrolls independently of the fixed controls.
        // The padding keeps document content clear of those controls.
        EditorDrawableCircle(R.drawable.ic_flow_home, "Home", goBack, modifier = Modifier.align(Alignment.TopStart).padding(24.dp))
        Row(
            modifier = Modifier.align(Alignment.TopEnd).padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EditorDrawableCircle(R.drawable.ic_flow_settings, "Settings", onClick = { showDiagramSettings = true }, enabled = hasScreens)
        }
        if (screens.isEmpty()) Text("Create a diagram with the + option below", Modifier.align(Alignment.Center), color = UiColor.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        else {
            DiagramPreview(
                screens = screens,
                transitions = transitions,
                flow = flow,
                onDeleteScreen = { screen -> vm.deleteScreen(screen) { android.widget.Toast.makeText(context, "Screenshot deleted", android.widget.Toast.LENGTH_SHORT).show() } },
                onReplaceScreen = { screen ->
                    replacementTarget = screen
                    replaceImage.launch("image/*")
                },
                onUpdatePointer = { screen, markerX, markerY ->
                    vm.updateScreenPointer(screen, markerX, markerY) {
                        android.widget.Toast.makeText(context, "Pointer updated", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                onLightboxVisibilityChange = onLightboxVisibilityChange,
                // The white card itself stays in this protected viewport. Only
                // its document content scrolls, so its rounded top and bottom
                // corners remain visible at every scroll position.
                modifier = Modifier.align(Alignment.Center)
                    .padding(vertical = 132.dp)
                    .fillMaxWidth(.90f)
                    .fillMaxHeight()
            )
        }
        Row(Modifier.align(Alignment.BottomStart).padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            EditorDrawableCircle(R.drawable.ic_flow_save, "Save", onClick = {
                showExportFormats = true
            }, enabled = hasScreens)
            EditorDrawableCircle(
                iconRes = R.drawable.ic_google_drive,
                label = "Share PDF and SVG with Google Drive",
                onClick = { showShareGuide = true },
                enabled = hasScreens,
                iconTint = Color.Unspecified
            )
            EditorDrawableCircle(R.drawable.ic_flow_delete, "Delete diagram", onClick = { showDeleteConfirmation = true })
        }
        Column(Modifier.align(Alignment.BottomEnd).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.End) {
            EditorDrawableCircle(R.drawable.ic_flow_add, "Capture", beginCapture, large = true, enabled = !captureLaunchInProgress)
        }
        if (showCaptureGuide) {
            GuideHint(
                "2 of 4",
                "Tap + below to start capturing. Allow the requested permissions, then drag and tap the floating bubble to take a screenshot.",
                Modifier.align(Alignment.BottomCenter).padding(start = 24.dp, end = 24.dp, bottom = 100.dp)
            )
        }
    }
    if (showDiagramSettings) {
        DiagramSettingsDialog(
            flow = flow,
            onDismiss = { showDiagramSettings = false },
            onSave = { title, version, createdBy, showTitle, showVersion, showCreatedAt, showCreatedBy, showAppVersion, showMobileStencils, connectorColor, preferredColumns ->
                    vm.updateFlowMetadata(flow, title, version, createdBy, showTitle, showVersion, showCreatedAt, showCreatedBy, showAppVersion) { savedFlow ->
                    vm.updateDiagramAppearance(savedFlow, showMobileStencils, connectorColor, preferredColumns) { showDiagramSettings = false }
                }
            }
        )
    }
    if (showExportFormats) {
        ExportFormatDialog(
            onDismiss = { showExportFormats = false },
            onExportPdf = {
                showExportFormats = false
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    pdfStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else startPdfExport()
            },
            onExportJpeg = {
                showExportFormats = false
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    jpegStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else startJpegExport()
            },
            onExportPng = {
                showExportFormats = false
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    pngStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else startPngExport()
            },
            onExportSketch = {
                showExportFormats = false
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    sketchStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else startSketchExport()
            },
            onUnavailable = { format ->
                showExportFormats = false
                android.widget.Toast.makeText(context, "$format export is not available yet", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
    if (showShareGuide) {
        ShareToDriveGuideDialog(
            onDismiss = { showShareGuide = false },
            onContinue = {
                showShareGuide = false
                startShareExport()
            }
        )
    }
    activeExportKind?.let { kind -> ExportProgressDialog(kind) }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = UiColor.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = UiColor.extra.destructiveContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("!", color = UiColor.error, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Delete diagram?", color = UiColor.onSurface, fontWeight = FontWeight.SemiBold)
                }
            },
            text = {
                Text(
                    "Your diagram will be permanently deleted. Are you sure you want to do this?",
                    color = UiColor.onSurface, fontSize = 17.sp
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = UiColor.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.deleteFlow(flow) {
                        showDeleteConfirmation = false
                        goBack()
                    }
                }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = UiColor.error, contentColor = UiColor.onError)) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
    if (showTips) CaptureTipDialog(onConfirm = { dontShowAgain -> if (dontShowAgain) tipPreferences.edit { putBoolean("hide_capture_tip", true) }; val result = pendingResult!!; ContextCompat.startForegroundService(context, Intent(context, CaptureService::class.java).putExtra(CaptureService.EXTRA_FLOW_ID, flowId).putExtra(CaptureService.EXTRA_RESULT_CODE, result.resultCode).putExtra(CaptureService.EXTRA_RESULT_DATA, result.data)); showTips = false })
}

@Composable private fun CaptureTipDialog(onConfirm: (Boolean) -> Unit) {
    val primaryBlue = UiColor.primary
    var dontShowAgain by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = { }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(.88f),
            color = UiColor.surface,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 16.dp
        ) {
            Column(Modifier.padding(22.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(46.dp).background(UiColor.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = primaryBlue, modifier = Modifier.size(25.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Ready to capture?", color = UiColor.onSurface, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                        Text("A quick guide to the capture bubble", color = UiColor.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    "FlowSnap will show a floating bubble above the app you are documenting.",
                    color = UiColor.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(16.dp))
                DragTapIllustration(primaryBlue)
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { dontShowAgain = !dontShowAgain }.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it },
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(checkedColor = primaryBlue, uncheckedColor = UiColor.onSurfaceVariant, checkmarkColor = UiColor.onPrimary)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Don't show this guide again", color = UiColor.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = { onConfirm(dontShowAgain) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = UiColor.onPrimary)
                ) { Text("Got it", fontWeight = FontWeight.Bold, letterSpacing = .2.sp) }
            }
        }
    }
}

@Composable
private fun ShareToDriveGuideDialog(onDismiss: () -> Unit, onContinue: () -> Unit) {
    val primaryBlue = UiColor.primary
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(.88f),
            color = UiColor.surface,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 18.dp
        ) {
            Column(Modifier.padding(26.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = UiColor.primaryContainer
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_google_drive),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.padding(11.dp)
                        )
                    }
                    Spacer(Modifier.width(15.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Save to Google Drive", color = UiColor.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(2.dp))
                        Text("PDF + SVG export", color = primaryBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(22.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = UiColor.surfaceVariant
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                        DriveGuideStep("1", "FlowSnap creates your PDF and SVG")
                        DriveGuideStep("2", "Choose Google Drive in the share menu")
                        DriveGuideStep("3", "Select an account and folder, then upload")
                    }
                }
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = UiColor.onPrimary),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Continue to share", fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Not now", color = UiColor.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DriveGuideStep(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(25.dp), shape = CircleShape, color = UiColor.primary) {
            Box(contentAlignment = Alignment.Center) {
            Text(number, color = UiColor.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(text, color = UiColor.onSurface, fontSize = 14.sp, lineHeight = 19.sp)
    }
}

private data class ExportProgressDetails(
    val iconRes: Int,
    val keepIconColors: Boolean,
    val title: String,
    val description: String,
    val usePdfIcon: Boolean = false
)

@Composable
private fun ExportProgressDialog(kind: ExportProgressKind) {
    val primaryBlue = UiColor.primary
    val details = when (kind) {
        ExportProgressKind.JPEG -> ExportProgressDetails(
            iconRes = R.drawable.ic_flow_save,
            keepIconColors = false,
            title = "Saving JPEG image",
            description = "Creating a high-quality image of your diagram."
        )
        ExportProgressKind.PNG -> ExportProgressDetails(
            iconRes = R.drawable.ic_flow_save,
            keepIconColors = false,
            title = "Saving PNG image",
            description = "Creating a portable PNG image of your diagram."
        )
        ExportProgressKind.PDF -> ExportProgressDetails(
            iconRes = R.drawable.ic_flow_save,
            keepIconColors = false,
            usePdfIcon = true,
            title = "Saving PDF document",
            description = "Creating a printable PDF version of your diagram."
        )
        ExportProgressKind.PDF_AND_SVG -> ExportProgressDetails(
            iconRes = R.drawable.ic_google_drive,
            keepIconColors = true,
            title = "Preparing PDF and SVG",
            description = "Creating files for Google Drive."
        )
        ExportProgressKind.SVG -> ExportProgressDetails(
            iconRes = R.drawable.ic_flow_save,
            keepIconColors = false,
            title = "Saving SVG diagram",
            description = "Creating an editable vector version of your diagram."
        )
    }
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(.88f),
            color = UiColor.surface,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 18.dp
        ) {
            Column(Modifier.padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = UiColor.primaryContainer
                ) {
                    if (details.usePdfIcon) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = primaryBlue,
                            modifier = Modifier.padding(13.dp)
                        )
                    } else {
                        Icon(
                            painterResource(details.iconRes),
                            contentDescription = null,
                            tint = if (details.keepIconColors) Color.Unspecified else primaryBlue,
                            modifier = Modifier.padding(13.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(details.title, color = UiColor.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Text(
                    details.description,
                    color = UiColor.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(22.dp))
                CircularProgressIndicator(modifier = Modifier.size(48.dp), strokeWidth = 4.dp, color = primaryBlue)

            }
        }
    }
}

/** Export chooser shown by the bottom-left floppy-disk control. */
@Composable private fun ExportFormatDialog(
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onExportJpeg: () -> Unit,
    onExportPng: () -> Unit,
    onExportSketch: () -> Unit,
    onUnavailable: (String) -> Unit
) {
    val primaryBlue = UiColor.primary
    var selected by remember { mutableStateOf("JPEG") }
    val formats = listOf(
        "JPEG" to "High-quality image for sharing and viewing.",
        "PNG" to "Portable Network Graphics image",
        "PDF" to "Portable Document Format",
        "SVG / FIGMA (.svg)" to "Editable SVG for Figma."
    )
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(.88f),
            color = UiColor.surface,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 14.dp
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 26.dp)) {
                Text(
                    "Export diagram",
                    color = UiColor.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Choose a format for your flow",
                    color = UiColor.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(18.dp))
                formats.forEach { (format, description) ->
                    val isSelected = selected == format
                    val rowShape = RoundedCornerShape(16.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(rowShape)
                            .background(if (isSelected) UiColor.primary.copy(alpha = .05f) else UiColor.transparent)
                            .clickable { selected = format }
                            .padding(horizontal = 12.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExportFormatRadio(selected = isSelected, primaryBlue = primaryBlue)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(format, color = UiColor.onSurface, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            if (isSelected) {
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    description,
                                    color = UiColor.onSurfaceVariant,
                        fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = primaryBlue, fontWeight = FontWeight.SemiBold) }
                    Spacer(Modifier.width(14.dp))
                    Button(
                        onClick = {
                        when (selected) {
                            "JPEG" -> onExportJpeg()
                            "PNG" -> onExportPng()
                            "PDF" -> onExportPdf()
                            "SVG / FIGMA (.svg)" -> onExportSketch()
                            else -> onUnavailable(selected)
                        }
                    },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = UiColor.onPrimary),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
                    ) { Text("Save as", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun ExportFormatRadio(selected: Boolean, primaryBlue: Color) {
    Box(
        modifier = Modifier.size(28.dp)
            .background(if (selected) UiColor.primary.copy(alpha = .10f) else UiColor.transparent, CircleShape)
            .border(2.dp, if (selected) primaryBlue else UiColor.outline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) Box(Modifier.size(12.dp).background(primaryBlue, CircleShape))
    }
}

/** Diagram label and appearance settings, opened by the editor gear button. */
@Composable private fun DiagramSettingsDialog(
    flow: FlowEntity,
    onDismiss: () -> Unit,
    onSave: (title: String, version: String, createdBy: String, showTitle: Boolean, showVersion: Boolean, showCreatedAt: Boolean, showCreatedBy: Boolean, showAppVersion: Boolean, showMobileStencils: Boolean, connectorColor: Long, preferredColumns: Int) -> Unit
) {
    val hostActivity = LocalActivity.current
    // Settings is a focused modal surface. Hiding the system status bar stops
    // its clock/network icons from showing through the dimmed workspace.
    androidx.compose.runtime.DisposableEffect(hostActivity) {
        if (hostActivity == null) return@DisposableEffect onDispose { }
        val controller = WindowCompat.getInsetsController(hostActivity.window, hostActivity.window.decorView)
        val previousLightStatusIcons = controller.isAppearanceLightStatusBars
        controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.statusBars())
        onDispose {
            controller.show(WindowInsetsCompat.Type.statusBars())
            controller.isAppearanceLightStatusBars = previousLightStatusIcons
        }
    }
    var title by remember(flow.id) { mutableStateOf(flow.name) }
    var version by remember(flow.id) { mutableStateOf(flow.diagramVersion) }
    var createdBy by remember(flow.id) { mutableStateOf(flow.createdBy) }
    var showTitle by remember(flow.id) { mutableStateOf(flow.showTitle) }
    var showVersion by remember(flow.id) { mutableStateOf(flow.showVersion) }
    var showCreatedAt by remember(flow.id) { mutableStateOf(flow.showCreatedAt) }
    var showCreatedBy by remember(flow.id) { mutableStateOf(flow.showCreatedBy) }
    var showAppVersion by remember(flow.id) { mutableStateOf(flow.showAppVersion) }
    var showMobileStencils by remember(flow.id) { mutableStateOf(flow.showMobileStencils) }
    var connectorColor by remember(flow.id) { mutableLongStateOf(flow.connectorColor) }
    var preferredColumns by remember(flow.id) { mutableIntStateOf(flow.preferredColumns.coerceIn(1, 9)) }
    var appearancePage by remember(flow.id) { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        // Dim the editor beneath the modal so the settings panel is the clear focus.
        val dialogView = LocalView.current
        SideEffect {
            (dialogView.parent as? DialogWindowProvider)?.window?.setDimAmount(0.60f)
        }
        Box(Modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.Center) {
        Surface(
            // Wrap content on roomy screens, but cap the height so the
            // settings remain scrollable rather than clipping on compact ones.
            modifier = Modifier.fillMaxWidth(.90f).heightIn(max = 620.dp).offset(y = (-20).dp),
            color = UiColor.surface,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 12.dp
        ) {
            Column(
                Modifier
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Diagram settings", color = UiColor.onSurface, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().background(UiColor.surfaceVariant, RoundedCornerShape(12.dp)).padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DiagramSettingsTab("Labels", selected = !appearancePage, onClick = { appearancePage = false }, modifier = Modifier.weight(1f))
                    DiagramSettingsTab("Look & feel", selected = appearancePage, onClick = { appearancePage = true }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                AnimatedContent(
                    targetState = appearancePage,
                    modifier = Modifier.fillMaxWidth().pointerInput(appearancePage) {
                        var horizontalDrag = 0f
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount -> horizontalDrag += dragAmount },
                            onDragCancel = { horizontalDrag = 0f },
                            onDragEnd = {
                                when {
                                    horizontalDrag < -72f && !appearancePage -> appearancePage = true
                                    horizontalDrag > 72f && appearancePage -> appearancePage = false
                                }
                                horizontalDrag = 0f
                            }
                        )
                    },
                    transitionSpec = {
                        val direction = if (targetState) 1 else -1
                        slideInHorizontally { direction * it } togetherWith slideOutHorizontally { -direction * it }
                    },
                    label = "diagramSettingsSlide"
                ) { showAppearance ->
                    if (showAppearance) {
                        DiagramAppearancePage(
                            showMobileStencils = showMobileStencils,
                            onShowMobileStencilsChange = { showMobileStencils = it },
                            preferredColumns = preferredColumns,
                            onPreferredColumnsChange = { preferredColumns = it },
                            connectorColor = connectorColor,
                            onConnectorColorChange = { connectorColor = it }
                        )
                    } else {
                        Column(Modifier.fillMaxWidth()) {
                            EditableDiagramSetting("Title", title, { title = it }, showTitle) { showTitle = !showTitle }
                            EditableDiagramSetting("Diagram Version", version, { version = it }, showVersion) { showVersion = !showVersion }
                            ReadOnlyDiagramSetting(java.text.SimpleDateFormat("yyyy MMM dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(flow.createdAt)), showCreatedAt) { showCreatedAt = !showCreatedAt }
                            EditableDiagramSetting("Created By", createdBy, { createdBy = it }, showCreatedBy) { showCreatedBy = !showCreatedBy }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(end = 14.dp)) {
                        Text("Cancel", color = UiColor.onSurfaceVariant, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { onSave(title, version, createdBy, showTitle, showVersion, showCreatedAt, showCreatedBy, showAppVersion, showMobileStencils, connectorColor, preferredColumns) }) {
                        Text("Save", color = UiColor.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        }
    }
}

@Composable private fun DiagramSettingsTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.height(34.dp).clip(RoundedCornerShape(9.dp))
            .background(if (selected) UiColor.surface else UiColor.transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (selected) UiColor.primary else UiColor.onSurfaceVariant)
    }
}

@Composable private fun DiagramAppearancePage(
    showMobileStencils: Boolean,
    onShowMobileStencilsChange: (Boolean) -> Unit,
    preferredColumns: Int,
    onPreferredColumnsChange: (Int) -> Unit,
    connectorColor: Long,
    onConnectorColorChange: (Long) -> Unit
) {
    Column(
        Modifier.fillMaxWidth()
            .background(UiColor.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        AppearanceToggle(showMobileStencils, onShowMobileStencilsChange)
        LookAndFeelDivider()
        DiagramColumnsSetting(preferredColumns, onPreferredColumnsChange)
        LookAndFeelDivider()
        ConnectorColorSetting(connectorColor, onConnectorColorChange)
    }
}

@Composable private fun LookAndFeelDivider() {
    Box(
        Modifier.fillMaxWidth().padding(vertical = 9.dp)
            .height(1.dp).background(UiColor.outlineVariant)
    )
}

@Composable private fun DiagramColumnsSetting(columns: Int, onColumnsChange: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(36.dp)
                .background(UiColor.surface, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = "Diagram columns",
                tint = UiColor.onSurface,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Number of columns", color = UiColor.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DiagramColumnStepButton("−", enabled = columns > 1) { onColumnsChange(columns - 1) }
            Text(columns.toString(), modifier = Modifier.width(26.dp), textAlign = TextAlign.Center, color = UiColor.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            DiagramColumnStepButton("+", enabled = columns < 9) { onColumnsChange(columns + 1) }
        }
    }
}

@Composable private fun DiagramColumnStepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(30.dp).background(if (enabled) UiColor.surfaceVariant else UiColor.transparent, CircleShape)
            .clip(CircleShape).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (enabled) UiColor.onSurface else UiColor.onSurfaceVariant, fontSize = 20.sp, lineHeight = 20.sp)
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable private fun EditableDiagramSetting(label: String, value: String, onValueChange: (String) -> Unit, included: Boolean, toggleIncluded: () -> Unit) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    val keyboardBottom = WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current)
    androidx.compose.runtime.LaunchedEffect(focused, keyboardBottom) {
        if (focused) bringIntoViewRequester.bringIntoView()
    }
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, color = UiColor.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(3.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.weight(1f).height(44.dp)
                    .background(UiColor.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val primaryBlue = UiColor.primary
                CompositionLocalProvider(
                    LocalTextSelectionColors provides TextSelectionColors(
                        handleColor = primaryBlue,
                        backgroundColor = primaryBlue.copy(alpha = .28f)
                    )
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth().bringIntoViewRequester(bringIntoViewRequester).onFocusChanged { focused = it.isFocused },
                        singleLine = true,
                        cursorBrush = SolidColor(primaryBlue),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 15.sp,
                        color = UiColor.onSurface
                        )
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            IncludedMark(included, toggleIncluded)
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable private fun ReadOnlyDiagramSetting(value: String, included: Boolean, toggleIncluded: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text("Created At", fontSize = 13.sp, color = UiColor.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(3.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.weight(1f).height(44.dp)
                    .background(UiColor.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(value, fontSize = 15.sp, color = UiColor.onSurface, maxLines = 1)
            }
            Spacer(Modifier.width(10.dp))
            IncludedMark(included, toggleIncluded)
        }
    }
    Spacer(Modifier.height(4.dp))
}

/** Controls whether the transition path and the saved tap marker are visible. */
@Composable private fun AppearanceToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(36.dp)
                .background(UiColor.surface, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = "Mobile stencil",
                tint = UiColor.onSurface,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Show mobile stencils", color = UiColor.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        IncludedMark(checked) { onCheckedChange(!checked) }
    }
}

/** A compact preset picker keeps connector colors readable and consistent. */
@Composable private fun ConnectorColorSetting(selectedColor: Long, onColorSelected: (Long) -> Unit) {
    val colors = listOf(
        ConnectorPalette.Green to "Green",
        ConnectorPalette.Indigo to "Indigo",
        ConnectorPalette.Red to "Red",
        ConnectorPalette.Charcoal to "Charcoal",
        ConnectorPalette.Orange to "Orange",
        ConnectorPalette.Purple to "Purple",
        ConnectorPalette.Teal to "Teal",
        ConnectorPalette.Pink to "Pink"
    )
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).background(Color(selectedColor.toInt()), RoundedCornerShape(10.dp))
                    .border(1.dp, UiColor.onSurface.copy(alpha = .13f), RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Color of arrows", color = UiColor.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("#%06X".format(selectedColor and 0xFFFFFF), color = UiColor.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        // Eight compact swatches fit in one horizontal line, leaving room for
        // the dialog actions at the bottom on normal phone screens.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            colors.forEach { (color, _) ->
                val selected = color == selectedColor
                Box(
                    Modifier.size(27.dp)
                        .clip(CircleShape)
                        .background(Color(color.toInt()), CircleShape)
                        .border(if (selected) 2.dp else 1.dp, if (selected) UiColor.surface else UiColor.outline, CircleShape)
                        .clickable { onColorSelected(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) ConnectorSelectionMark(Color(color.toInt()), Modifier.size(17.dp))
                }
            }
        }
    }
}

@Composable private fun IncludedMark(included: Boolean, onClick: (() -> Unit)? = null) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (hovered) UiColor.onSurfaceVariant else UiColor.outlineVariant,
        animationSpec = tween(durationMillis = 150),
        label = "checkboxBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = when {
            pressed -> UiColor.primary.copy(alpha = .14f)
            included -> UiColor.surfaceVariant
            hovered -> UiColor.surfaceVariant
            else -> UiColor.surface
        },
        animationSpec = tween(durationMillis = 150),
        label = "checkboxBackground"
    )
    Box(
        Modifier.size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = onClick != null
            ) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        if (included) Icon(Icons.Default.Check, contentDescription = null, tint = UiColor.primary, modifier = Modifier.size(20.dp))
    }
}

@Composable private fun DragTapIllustration(primaryBlue: Color) = Surface(
    modifier = Modifier.fillMaxWidth(),
    color = UiColor.secondaryContainer,
    shape = RoundedCornerShape(14.dp)
) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CaptureTipStep(
            number = "1",
            title = "Drag the bubble",
            detail = "Place it over the spot you want to point to.",
            primaryBlue = primaryBlue
        )
        CaptureTipStep(
            number = "2",
            title = "Tap to capture",
            detail = "Take a screenshot when the bubble is in place.",
            primaryBlue = primaryBlue
        )
        CaptureTipStep(
            number = "3",
            title = "Return to FlowSnap",
            detail = "Tap the checkmark when you are finished.",
            primaryBlue = primaryBlue
        )
    }
}

@Composable private fun CaptureTipStep(number: String, title: String, detail: String, primaryBlue: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp).background(primaryBlue, CircleShape), contentAlignment = Alignment.Center) {
            Text(number, color = UiColor.onPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = UiColor.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = UiColor.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

@Composable private fun Modifier.diagramGrid(): Modifier {
    val workspace = UiColor.extra
    val surfaceVariant = UiColor.surfaceVariant
    val canvasBackground = workspace.canvasBackground
    val minorGrid = workspace.canvasMinorGrid
    val majorGrid = workspace.canvasMajorGrid
    return this.drawBehind {
    // Fine graph-paper grid: it gives the editor structure without competing
    // with the screenshots. Every fourth line is a gentle visual guide.
    if (canvasBackground == Color.Black) {
        drawRect(Color.Black)
    } else {
        drawRect(
            Brush.linearGradient(
                colors = listOf(surfaceVariant, canvasBackground),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
        )
    }
    val spacing = 54f
    var x = 0f
    var column = 0
    while (x <= size.width) {
        val isMajor = column % 4 == 0
        drawLine(
            color = if (isMajor) majorGrid else minorGrid,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = if (isMajor) 1.5f else 0.8f
        )
        x += spacing
        column++
    }
    var y = 0f
    var row = 0
    while (y <= size.height) {
        val isMajor = row % 4 == 0
        drawLine(
            color = if (isMajor) majorGrid else minorGrid,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = if (isMajor) 1.5f else 0.8f
        )
        y += spacing
        row++
    }
    }
}

/**
 * Decodes only the pixels an export can actually display. Screenshot captures
 * are commonly 1080 x 2400, while an export node is usually 300px wide.
 */
// SVG nodes are 300px wide. A small amount of oversampling keeps them sharp
// without making the generated SVG and Android share sheet unnecessarily slow.
private const val EDITABLE_EXPORT_IMAGE_WIDTH = 360
private const val EDITABLE_EXPORT_IMAGE_HEIGHT = 800

/** A stable, filesystem-safe export name without timestamps or generated IDs. */
private fun exportFileName(flowName: String, extension: String): String =
    flowName.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "flowsnap" } + ".${extension.trimStart('.')}"

private fun decodeExportBitmap(context: Context, path: String, maxWidth: Int, maxHeight: Int): android.graphics.Bitmap? {
    if (maxWidth <= 0 || maxHeight <= 0) return null
    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    decodeBitmap(context, path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (
        bounds.outWidth / (sampleSize * 2) >= maxWidth &&
        bounds.outHeight / (sampleSize * 2) >= maxHeight
    ) {
        sampleSize *= 2
    }
    val decoded = decodeBitmap(context, path, android.graphics.BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
        }) ?: return null
    if (decoded.width <= maxWidth && decoded.height <= maxHeight) return decoded

    val scale = minOf(maxWidth.toFloat() / decoded.width, maxHeight.toFloat() / decoded.height)
    val scaled = decoded.scale(
        (decoded.width * scale).toInt().coerceAtLeast(1),
        (decoded.height * scale).toInt().coerceAtLeast(1)
    )
    decoded.recycle()
    return scaled
}

private fun decodeBitmap(context: Context, location: String, options: android.graphics.BitmapFactory.Options): android.graphics.Bitmap? =
    if (location.startsWith("content://")) {
        context.contentResolver.openInputStream(location.toUri())?.use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
    } else android.graphics.BitmapFactory.decodeFile(location, options)

/** Reads dimensions only, so SVG placement can preserve each source image's aspect ratio. */
private fun sourceImageAspectRatio(context: Context, path: String): Double? = runCatching {
    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    decodeBitmap(context, path, bounds)
    if (bounds.outWidth > 0 && bounds.outHeight > 0) bounds.outWidth.toDouble() / bounds.outHeight else null
}.getOrNull()

/** Writes the same paged 7 x 4 flow arrangement to a user-selected PDF file. */
private fun writeFlowPdf(
    context: Context,
    destination: Uri,
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    flow: FlowEntity
) : Result<Unit> = runCatching {
        // PdfDocument is not a java.io.Closeable on every Android API stub,
        // so close it explicitly instead of Kotlin's use { } helper.
        val document = PdfDocument()
        try {
            for (pageStart in screens.indices step 28) {
                val pageNumber = pageStart / 28
                val pageScreens = screens.subList(pageStart, minOf(pageStart + 28, screens.size))
                val pageInfo = PdfDocument.PageInfo.Builder(1200, 1200, pageNumber + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                canvas.drawColor(android.graphics.Color.WHITE)
                val columns = flow.preferredColumns.coerceIn(1, pageScreens.size)
                val rows = (pageScreens.size + columns - 1) / columns
                val margin = 48f
                val metadataHeight = 132f
                val gap = 18f
                // Leave room for the curved row transition and its arrowhead.
                val rowGap = 44f
                val availableHeight = 1200f - margin * 2 - metadataHeight - rowGap * (rows - 1)
                val nodeWidth = minOf(
                    (1200f - margin * 2 - gap * (columns - 1)) / columns,
                    availableHeight / (rows * 2.15f)
                )
                val nodeHeight = nodeWidth * 2.15f
                val dimensions = pageScreens.map { screen ->
                    val aspect = sourceImageAspectRatio(context, screen.imagePath)?.toFloat() ?: (1f / 2.15f)
                    val imageHeight = minOf(nodeWidth / aspect, nodeHeight)
                    imageHeight * aspect to imageHeight
                }
                val rowTops = mutableListOf<Float>()
                var rowTop = margin
                for (row in 0 until rows) {
                    rowTops.add(rowTop)
                    rowTop += dimensions.drop(row * columns).take(columns).maxOf { it.second } + rowGap
                }
                val positions = List(pageScreens.size) { index ->
                    val row = index / columns
                    val count = minOf(columns, pageScreens.size - row * columns)
                    val rowWidth = count * nodeWidth + (count - 1) * gap
                    val (imageWidth, imageHeight) = dimensions[index]
                    val left = (pageInfo.pageWidth - rowWidth) / 2f + (index % columns) * (nodeWidth + gap) + (nodeWidth - imageWidth) / 2f
                    android.graphics.RectF(left, rowTops[row], left + imageWidth, rowTops[row] + imageHeight)
                }
                val indexes = pageScreens.withIndex().associate { it.value.id to it.index }
                pageScreens.forEachIndexed { index, screen ->
                    val target = positions[index]
                    // Two source pixels per PDF pixel keep the document sharp while
                    // avoiding oversized temporary share exports.
                    decodeExportBitmap(context, screen.imagePath, (target.width() * 2).toInt(), (target.height() * 2).toInt())?.let { bitmap ->
                        canvas.drawBitmap(bitmap, null, target, paint)
                        bitmap.recycle()
                    }
                }

                // Draw after bitmaps. Drawing edges first caused the screenshots to hide the lines.
                transitions.filter { it.fromScreenId in indexes && it.toScreenId in indexes }.forEach { edge ->
                    drawPdfTransition(
                        canvas = canvas,
                        from = positions[indexes.getValue(edge.fromScreenId)],
                        to = positions[indexes.getValue(edge.toScreenId)],
                        fromMarkerX = edge.fromMarkerX,
                        fromMarkerY = edge.fromMarkerY,
                        sameRow = indexes.getValue(edge.fromScreenId) / columns == indexes.getValue(edge.toScreenId) / columns,
                        connectorColor = flow.connectorColor.toInt()
                    )
                }
                drawPdfMetadataTable(canvas, paint, flow, pageNumber + 1)
                document.finishPage(page)
            }
            val output = if (destination.scheme == "file") {
                java.io.FileOutputStream(destination.path ?: error("Invalid PDF destination"))
            } else {
                context.contentResolver.openOutputStream(destination)
                    ?: error("Unable to open the selected PDF destination")
            }
            output.use { stream -> document.writeTo(stream) }
        } finally {
            document.close()
        }
}

/** Saves a standalone PDF to Downloads/FlowSnap. */
private fun writeFlowPdfToDownloads(
    context: Context,
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    flow: FlowEntity
): Result<Uri> = runCatching {
    val resolver = context.contentResolver
    val displayName = exportFileName(flow.name, "pdf")
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/FlowSnap")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        } else {
            val directory = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FlowSnap")
            check(directory.exists() || directory.mkdirs()) { "Unable to create Downloads/FlowSnap" }
            put(MediaStore.MediaColumns.DATA, java.io.File(directory, displayName).absolutePath)
        }
    }
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Files.getContentUri("external")
    }
    val uri = resolver.insert(collection, values) ?: error("Unable to create PDF in Downloads")
    try {
        writeFlowPdf(context, uri, screens, transitions, flow).getOrThrow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        }
        uri
    } catch (error: Throwable) {
        resolver.delete(uri, null, null)
        throw error
    }
}

/** Renders the complete linear diagram into one high-quality JPEG in Pictures/FlowSnap. */
private fun writeFlowJpeg(
    context: Context,
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    flow: FlowEntity
): Result<Uri> = runCatching {
    require(screens.isNotEmpty()) { "The diagram has no screenshots" }
    val columns = flow.preferredColumns.coerceIn(1, screens.size)
    val rows = (screens.size + columns - 1) / columns
    val baseWidth = 2400f
    val margin = 96f
    val gap = 36f
    val slotWidth = (baseWidth - margin * 2 - gap * (columns - 1)) / columns
    val slotHeight = slotWidth * 2.15f
    // JPEG is a clean diagram image: unlike PDF, it intentionally has no footer or metadata table.
    val rawHeight = margin * 2 + rows * slotHeight + (rows - 1) * gap
    // Keep memory below roughly 256 MB on unusually long flows.
    val scale = minOf(1f, 8192f / rawHeight)
    val width = (baseWidth * scale).toInt()
    val height = (rawHeight * scale).toInt()
    val bitmap = createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    try {
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        val positions = List(screens.size) { index ->
            val row = index / columns
            val column = index % columns
            android.graphics.RectF(
                (margin + column * (slotWidth + gap)) * scale,
                (margin + row * (slotHeight + gap)) * scale,
                (margin + column * (slotWidth + gap) + slotWidth) * scale,
                (margin + row * (slotHeight + gap) + slotHeight) * scale
            )
        }
        val imagePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        screens.forEachIndexed { index, screen ->
            val target = positions[index]
            decodeExportBitmap(context, screen.imagePath, (target.width() * 2).toInt(), (target.height() * 2).toInt())?.let { source ->
                val factor = minOf(target.width() / source.width, target.height() / source.height)
                val imageWidth = source.width * factor
                val imageHeight = source.height * factor
                val imageRect = android.graphics.RectF(target.centerX() - imageWidth / 2, target.centerY() - imageHeight / 2, target.centerX() + imageWidth / 2, target.centerY() + imageHeight / 2)
                canvas.drawBitmap(source, null, imageRect, imagePaint)
                source.recycle()
            }
        }
        val indexes = screens.withIndex().associate { it.value.id to it.index }
        transitions.filter { it.fromScreenId in indexes && it.toScreenId in indexes }.forEach { edge ->
            val fromIndex = indexes.getValue(edge.fromScreenId)
            val toIndex = indexes.getValue(edge.toScreenId)
            drawPdfTransition(canvas, positions[fromIndex], positions[toIndex], edge.fromMarkerX, edge.fromMarkerY, fromIndex / columns == toIndex / columns, flow.connectorColor.toInt())
        }
        val resolver = context.contentResolver
        val displayName = exportFileName(flow.name, "jpg")
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/FlowSnap")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            } else {
                val directory = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FlowSnap")
                check(directory.exists() || directory.mkdirs()) { "Unable to create Pictures/FlowSnap" }
                put(MediaStore.Images.Media.DATA, java.io.File(directory, displayName).absolutePath)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, values) ?: error("Unable to create JPEG in MediaStore")
        try {
            resolver.openOutputStream(uri)?.use { output ->
                check(bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, output)) { "JPEG encoding failed" }
            } ?: error("Unable to open JPEG output stream")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            }
            uri
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    } finally {
        bitmap.recycle()
    }
}

/** Writes the diagram to a lossless PNG while preserving transparent pixels around the nodes. */
private fun writeFlowPng(
    context: Context,
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    flow: FlowEntity
): Result<Uri> = runCatching {
    require(screens.isNotEmpty()) { "The diagram has no screenshots" }
    val columns = flow.preferredColumns.coerceIn(1, screens.size)
    val rows = (screens.size + columns - 1) / columns
    val baseWidth = 2400f
    val margin = 96f
    val gap = 36f
    val slotWidth = (baseWidth - margin * 2 - gap * (columns - 1)) / columns
    val slotHeight = slotWidth * 2.15f
    val rawHeight = margin * 2 + rows * slotHeight + (rows - 1) * gap
    val scale = minOf(1f, 8192f / rawHeight)
    val width = (baseWidth * scale).toInt()
    val height = (rawHeight * scale).toInt()
    val bitmap = createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    try {
        val canvas = android.graphics.Canvas(bitmap)
        // Unlike JPEG, do not paint white: untouched pixels stay transparent.
        canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
        val positions = List(screens.size) { index ->
            val row = index / columns
            val column = index % columns
            android.graphics.RectF(
                (margin + column * (slotWidth + gap)) * scale,
                (margin + row * (slotHeight + gap)) * scale,
                (margin + column * (slotWidth + gap) + slotWidth) * scale,
                (margin + row * (slotHeight + gap) + slotHeight) * scale
            )
        }
        val imagePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        screens.forEachIndexed { index, screen ->
            val target = positions[index]
            decodeExportBitmap(context, screen.imagePath, (target.width() * 2).toInt(), (target.height() * 2).toInt())?.let { source ->
                val factor = minOf(target.width() / source.width, target.height() / source.height)
                val imageWidth = source.width * factor
                val imageHeight = source.height * factor
                val imageRect = android.graphics.RectF(target.centerX() - imageWidth / 2, target.centerY() - imageHeight / 2, target.centerX() + imageWidth / 2, target.centerY() + imageHeight / 2)
                canvas.drawBitmap(source, null, imageRect, imagePaint)
                source.recycle()
            }
        }
        val indexes = screens.withIndex().associate { it.value.id to it.index }
        transitions.filter { it.fromScreenId in indexes && it.toScreenId in indexes }.forEach { edge ->
            val fromIndex = indexes.getValue(edge.fromScreenId)
            val toIndex = indexes.getValue(edge.toScreenId)
            drawPdfTransition(canvas, positions[fromIndex], positions[toIndex], edge.fromMarkerX, edge.fromMarkerY, fromIndex / columns == toIndex / columns, flow.connectorColor.toInt())
        }

        val resolver = context.contentResolver
        val displayName = exportFileName(flow.name, "png")
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/FlowSnap")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            } else {
                val directory = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FlowSnap")
                check(directory.exists() || directory.mkdirs()) { "Unable to create Pictures/FlowSnap" }
                put(MediaStore.Images.Media.DATA, java.io.File(directory, displayName).absolutePath)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, values) ?: error("Unable to create PNG in MediaStore")
        try {
            resolver.openOutputStream(uri)?.use { output ->
                check(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)) { "PNG encoding failed" }
            } ?: error("Unable to open PNG output stream")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            }
            uri
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    } finally {
        bitmap.recycle()
    }
}

/** Creates an uncompressed, self-contained mxfile that diagrams.net/draw.io can edit. */
private fun writeFlowDrawio(
    context: Context,
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    flow: FlowEntity,
    destination: java.io.File? = null
): Result<Uri> = runCatching {
    require(screens.isNotEmpty()) { "The diagram has no screenshots" }
    val columns = flow.preferredColumns.coerceIn(1, screens.size)
    val margin = 40f
    val gap = 28f
    val nodeWidth = 300f
    val nodeHeight = 645f
    val connectorColor = String.format(java.util.Locale.US, "#%06X", flow.connectorColor and 0xFFFFFF)
    val screenIndex = screens.withIndex().associate { it.value.id to it.index }
    val resolver = context.contentResolver
    val displayName = flow.name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "flowsnap" } + ".drawio"
    val uri = if (destination != null) {
        Uri.fromFile(destination)
    } else {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/xml")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/FlowSnap")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            } else {
                val directory = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FlowSnap")
                check(directory.exists() || directory.mkdirs()) { "Unable to create Downloads/FlowSnap" }
                put(MediaStore.MediaColumns.DATA, java.io.File(directory, displayName).absolutePath)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Files.getContentUri("external")
        }
        resolver.insert(collection, values) ?: error("Unable to create Draw.io file in MediaStore")
    }
    try {
        val output = destination?.outputStream() ?: resolver.openOutputStream(uri)
        output?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            // Stream each 720px high-density preview immediately. Keeping all Base64 images in a
            // StringBuilder caused large diagrams to stall during garbage collection.
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            writer.write("<mxfile host=\"app.diagrams.net\" modified=\"${System.currentTimeMillis()}\" agent=\"FlowSnap\" version=\"24.7.17\">\n")
            writer.write("  <diagram id=\"${xmlEscape(flow.id)}\" name=\"${xmlEscape(flow.name)}\">\n")
            writer.write("    <mxGraphModel dx=\"1200\" dy=\"800\" grid=\"1\" gridSize=\"10\" guides=\"1\" tooltips=\"1\" connect=\"1\" arrows=\"1\" fold=\"1\" page=\"1\" pageScale=\"1\" pageWidth=\"1169\" pageHeight=\"827\" math=\"0\" shadow=\"0\">\n")
            writer.write("      <root><mxCell id=\"0\"/><mxCell id=\"1\" parent=\"0\"/>\n")
            val writtenScreens = mutableSetOf<Int>()
            screens.forEachIndexed { index, screen ->
                val imageBase64 = screenshotPngBase64(context, screen.imagePath) ?: return@forEachIndexed
                val row = index / columns
                val column = index % columns
                // A PNG data URI uses only XML-attribute-safe Base64 characters.
                // Write it in-place: building a style String would copy it again.
                writer.write("        <mxCell id=\"screen_$index\" value=\"\" style=\"shape=image;verticalLabelPosition=bottom;verticalAlign=top;imageAspect=0;aspect=fixed;image=")
                writer.write("data:image/png;base64,")
                writer.write(imageBase64)
                writer.write(";\" vertex=\"1\" parent=\"1\">\n")
                writer.write("          <mxGeometry x=\"${margin + column * (nodeWidth + gap)}\" y=\"${margin + row * (nodeHeight + gap)}\" width=\"$nodeWidth\" height=\"$nodeHeight\" as=\"geometry\"/>\n")
                writer.write("        </mxCell>\n")
                writtenScreens += index
            }
            transitions.forEachIndexed { edgeIndex, edge ->
                val fromIndex = screenIndex[edge.fromScreenId] ?: return@forEachIndexed
                val toIndex = screenIndex[edge.toScreenId] ?: return@forEachIndexed
                if (fromIndex !in writtenScreens || toIndex !in writtenScreens) return@forEachIndexed
                val sameRow = fromIndex / columns == toIndex / columns
                val entryX = if (sameRow) 0f else .5f
                val entryY = if (sameRow) .5f else 0f
                val style = "edgeStyle=orthogonalEdgeStyle;rounded=1;arcSize=15;orthogonalLoop=1;jettySize=auto;html=1;endArrow=block;endFill=1;strokeColor=$connectorColor;exitX=${edge.fromMarkerX.coerceIn(0f, 1f)};exitY=${edge.fromMarkerY.coerceIn(0f, 1f)};exitPerimeter=0;entryX=$entryX;entryY=$entryY;entryPerimeter=0;"
                writer.write("        <mxCell id=\"edge_$edgeIndex\" value=\"\" style=\"${xmlEscape(style)}\" edge=\"1\" parent=\"1\" source=\"screen_$fromIndex\" target=\"screen_$toIndex\">\n")
                writer.write("          <mxGeometry relative=\"1\" as=\"geometry\"/>\n        </mxCell>\n")
            }
            writer.write("      </root>\n    </mxGraphModel>\n  </diagram>\n</mxfile>\n")
        }
            ?: error("Unable to open Draw.io output stream")
        if (destination == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        }
        uri
    } catch (error: Throwable) {
        if (destination != null) destination.delete() else resolver.delete(uri, null, null)
        throw error
    }
}

/**
 * Figma-safe fallback for the unreliable Sketch bitmap package: a standard SVG
 * with base64 PNG image nodes and editable vector connector paths.
 */
private fun writeFlowSvg(
    context: Context,
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    flow: FlowEntity,
    destination: java.io.File? = null
): Result<Uri> = runCatching {
    require(screens.isNotEmpty()) { "The diagram has no screenshots" }
    val columns = flow.preferredColumns.coerceIn(1, screens.size)
    val margin = 40.0
    val gap = 28.0
    val rowGap = 44.0
    val nodeWidth = 300.0
    val rows = (screens.size + columns - 1) / columns
    // Match the actual screenshot/photo dimensions. This also protects the
    // output in SVG viewers that do not fully honor preserveAspectRatio.
    val nodeHeights = screens.map { screen ->
        val aspectRatio = sourceImageAspectRatio(context, screen.imagePath) ?: (300.0 / 645.0)
        (nodeWidth / aspectRatio).coerceIn(220.0, 900.0)
    }
    val rowHeights = List(rows) { row ->
        val start = row * columns
        nodeHeights.subList(start, minOf(start + columns, nodeHeights.size)).maxOrNull() ?: 645.0
    }
    val rowTops = buildList(rows) {
        var currentTop = margin
        rowHeights.forEach { rowHeight ->
            add(currentTop)
            currentTop += rowHeight + rowGap
        }
    }
    val width = margin * 2 + columns * nodeWidth + (columns - 1) * gap
    val height = margin * 2 + rowHeights.sum() + (rows - 1) * rowGap
    val positions = List(screens.size) { index ->
        val row = index / columns
        val column = index % columns
        val nodeHeight = nodeHeights[index]
        val itemsInRow = minOf(columns, screens.size - row * columns)
        val rowWidth = itemsInRow * nodeWidth + (itemsInRow - 1) * gap
        val rowLeft = (width - rowWidth) / 2
        android.graphics.RectF(
            (rowLeft + column * (nodeWidth + gap)).toFloat(),
            rowTops[row].toFloat(),
            (rowLeft + column * (nodeWidth + gap) + nodeWidth).toFloat(),
            (rowTops[row] + nodeHeight).toFloat()
        )
    }
    val screenIndex = screens.withIndex().associate { it.value.id to it.index }
    val connectorColor = String.format(java.util.Locale.US, "#%06X", flow.connectorColor and 0xFFFFFF)
    val resolver = context.contentResolver
    val displayName = exportFileName(flow.name, "svg")
    val uri = if (destination != null) {
        Uri.fromFile(destination)
    } else {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/svg+xml")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/FlowSnap")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            } else {
                val directory = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FlowSnap")
                check(directory.exists() || directory.mkdirs()) { "Unable to create Downloads/FlowSnap" }
                put(MediaStore.MediaColumns.DATA, java.io.File(directory, displayName).absolutePath)
            }
        }
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else MediaStore.Files.getContentUri("external")
        resolver.insert(collection, values) ?: error("Unable to create SVG file in MediaStore")
    }
    try {
        val output = destination?.outputStream() ?: resolver.openOutputStream(uri)
        output?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            writer.write("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"$width\" height=\"$height\" viewBox=\"0 0 $width $height\">\n")
            writer.write("  <title>${xmlEscape(flow.name)}</title>\n  <rect width=\"100%\" height=\"100%\" fill=\"white\"/>\n")
            // Write and release one compact image at a time instead of retaining
            // a whole diagram worth of Base64 strings in memory.
            screens.forEachIndexed { index, screen ->
                val imageBase64 = screenshotPngBase64(context, screen.imagePath) ?: return@forEachIndexed
                val rect = positions[index]
                // Base64 output contains no quote, ampersand, or angle bracket,
                // so it can be streamed directly without a second full-size copy.
                writer.write("  <g id=\"screen-${index + 1}\">\n    <image id=\"screenshot-${index + 1}\" x=\"${rect.left}\" y=\"${rect.top}\" width=\"${rect.width()}\" height=\"${rect.height()}\" preserveAspectRatio=\"xMidYMid meet\" href=\"")
                writer.write("data:image/png;base64,")
                writer.write(imageBase64)
                writer.write("\"/>\n  </g>\n")
            }
            // SVG source order controls z-order, so transitions intentionally
            // follow images and remain visible above every screenshot.
            transitions.forEach { edge ->
                val fromIndex = screenIndex[edge.fromScreenId] ?: return@forEach
                val toIndex = screenIndex[edge.toScreenId] ?: return@forEach
                val from = positions[fromIndex]
                val to = positions[toIndex]
                val sameRow = fromIndex / columns == toIndex / columns
                val startX = from.left + from.width() * edge.fromMarkerX.coerceIn(0f, 1f)
                val startY = from.top + from.height() * edge.fromMarkerY.coerceIn(0f, 1f)
                val endX = if (sameRow) to.left else to.centerX()
                val endY = if (sameRow) to.centerY() else to.top
                val (routePoints, finalStartX, finalStartY) = if (sameRow) {
                    val laneX = (startX + endX) / 2f
                    Triple(listOf(startX to startY, laneX to startY, laneX to endY), laneX, endY)
                } else {
                    val laneX = maxOf(from.right, to.right) + 10f
                    val laneY = endY - 28f
                    Triple(listOf(startX to startY, laneX to startY, laneX to laneY, endX to laneY), endX, laneY)
                }
                val directionX = endX - finalStartX
                val directionY = endY - finalStartY
                val length = kotlin.math.sqrt(directionX * directionX + directionY * directionY).coerceAtLeast(1f)
                val unitX = directionX / length
                val unitY = directionY / length
                val baseX = endX - unitX * 13f
                val baseY = endY - unitY * 13f
                val perpendicularX = -unitY * 7f
                val perpendicularY = unitX * 7f
                val route = roundedSvgPath(routePoints + (endX to endY))
                writer.write("  <path d=\"$route\" fill=\"none\" stroke=\"$connectorColor\" stroke-width=\"3.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>\n")
                writer.write("  <path d=\"M ${baseX + perpendicularX} ${baseY + perpendicularY} L $endX $endY L ${baseX - perpendicularX} ${baseY - perpendicularY}\" fill=\"none\" stroke=\"$connectorColor\" stroke-width=\"3.5\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>\n")
                writer.write("  <circle cx=\"$startX\" cy=\"$startY\" r=\"5\" fill=\"$connectorColor\"/>\n")
            }
            writer.write("</svg>\n")
        }
            ?: error("Unable to open SVG output stream")
        if (destination == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        uri
    } catch (error: Throwable) {
        if (destination != null) destination.delete() else resolver.delete(uri, null, null)
        throw error
    }
}

/** Encodes the compact preview used by Draw.io/SVG without Base64 line wraps. */
private fun screenshotPngBase64(context: Context, path: String): String? = runCatching {
    // Keep a modestly oversized asset for the 300px SVG/Draw.io node: it stays
    // crisp in Figma without retaining full device-resolution captures.
    val bitmap = decodeExportBitmap(context, path, EDITABLE_EXPORT_IMAGE_WIDTH, EDITABLE_EXPORT_IMAGE_HEIGHT) ?: return@runCatching null
    try {
        val bytes = java.io.ByteArrayOutputStream().use { output ->
            check(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)) { "Unable to encode screenshot PNG" }
            output.toByteArray()
        }
        Base64.encodeToString(bytes, Base64.NO_WRAP)
    } finally {
        bitmap.recycle()
    }
}.getOrNull()

private fun xmlEscape(value: String): String = buildString(value.length) {
    value.forEach { character ->
        append(when (character) {
            '&' -> "&amp;"; '<' -> "&lt;"; '>' -> "&gt;"; '\"' -> "&quot;"; '\'' -> "&apos;"
            else -> character
        })
    }
}

private data class SketchImageAsset(val index: Int, val screen: ScreenEntity, val id: String, val location: String)

/** Re-encode to a conventional PNG so the asset is readable by Sketch/Figma. */
private fun sketchPngBytes(context: Context, location: String): ByteArray? =
    decodeExportBitmap(context, location, EDITABLE_EXPORT_IMAGE_WIDTH, EDITABLE_EXPORT_IMAGE_HEIGHT)?.let { bitmap ->
        try {
            java.io.ByteArrayOutputStream().use { output ->
                check(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output)) {
                    "Unable to encode screenshot PNG"
                }
                output.toByteArray()
            }
        } finally {
            bitmap.recycle()
        }
    }

/** Sketch addresses bundled images by an MD5 filename that includes the file extension. */
private fun sketchImageId(pngBytes: ByteArray): String {
    val digest = MessageDigest.getInstance("MD5")
    digest.update(pngBytes)
    return digest.digest().joinToString("") { byte -> "%02x".format(byte.toInt() and 0xFF) }
}

/** Produces a portable Sketch archive with editable artboards, bitmap layers, and transition paths. */
private fun writeFlowSketch(
    context: Context,
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    flow: FlowEntity
): Result<Uri> = runCatching {
    require(screens.isNotEmpty()) { "The diagram has no screenshots" }
    val assets = screens.mapIndexedNotNull { index, screen ->
        sketchPngBytes(context, screen.imagePath)?.let { pngBytes ->
            SketchImageAsset(index, screen, sketchImageId(pngBytes), screen.imagePath)
        }
    }
    require(assets.isNotEmpty()) { "No screenshot assets are available" }
    val assetsByScreenId = assets.associateBy { it.screen.id }
    val pageId = UUID.randomUUID().toString().uppercase()
    val documentId = UUID.randomUUID().toString().uppercase()
    val columns = minOf(5, screens.size)
    val margin = 40.0
    val gap = 28.0
    val nodeWidth = 300.0
    val nodeHeight = 645.0
    val rows = (screens.size + columns - 1) / columns
    val pageWidth = margin * 2 + columns * nodeWidth + (columns - 1) * gap
    val pageHeight = margin * 2 + rows * nodeHeight + (rows - 1) * gap
    fun frame(x: Double, y: Double, width: Double, height: Double) = JSONObject()
        .put("_class", "rect")
        .put("constrainProportions", false)
        .put("x", x).put("y", y).put("width", width).put("height", height)

    fun color(red: Double, green: Double, blue: Double, alpha: Double = 1.0) = JSONObject()
        .put("_class", "color")
        .put("red", red).put("green", green).put("blue", blue).put("alpha", alpha)

    fun solidGradient() = JSONObject()
        .put("_class", "gradient")
        .put("gradientType", 0)
        .put("elipseLength", 0)
        .put("from", "{0, 0}")
        .put("to", "{1, 1}")
        .put("stops", JSONArray().put(JSONObject()
            .put("_class", "gradientStop")
            .put("position", 0)
            .put("color", color(0.0, 0.0, 0.0, 1.0))))

    fun graphicsContext() = JSONObject()
        .put("_class", "graphicsContextSettings")
        .put("blendMode", 0)
        .put("opacity", 1)

    fun emptyStyle() = JSONObject()
        .put("_class", "style")
        .put("do_objectID", UUID.randomUUID().toString().uppercase())
        .put("borderOptions", JSONObject()
            .put("_class", "borderOptions")
            .put("isEnabled", false)
            .put("dashPattern", JSONArray())
            .put("lineCapStyle", 0)
            .put("lineJoinStyle", 0))
        .put("startMarkerType", 0)
        .put("endMarkerType", 0)
        .put("miterLimit", 10)
        .put("windingRule", 1)
        .put("borders", JSONArray())
        .put("fills", JSONArray())
        .put("shadows", JSONArray())
        .put("innerShadows", JSONArray())
        .put("colorControls", JSONObject()
            .put("_class", "colorControls")
            .put("isEnabled", false)
            .put("brightness", 0)
            .put("contrast", 0)
            .put("hue", 0)
            .put("saturation", 0))

    fun exportOptions() = JSONObject()
        .put("_class", "exportOptions")
        .put("exportFormats", JSONArray())
        .put("includedLayerIds", JSONArray())
        .put("layerOptions", 0)
        .put("shouldTrim", false)

    fun baseLayer(layerClass: String, id: String, name: String, layerFrame: JSONObject) = JSONObject()
        .put("_class", layerClass)
        .put("do_objectID", id)
        .put("name", name)
        .put("nameIsFixed", false)
        .put("frame", layerFrame)
        .put("booleanOperation", -1)
        .put("isFixedToViewport", false)
        .put("isFlippedHorizontal", false)
        .put("isFlippedVertical", false)
        .put("isLocked", false)
        .put("isVisible", true)
        .put("layerListExpandedType", 0)
        .put("resizingConstraint", 63)
        .put("resizingType", 0)
        .put("rotation", 0)
        .put("shouldBreakMaskChain", false)
        .put("exportOptions", exportOptions())

    fun bitmapLayer(asset: SketchImageAsset) = baseLayer(
        layerClass = "bitmap",
        id = UUID.randomUUID().toString().uppercase(),
        name = "Screenshot ${asset.index + 1}",
        layerFrame = frame(0.0, 0.0, nodeWidth, nodeHeight)
    )
        .put("image", JSONObject()
            .put("_class", "MSJSONFileReference")
            .put("_ref_class", "MSImageData")
            .put("_ref", "images/${asset.id}.png"))
        .put("clippingMaskMode", 0)
        .put("fillReplacesImage", false)
        .put("intendedDPI", 72)
        .put("clippingMask", "{{0, 0}, {1, 1}}")
        .put("nineSliceCenterRect", "{{0, 0}, {0, 0}}")
        .put("nineSliceScale", "{1, 1}")
        .put("style", emptyStyle())

    val layers = JSONArray()
    val artboards = JSONObject()
    assets.forEach { asset ->
        val row = asset.index / columns
        val column = asset.index % columns
        val artboardId = UUID.randomUUID().toString().uppercase()
        layers.put(baseLayer(
            layerClass = "artboard",
            id = artboardId,
            name = "Screen ${asset.index + 1}",
            layerFrame = frame(margin + column * (nodeWidth + gap), margin + row * (nodeHeight + gap), nodeWidth, nodeHeight)
        )
            .put("layers", JSONArray().put(bitmapLayer(asset)))
            .put("backgroundColor", color(1.0, 1.0, 1.0, 1.0))
            .put("hasBackgroundColor", false)
            .put("includeBackgroundColorInExport", true)
            .put("includeInCloudUpload", true)
            .put("hasClickThrough", false)
            .put("isFlowHome", false)
            .put("resizesContent", false)
            .put("horizontalRulerData", JSONObject().put("_class", "rulerData").put("base", 0).put("guides", JSONArray()))
            .put("verticalRulerData", JSONObject().put("_class", "rulerData").put("base", 0).put("guides", JSONArray()))
            .put("style", emptyStyle()))
        artboards.put(artboardId, JSONObject().put("name", "Screen ${asset.index + 1}"))
    }
    // Preserve transition relationships as editable Sketch vector path layers.
    transitions.forEach { transition ->
        val fromAsset = assetsByScreenId[transition.fromScreenId] ?: return@forEach
        val toAsset = assetsByScreenId[transition.toScreenId] ?: return@forEach
        val fromRow = fromAsset.index / columns
        val fromColumn = fromAsset.index % columns
        val toRow = toAsset.index / columns
        val toColumn = toAsset.index % columns
        val startX = margin + fromColumn * (nodeWidth + gap) + nodeWidth * transition.fromMarkerX.coerceIn(0f, 1f)
        val startY = margin + fromRow * (nodeHeight + gap) + nodeHeight * transition.fromMarkerY.coerceIn(0f, 1f)
        val endX = if (fromRow == toRow) margin + toColumn * (nodeWidth + gap) else margin + toColumn * (nodeWidth + gap) + nodeWidth / 2
        val endY = if (fromRow == toRow) margin + toRow * (nodeHeight + gap) + nodeHeight / 2 else margin + toRow * (nodeHeight + gap)
        val left = minOf(startX, endX)
        val top = minOf(startY, endY)
        val pathWidth = maxOf(1.0, kotlin.math.abs(endX - startX))
        val pathHeight = maxOf(1.0, kotlin.math.abs(endY - startY))
        fun point(x: Double, y: Double) = JSONObject()
            .put("_class", "curvePoint")
            .put("cornerRadius", 0)
            .put("curveFrom", "{0, 0}")
            .put("curveTo", "{0, 0}")
            .put("curveMode", 1)
            .put("hasCurveFrom", false)
            .put("hasCurveTo", false)
            .put("point", "{${(x - left) / pathWidth}, ${(y - top) / pathHeight}}")
        val border = JSONObject()
            .put("_class", "border")
            .put("isEnabled", true)
            .put("thickness", 2)
            .put("fillType", 0)
            .put("position", 0)
            .put("contextSettings", graphicsContext())
            .put("gradient", solidGradient())
            .put("color", color(
                ((flow.connectorColor shr 16) and 0xFF) / 255.0,
                ((flow.connectorColor shr 8) and 0xFF) / 255.0,
                (flow.connectorColor and 0xFF) / 255.0
            ))
        val transitionStyle = emptyStyle()
            .put("borders", JSONArray().put(border))
            .put("endMarkerType", 1)
        layers.put(baseLayer(
            layerClass = "shapePath",
            id = UUID.randomUUID().toString().uppercase(),
            name = "Transition",
            layerFrame = frame(left, top, pathWidth, pathHeight)
        )
            .put("isClosed", false)
            .put("edited", false)
            .put("pointRadiusBehaviour", 1)
            .put("points", JSONArray().put(point(startX, startY)).put(point(endX, endY)))
            .put("style", transitionStyle)
            .put("clippingMaskMode", 0))
    }

    val page = baseLayer(
        layerClass = "page",
        id = pageId,
        name = flow.name,
        layerFrame = frame(0.0, 0.0, pageWidth, pageHeight)
    )
        .put("hasClickThrough", false)
        .put("includeInCloudUpload", true)
        .put("horizontalRulerData", JSONObject().put("_class", "rulerData").put("base", 0).put("guides", JSONArray()))
        .put("verticalRulerData", JSONObject().put("_class", "rulerData").put("base", 0).put("guides", JSONArray()))
        .put("style", emptyStyle())
        .put("layers", layers)
    // Multiple flow steps can legitimately reuse the same screenshot. Each layer
    // keeps its own placement, but a Sketch ZIP may contain each MD5 image only once.
    val uniqueAssets = assets.distinctBy { it.id }
    val imageReferences = JSONArray().apply {
        uniqueAssets.forEach { asset ->
            put(JSONObject()
                .put("_class", "MSJSONFileReference")
                .put("_ref_class", "MSImageData")
                .put("_ref", "images/${asset.id}.png"))
        }
    }
    val document = JSONObject()
        .put("_class", "document")
        .put("do_objectID", documentId)
        .put("assets", JSONObject()
            .put("_class", "assetCollection")
            .put("do_objectID", UUID.randomUUID().toString().uppercase())
            .put("colorAssets", JSONArray())
            .put("gradientAssets", JSONArray())
            .put("colors", JSONArray())
            .put("gradients", JSONArray())
            .put("images", imageReferences)
            .put("exportPresets", JSONArray())
            .put("imageCollection", JSONObject().put("_class", "imageCollection").put("images", JSONObject())))
        .put("currentPageIndex", 0)
        .put("colorSpace", 0)
        .put("foreignLayerStyles", JSONArray())
        .put("foreignSymbols", JSONArray())
        .put("foreignTextStyles", JSONArray())
        .put("layerStyles", JSONObject().put("_class", "sharedStyleContainer").put("objects", JSONArray()))
        .put("layerTextStyles", JSONObject().put("_class", "sharedTextStyleContainer").put("objects", JSONArray()))
        .put("layerSymbols", JSONObject().put("_class", "symbolContainer").put("objects", JSONArray()))
        .put("pages", JSONArray().put(JSONObject()
            .put("_class", "MSJSONFileReference")
            .put("_ref", "pages/$pageId")
            .put("_ref_class", "MSImmutablePage")))
    val meta = JSONObject()
        .put("version", 121)
        .put("compatibilityVersion", 99)
        .put("app", "com.bohemiancoding.sketch3")
        .put("appVersion", "62")
        .put("build", 0)
        .put("commit", "flowsnap")
        .put("fonts", JSONArray())
        .put("autosaved", 0)
        .put("variant", "NONAPPSTORE")
        .put("saveHistory", JSONArray().put("NONAPPSTORE.0"))
        .put("created", JSONObject()
            .put("app", "com.bohemiancoding.sketch3")
            .put("appVersion", "62")
            .put("build", 0)
            .put("commit", "flowsnap")
            .put("compatibilityVersion", 99)
            .put("version", 121)
            .put("variant", "NONAPPSTORE"))
        .put("pagesAndArtboards", JSONObject().put(pageId, JSONObject().put("name", flow.name).put("artboards", artboards)))
    val user = JSONObject()
        .put("document", JSONObject().put("pageListHeight", 0).put("pageListCollapsed", 0))
        .put(pageId, JSONObject().put("scrollOrigin", "{0, 0}").put("zoomValue", 1))

    val resolver = context.contentResolver
    val displayName = sketchDisplayName(flow.name)
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        // Preserve the explicit .sketch display name; generic octet-stream is often downloaded as .bin.
        put(MediaStore.MediaColumns.MIME_TYPE, "application/x-sketch")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/FlowSnap")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        } else {
            val directory = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FlowSnap")
            check(directory.exists() || directory.mkdirs()) { "Unable to create Downloads/FlowSnap" }
            put(MediaStore.MediaColumns.DATA, java.io.File(directory, displayName).absolutePath)
        }
    }
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) else MediaStore.Files.getContentUri("external")
    val uri = resolver.insert(collection, values) ?: error("Unable to create Sketch file in MediaStore")
    try {
        resolver.openOutputStream(uri)?.use { output ->
            ZipOutputStream(output.buffered()).use { zip ->
                zip.writeZipText("document.json", document.toString())
                zip.writeZipText("meta.json", meta.toString())
                zip.writeZipText("user.json", user.toString())
                zip.writeZipText("pages/$pageId.json", page.toString())
                uniqueAssets.forEach { asset ->
                    // The entry must exactly match the image layer's `_ref`.
                    zip.putNextEntry(ZipEntry("images/${asset.id}.png"))
                    val pngBytes = sketchPngBytes(context, asset.location)
                        ?: error("Unable to decode screenshot asset: ${asset.location}")
                    check(sketchImageId(pngBytes) == asset.id) { "Screenshot asset changed during export" }
                    zip.write(pngBytes)
                    zip.closeEntry()
                }
                // Writes the ZIP central directory before MediaStore exposes the pending file.
                zip.finish()
            }
        } ?: error("Unable to open Sketch output stream")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        uri
    } catch (error: Throwable) {
        resolver.delete(uri, null, null)
        throw error
    }
}

private fun sketchDisplayName(flowName: String): String {
    val sanitized = flowName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        .replace(Regex("(?i)(\\.sketch|\\.zip)+$"), "")
        .trimEnd('.')
        .ifBlank { "flowsnap" }
    // Make the name unique before MediaStore inserts it. Otherwise, some providers append
    // " (1)" after the extension, yielding `flow.sketch (1)`, which Figma will not filter.
    return "$sanitized-${System.currentTimeMillis()}.sketch"
}

private fun ZipOutputStream.writeZipText(name: String, content: String) {
    putNextEntry(ZipEntry(name))
    write(content.toByteArray(Charsets.UTF_8))
    closeEntry()
}

/** Rounds each SVG polyline corner without exceeding either adjacent segment. */
private fun roundedSvgPath(points: List<Pair<Float, Float>>): String {
    val radius = 10f
    val result = StringBuilder("M ${points.first().first} ${points.first().second}")
    for (i in 1 until points.lastIndex) {
        val (x0, y0) = points[i - 1]
        val (x, y) = points[i]
        val (x1, y1) = points[i + 1]
        val before = kotlin.math.hypot(x - x0, y - y0)
        val after = kotlin.math.hypot(x1 - x, y1 - y)
        if (before < .001f || after < .001f) continue
        val r = minOf(radius, before / 2f, after / 2f)
        result.append(" L ${x - (x - x0) * r / before} ${y - (y - y0) * r / before}")
        result.append(" Q $x $y ${x + (x1 - x) * r / after} ${y + (y1 - y) * r / after}")
    }
    result.append(" L ${points.last().first} ${points.last().second}")
    return result.toString()
}

/** Mirrors the editor's visible orthogonal marker-to-next-screen connector. */
private fun drawPdfTransition(
    canvas: android.graphics.Canvas,
    from: android.graphics.RectF,
    to: android.graphics.RectF,
    fromMarkerX: Float,
    fromMarkerY: Float,
    sameRow: Boolean,
    connectorColor: Int
) {
    val startX = from.left + from.width() * fromMarkerX
    val startY = from.top + from.height() * fromMarkerY
    val endX = if (sameRow) to.left else to.centerX()
    val endY = if (sameRow) to.centerY() else to.top
    val (finalStartX, finalStartY, path) = if (sameRow) {
        val laneX = (startX + endX) / 2f
        Triple(
            laneX,
            endY,
            android.graphics.Path().apply {
                moveTo(startX, startY)
                lineTo(laneX, startY)
                lineTo(laneX, endY)
            }
        )
    } else {
        // Finish vertically above the next-row target so the arrow points down
        // into its top edge, matching the SVG export.
        val laneX = maxOf(from.right, to.right) + 10f
        val laneY = endY - 28f
        Triple(
            endX,
            laneY,
            android.graphics.Path().apply {
                moveTo(startX, startY)
                lineTo(laneX, startY)
                lineTo(laneX, laneY)
                lineTo(endX, laneY)
            }
        )
    }
    val directionX = endX - finalStartX
    val directionY = endY - finalStartY
    val directionLength = kotlin.math.sqrt(directionX * directionX + directionY * directionY)
        .coerceAtLeast(1f)
    val unitX = directionX / directionLength
    val unitY = directionY / directionLength
    val arrowLength = 13f
    val arrowHalfWidth = 7f
    val baseX = endX - unitX * arrowLength
    val baseY = endY - unitY * arrowLength
    val perpendicularX = -unitY * arrowHalfWidth
    val perpendicularY = unitX * arrowHalfWidth
    path.lineTo(endX, endY)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = connectorColor; style = android.graphics.Paint.Style.STROKE; strokeWidth = 3.5f
        strokeJoin = android.graphics.Paint.Join.ROUND; strokeCap = android.graphics.Paint.Cap.ROUND
        pathEffect = android.graphics.CornerPathEffect(10f)
    }
    canvas.drawPath(path, paint)
    paint.pathEffect = null
    val arrow = android.graphics.Path().apply {
        moveTo(baseX + perpendicularX, baseY + perpendicularY)
        lineTo(endX, endY)
        lineTo(baseX - perpendicularX, baseY - perpendicularY)
    }
    canvas.drawPath(arrow, paint)
    paint.style = android.graphics.Paint.Style.FILL
    canvas.drawCircle(startX, startY, 5f, paint)
}

private fun drawPdfMetadataTable(
    canvas: android.graphics.Canvas,
    paint: android.graphics.Paint,
    flow: FlowEntity,
    pageNumber: Int
) {
    val left = 48f
    val top = 1020f
    val right = 1152f
    val height = 132f
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.rgb(248, 248, 248)
    canvas.drawRect(left, top, right, top + height, paint)
    val labels = listOf("Title", "Version", "Created At", "Created By", "App Version")
    val values = listOf(
        flow.name,
        flow.diagramVersion,
        java.text.SimpleDateFormat("yyyy MMM dd", java.util.Locale.getDefault()).format(java.util.Date(flow.createdAt)),
        flow.createdBy,
        "v1.0"
    )
    val columnWidth = (right - left) / labels.size
    labels.indices.forEach { index ->
        val x = left + 16f + index * columnWidth
        paint.color = android.graphics.Color.rgb(135, 135, 135)
        paint.textSize = 16f
        canvas.drawText(labels[index], x, top + 42f, paint)
        paint.color = android.graphics.Color.rgb(55, 55, 55)
        paint.textSize = 21f
        canvas.drawText(ellipsizePdfText(values[index], paint, columnWidth - 28f), x, top + 82f, paint)
    }
    paint.color = android.graphics.Color.rgb(135, 135, 135)
    paint.textSize = 12f
    canvas.drawText("Page $pageNumber", right - 58f, top + height - 12f, paint)
}

private fun ellipsizePdfText(value: String, paint: android.graphics.Paint, maxWidth: Float): String {
    if (paint.measureText(value) <= maxWidth) return value
    val suffix = "…"
    var end = value.length
    while (end > 0 && paint.measureText(value.substring(0, end) + suffix) > maxWidth) end--
    return value.substring(0, end) + suffix
}

/** Renders the supplied Android Studio vector assets in the existing editor control style. */
@Composable private fun EditorDrawableCircle(iconRes: Int, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, large: Boolean = false, enabled: Boolean = true, iconTint: Color? = null) {
    val size = if (large) 60.dp else 44.dp
    val defaultIconTint = if (enabled) UiColor.primary else UiColor.extra.disabled
    val controlColor = if (UiColor.background.luminance() > .5f) UiColor.surface else UiColor.primaryContainer
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = controlColor,
        shadowElevation = 5.dp
    ) {
        Box(Modifier.fillMaxSize().clip(CircleShape).clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(iconRes),
                label,
                tint = if (enabled) iconTint ?: defaultIconTint else defaultIconTint,
                modifier = Modifier.size(if (large) 32.dp else 23.dp)
            )
        }
    }
}

@Composable private fun DiagramPreview(
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    flow: FlowEntity,
    onDeleteScreen: (ScreenEntity) -> Unit,
    onReplaceScreen: (ScreenEntity) -> Unit,
    onUpdatePointer: (ScreenEntity, Float, Float) -> Unit,
    onLightboxVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmaps = rememberScreenBitmaps(screens, maxDimension = 1600)
    var selectedScreen by remember { mutableStateOf<ScreenEntity?>(null) }
    var previewZoom by remember(flow.id) { mutableFloatStateOf(1f) }
    var previewPan by remember(flow.id) { mutableStateOf(Offset.Zero) }
    val columns = flow.preferredColumns.coerceIn(1, screens.size.coerceAtLeast(1))
    val rows = (screens.size + columns - 1) / columns
    val previewHeight = (rows * 132).dp
    Column(modifier) {
    Card(
        modifier = Modifier.fillMaxWidth().weight(1f),
        colors = CardDefaults.cardColors(containerColor = UiColor.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(12.dp).fillMaxSize()) {
            Box(
                Modifier.fillMaxWidth().weight(1f).clipToBounds()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, gesturePan, gestureZoom, _ ->
                            previewZoom = (previewZoom * gestureZoom).coerceIn(.5f, 3f)
                            previewPan += gesturePan
                        }
                    }
            ) {
                FlowDiagramCanvas(
                    screens = screens,
                    transitions = transitions,
                    bitmaps = bitmaps,
                    showMobileStencils = flow.showMobileStencils,
                    connectorColor = themedFlowConnectorColor(flow.connectorColor),
                    columnsOverride = columns,
                    onScreenClick = {
                        selectedScreen = it
                        onLightboxVisibilityChange(true)
                    },
                    modifier = Modifier.fillMaxWidth().height(previewHeight).graphicsLayer(
                        scaleX = previewZoom,
                        scaleY = previewZoom,
                        translationX = previewPan.x,
                        translationY = previewPan.y
                    )
                )
                Row(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallCanvasDrawableControl(R.drawable.ic_flow_zoom_out, "Zoom out") {
                        previewZoom = (previewZoom / 1.25f).coerceAtLeast(.5f)
                    }
                    SmallCanvasDrawableControl(R.drawable.ic_flow_zoom_in, "Zoom in") {
                        previewZoom = (previewZoom * 1.25f).coerceAtMost(3f)
                    }
                    SmallCanvasDrawableControl(R.drawable.ic_flow_arrange, "Fit flow") {
                        previewZoom = 1f
                        previewPan = Offset.Zero
                    }
                }
            }
            val metadata = buildList {
                if (flow.showTitle) add("Title" to flow.name)
                if (flow.showVersion) add("Version" to flow.diagramVersion)
                if (flow.showCreatedAt) add("Created at" to java.text.SimpleDateFormat("yyyy MMM dd", java.util.Locale.getDefault()).format(java.util.Date(flow.createdAt)))
                if (flow.showCreatedBy) add("Created by" to flow.createdBy)
                if (flow.showAppVersion) add("App version" to "v1.0")
            }
            if (metadata.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(UiColor.surfaceVariant)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    metadata.forEach { (label, value) ->
                        Column(Modifier.weight(1f, fill = false)) {
                            Text(label, fontSize = 11.sp, color = UiColor.onSurfaceVariant)
                            Text(value, fontSize = 13.sp, color = UiColor.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
    selectedScreen?.let { screen ->
        val currentIndex = screens.indexOfFirst { it.id == screen.id }.coerceAtLeast(0)
        ScreenshotLightbox(
            screen = screen,
            screenPosition = currentIndex,
            screenCount = screens.size,
            onDismiss = {
                selectedScreen = null
                onLightboxVisibilityChange(false)
            },
            onDelete = onDeleteScreen,
            onReplace = onReplaceScreen,
            onUpdatePointer = onUpdatePointer,
            onSlide = { direction ->
                screens.getOrNull(currentIndex + direction)?.let { selectedScreen = it }
            }
        )
    }
    }
}

/** Maps the built-in green choice, including legacy saved flows, to the theme's flow semantic. */
@Composable private fun themedFlowConnectorColor(connectorColor: Long): Color = when (connectorColor) {
    ConnectorPalette.Green,
    0xFF69B83AL -> UiColor.extra.marker
    else -> Color(connectorColor.toInt())
}

/** Full-screen viewer for inspecting an individual screenshot without changing the diagram. */
/** Bounds of the actual fitted screenshot, excluding black letterbox bars. */
private fun fittedScreenshotRect(
    containerWidth: Float,
    containerHeight: Float,
    imageWidth: Int,
    imageHeight: Int
): androidx.compose.ui.geometry.Rect {
    val bounds = fittedScreenshotBounds(containerWidth, containerHeight, imageWidth, imageHeight)
    return androidx.compose.ui.geometry.Rect(bounds.left, bounds.top, bounds.left + bounds.width, bounds.top + bounds.height)
}

/** The crop editor leaves a dark rail around the image for its border and handles. */
private fun fittedScreenshotRectInPadding(
    containerWidth: Float,
    containerHeight: Float,
    imageWidth: Int,
    imageHeight: Int,
    padding: Float
): androidx.compose.ui.geometry.Rect {
    val innerWidth = (containerWidth - 2f * padding).coerceAtLeast(1f)
    val innerHeight = (containerHeight - 2f * padding).coerceAtLeast(1f)
    val inner = fittedScreenshotRect(innerWidth, innerHeight, imageWidth, imageHeight)
    return androidx.compose.ui.geometry.Rect(
        inner.left + padding,
        inner.top + padding,
        inner.right + padding,
        inner.bottom + padding
    )
}

@Suppress("DEPRECATION") // Fallback colors for devices with opaque system-bar regions.
@Composable private fun ScreenshotLightbox(
    screen: ScreenEntity,
    screenPosition: Int,
    screenCount: Int,
    onDismiss: () -> Unit,
    onDelete: (ScreenEntity) -> Unit,
    onReplace: (ScreenEntity) -> Unit,
    onUpdatePointer: (ScreenEntity, Float, Float) -> Unit,
    onSlide: (Int) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hostActivity = LocalActivity.current
    val workspace = UiColor.extra
    val viewerBarColor = workspace.darkSurfaceVariant
    val lightboxStatusBar = workspace.statusBar
    val viewerSurface = workspace.darkSurface
    val viewerSurfaceVariant = Color(0xFF29344B)
    val viewerOutline = workspace.darkOutline
    val viewerOnSurface = workspace.onDarkSurface
    val viewerMuted = workspace.darkOnSurfaceMuted
    // The screenshot viewer stays dark independently of the rest of the app.
    androidx.compose.runtime.DisposableEffect(hostActivity, viewerBarColor) {
        val hostWindow = hostActivity?.window ?: return@DisposableEffect onDispose { }
        val controller = WindowCompat.getInsetsController(hostWindow, hostWindow.decorView)
        val previousLightStatusIcons = controller.isAppearanceLightStatusBars
        val previousLightNavigationIcons = controller.isAppearanceLightNavigationBars
        val previousStatusBarColor = hostWindow.statusBarColor
        val previousNavigationBarColor = hostWindow.navigationBarColor
        hostWindow.statusBarColor = lightboxStatusBar.toArgb()
        hostWindow.navigationBarColor = viewerBarColor.toArgb()
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        onDispose {
            hostWindow.statusBarColor = previousStatusBarColor
            hostWindow.navigationBarColor = previousNavigationBarColor
            controller.isAppearanceLightStatusBars = previousLightStatusIcons
            controller.isAppearanceLightNavigationBars = previousLightNavigationIcons
        }
    }
    val screenshotState = rememberScreenBitmapStates(listOf(screen), maxDimension = 2400)[screen.id]
        ?: ScreenshotBitmapState.Loading
    val bitmap = (screenshotState as? ScreenshotBitmapState.Ready)?.bitmap
    var visible by remember { mutableStateOf(false) }
    var closeRequested by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var editingPointer by remember(screen.id) { mutableStateOf(false) }
    var pointerPosition by remember(screen.id) { mutableStateOf(Offset(screen.markerX, screen.markerY)) }
    val pointerGuide = viewerOnSurface.copy(alpha = .45f)
    val pointerShadow = viewerBarColor.copy(alpha = .72f)
    val pointerRing = viewerOnSurface.copy(alpha = .9f)
    val pointerAccent = workspace.marker
    val backdropAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(180),
        label = "lightboxBackdrop"
    )
    val imageScale by animateFloatAsState(
        targetValue = if (visible) 1f else .94f,
        animationSpec = tween(220),
        label = "lightboxImage"
    )
    val requestClose = {
        if (!closeRequested) closeRequested = true
    }
    val backdropInteractionSource = remember { MutableInteractionSource() }
    val consumeTapInteractionSource = remember { MutableInteractionSource() }
    androidx.compose.runtime.LaunchedEffect(Unit) { visible = true }
    androidx.compose.runtime.LaunchedEffect(closeRequested) {
        if (closeRequested) {
            visible = false
            delay(180.milliseconds)
            onDismiss()
        }
    }
    Dialog(
        onDismissRequest = requestClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Android 12+ can blur the activity window behind this dialog. Older
        // versions retain the same focused dark scrim without the blur.
        val dialogView = LocalView.current
        SideEffect {
            val window = (dialogView.parent as? DialogWindowProvider)?.window
            // On Android 15 the status bar is edge-to-edge. Let this dialog
            // render behind it so the lightbox backdrop, rather than the
            // host screen's pale system-bar surface, fills that region.
            window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
            window?.setDimAmount(.42f)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window?.isNavigationBarContrastEnforced = false
            }
            window?.let {
                it.statusBarColor = lightboxStatusBar.toArgb()
                it.navigationBarColor = viewerBarColor.toArgb()
                val dialogController = WindowCompat.getInsetsController(it, dialogView)
                dialogController.isAppearanceLightStatusBars = false
                dialogController.isAppearanceLightNavigationBars = false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window?.setBackgroundBlurRadius(24)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = backdropAlpha)
                .background(viewerBarColor.copy(alpha = .98f))
                .clickable(interactionSource = backdropInteractionSource, indication = null, onClick = requestClose),
            contentAlignment = Alignment.Center
        ) {
            // This explicitly paints beneath the transparent, edge-to-edge
            // system status bar so the underlying diagram never shows through.
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(lightboxStatusBar)
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(.90f)
                    .fillMaxHeight(.64f)
                    .offset(y = (-63).dp)
                    .graphicsLayer(scaleX = imageScale, scaleY = imageScale)
                    .clickable(interactionSource = consumeTapInteractionSource, indication = null, onClick = {}),
                color = viewerSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, viewerOutline),
                shadowElevation = 24.dp
            ) {
                val pointerDragModifier = if (editingPointer) {
                    Modifier.pointerInput(screen.id, bitmap) {
                        var draggingMarker = false
                        fun normalize(position: Offset): Offset {
                            if (bitmap == null) return Offset(.5f, .5f)
                            val rect = fittedScreenshotRect(size.width.toFloat(), size.height.toFloat(), bitmap.width, bitmap.height)
                            return Offset(
                                ((position.x - rect.left) / rect.width).coerceIn(0f, 1f),
                                ((position.y - rect.top) / rect.height).coerceIn(0f, 1f)
                            )
                        }
                        detectDragGestures(
                            onDragStart = { start ->
                                draggingMarker = bitmap != null && fittedScreenshotRect(size.width.toFloat(), size.height.toFloat(), bitmap.width, bitmap.height).contains(start)
                                if (draggingMarker) pointerPosition = normalize(start)
                            }
                        ) { change, _ ->
                            if (draggingMarker) {
                                change.consume()
                                pointerPosition = normalize(change.position)
                            }
                        }
                    }
                } else Modifier
                val swipeModifier = if (!editingPointer && screenCount > 1) {
                    Modifier.pointerInput(screen.id, screenCount) {
                        var horizontalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { horizontalDrag = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                horizontalDrag += dragAmount
                            },
                            onDragEnd = {
                                if (kotlin.math.abs(horizontalDrag) >= 64f) {
                                    // Left advances; right returns to the previous screenshot.
                                    onSlide(if (horizontalDrag < 0f) 1 else -1)
                                }
                            }
                        )
                    }
                } else Modifier
                Box(Modifier.fillMaxSize().then(pointerDragModifier).then(swipeModifier)) {
                    if (bitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = bitmap,
                            contentDescription = "Expanded screenshot",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else if (screenshotState is ScreenshotBitmapState.Loading) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                color = viewerOnSurface,
                                strokeWidth = 3.dp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text("Loading screenshot…", color = viewerOnSurface, fontSize = 14.sp)
                        }
                    } else {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = UiColor.error, modifier = Modifier.size(30.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Screenshot unavailable", color = viewerOnSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text("The file was moved, deleted, or could not be read.", color = viewerMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    }
                    if (editingPointer) {
                        Canvas(Modifier.fillMaxSize()) {
                            val imageRect = if (bitmap == null) {
                                androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height)
                            } else fittedScreenshotRect(size.width, size.height, bitmap.width, bitmap.height)
                            val marker = Offset(
                                imageRect.left + imageRect.width * pointerPosition.x,
                                imageRect.top + imageRect.height * pointerPosition.y
                            )
                            // The positioning stencil makes the exact x/y location
                            // readable while the user drags the connection pointer.
                            drawLine(
                                pointerGuide,
                                Offset(marker.x, 0f),
                                Offset(marker.x, size.height),
                                strokeWidth = 1.5f
                            )
                            drawLine(
                                pointerGuide,
                                Offset(0f, marker.y),
                                Offset(size.width, marker.y),
                                strokeWidth = 1.5f
                            )
                            drawCircle(pointerShadow, 28f, marker)
                            drawCircle(pointerRing, 21f, marker)
                            drawCircle(pointerAccent, 12f, marker)
                            drawCircle(viewerOnSurface, 4f, marker)
                        }
                    }
                }
            }
            if (!editingPointer) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 25.dp, end = 20.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(onClick = requestClose),
                    shape = CircleShape,
                    color = viewerSurface.copy(alpha = .96f),
                    border = BorderStroke(1.dp, viewerOutline),
                    shadowElevation = 6.dp
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = viewerOnSurface,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
            if (!editingPointer && screenCount > 1) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-164).dp),
                    color = viewerSurfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Swipe to browse  •  ${screenPosition + 1} / $screenCount",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = viewerOnSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (editingPointer) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-164).dp),
                    color = viewerSurfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Drag the marker to adjust the pointer",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = viewerOnSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(.84f)
                    .height(84.dp)
                    .offset(y = (-72).dp)
                    .clickable(interactionSource = consumeTapInteractionSource, indication = null, onClick = {}),
                color = viewerSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, viewerOutline),
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editingPointer) {
                        LightboxAction(Icons.Default.Close, "Cancel", tint = viewerOnSurface) {
                            pointerPosition = Offset(screen.markerX, screen.markerY)
                            editingPointer = false
                        }
                        LightboxAction(Icons.Default.Check, "Save", tint = viewerOnSurface) {
                            onUpdatePointer(screen, pointerPosition.x, pointerPosition.y)
                            editingPointer = false
                        }
                    } else {
                    LightboxDrawableAction(R.drawable.ic_lightbox_share, "Share", onClick = {
                        runCatching {
                            val imageUri = if (screen.imagePath.startsWith("content://")) {
                                screen.imagePath.toUri()
                            } else FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                java.io.File(screen.imagePath)
                            )
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND)
                                        .setType("image/png")
                                        .putExtra(Intent.EXTRA_STREAM, imageUri)
                                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                                    "Share screenshot"
                                )
                            )
                        }.onFailure {
                            android.widget.Toast.makeText(context, "Unable to share screenshot", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }, tint = viewerOnSurface)
                    LightboxDrawableAction(
                        R.drawable.ic_lightbox_delete,
                        "Delete",
                        onClick = { showDeleteConfirmation = true },
                        tint = viewerOnSurface
                    )
                    LightboxDrawableAction(R.drawable.ic_lightbox_replace, "Replace", onClick = {
                        onReplace(screen)
                    }, tint = viewerOnSurface)
                    LightboxDrawableAction(R.drawable.ic_lightbox_edit, "Edit", onClick = {
                        editingPointer = true
                    }, tint = viewerOnSurface)
                    }
                }
            }
        }
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = viewerSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = Color(0xFF51252B)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("!", color = Color(0xFFFFA6AA), fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Delete screenshot?", color = viewerOnSurface, fontWeight = FontWeight.SemiBold)
                }
            },
            text = {
                Text(
                    "This screenshot will be permanently deleted. Are you sure you want to do this?",
                    color = viewerOnSurface,
                    fontSize = 17.sp
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = viewerMuted)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showDeleteConfirmation = false
                    onDelete(screen)
                    requestClose()
                }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = UiColor.error, contentColor = UiColor.onError)) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable private fun LightboxAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = UiColor.onSurface,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(60.dp).height(64.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = tint, modifier = Modifier.size(25.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, color = tint, fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = .1.sp)
    }
}

/** Renders the action artwork supplied as SVGs, packaged as vector drawables. */
@Composable private fun LightboxDrawableAction(
    @androidx.annotation.DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    tint: Color = UiColor.onSurface
) {
    Column(
        modifier = Modifier.width(60.dp).height(64.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(label, color = tint, fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = .1.sp)
    }
}

/** Computes fitted screenshot bounds once for drawing and hit testing. */
private fun diagramImageRects(
    screens: List<ScreenEntity>,
    bitmaps: Map<String, androidx.compose.ui.graphics.ImageBitmap>,
    width: Float,
    height: Float,
    margin: Float,
    gap: Float,
    rowGap: Float,
    columns: Int
): List<androidx.compose.ui.geometry.Rect> {
    val rows = (screens.size + columns - 1) / columns
    val nodeWidth = minOf(
        (width - margin * 2 - gap * (columns - 1)) / columns,
        (height - margin * 2 - rowGap * (rows - 1)) / (rows * 2.15f)
    ).coerceAtLeast(1f)
    val nodeHeight = nodeWidth * 2.15f
    val dimensions = screens.map { screen ->
        val image = bitmaps[screen.id]
        if (image == null) nodeWidth to nodeHeight else {
            val aspect = image.width.toFloat() / image.height.toFloat()
            val fittedHeight = minOf(nodeWidth / aspect, nodeHeight)
            fittedHeight * aspect to fittedHeight
        }
    }
    val rowTops = mutableListOf<Float>()
    var top = margin
    for (row in 0 until rows) {
        rowTops.add(top)
        top += dimensions.drop(row * columns).take(columns).maxOf { it.second } + rowGap
    }
    return screens.indices.map { index ->
        val row = index / columns
        val count = minOf(columns, screens.size - row * columns)
        val rowWidth = count * nodeWidth + (count - 1) * gap
        val (imageWidth, imageHeight) = dimensions[index]
        val left = (width - rowWidth) / 2f + (index % columns) * (nodeWidth + gap) + (nodeWidth - imageWidth) / 2f
        androidx.compose.ui.geometry.Rect(left, rowTops[row], left + imageWidth, rowTops[row] + imageHeight)
    }
}

/** Draws screenshot nodes and the persisted marker-to-next-screen transitions. */
@Composable private fun FlowDiagramCanvas(
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    bitmaps: Map<String, androidx.compose.ui.graphics.ImageBitmap>,
    showMobileStencils: Boolean,
    connectorColor: Color,
    modifier: Modifier = Modifier,
    columnsOverride: Int? = null,
    onScreenClick: (ScreenEntity) -> Unit = {}
) {
    val pointerRingColor = Color.White
    Canvas(
    modifier.pointerInput(screens, bitmaps, columnsOverride) {
        detectTapGestures { tap ->
            if (screens.isEmpty()) return@detectTapGestures
            val columns = (columnsOverride ?: 5).coerceIn(1, screens.size)
            val imageRects = diagramImageRects(
                screens, bitmaps, size.width.toFloat(), size.height.toFloat(),
                10.dp.toPx(), 8.dp.toPx(), maxOf(12.dp.toPx(), 22f), columns
            )
            imageRects.indexOfFirst { it.contains(tap) }
                .takeIf { it >= 0 }
                ?.let { onScreenClick(screens[it]) }
        }
    }
) {
    val margin = 10.dp.toPx()
    val columns = (columnsOverride ?: 5).coerceIn(1, screens.size)
    val imageRects = diagramImageRects(
        screens, bitmaps, size.width, size.height,
        margin, 8.dp.toPx(), maxOf(12.dp.toPx(), 22f), columns
    )
    val nodeById = screens.withIndex().associate { it.value.id to it.index }
    val visibleTransitions = if (showMobileStencils) transitions else emptyList()

    screens.forEachIndexed { index, screen ->
        val imageRect = imageRects[index]
        bitmaps[screen.id]?.let { image ->
            drawImage(
                image = image,
                dstOffset = androidx.compose.ui.unit.IntOffset(imageRect.left.toInt(), imageRect.top.toInt()),
                dstSize = androidx.compose.ui.unit.IntSize(imageRect.width.toInt(), imageRect.height.toInt()),
                filterQuality = FilterQuality.High
            )
        }
    }

    // Render the path again above the bitmap nodes. A capture marker represents
    // the exact place a user tapped, so the complete path must remain visible
    // over the originating screenshot instead of disappearing behind it.
    visibleTransitions.forEach { transition ->
        val fromIndex = nodeById[transition.fromScreenId] ?: return@forEach
        val toIndex = nodeById[transition.toScreenId] ?: return@forEach
        val source = imageRects[fromIndex]
        val destination = imageRects[toIndex]
        val start = Offset(
            source.left + source.width * transition.fromMarkerX,
            source.top + source.height * transition.fromMarkerY
        )
        val sameRow = fromIndex / columns == toIndex / columns
        val end = if (sameRow) {
            Offset(destination.left, destination.top + destination.height * .5f)
        } else {
            Offset(destination.left + destination.width * .5f, destination.top)
        }
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(start.x, start.y)
            if (sameRow) {
                val laneX = (start.x + end.x) / 2f
                lineTo(laneX, start.y)
                lineTo(laneX, end.y)
            } else {
                val laneX = size.width - margin
                val laneY = end.y - 20f
                lineTo(laneX, start.y)
                lineTo(laneX, laneY)
                lineTo(end.x, laneY)
            }
            lineTo(end.x, end.y)
        }
        drawPath(path, connectorColor, style = Stroke(2.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round, pathEffect = androidx.compose.ui.graphics.PathEffect.cornerPathEffect(8f)))
        drawCircle(connectorColor, 5f, start)
        if (sameRow) {
            drawLine(connectorColor, end, end + Offset(-8f, -5f), 2.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(connectorColor, end, end + Offset(-8f, 5f), 2.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        } else {
            drawLine(connectorColor, end, end + Offset(-5f, -8f), 2.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(connectorColor, end, end + Offset(5f, -8f), 2.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }

    // Draw saved transition points last so they remain visible *on* their
    // originating screenshot. The location is the same normalized coordinate
    // the user positioned on the Keep + Next / Keep Finish review screen.
    visibleTransitions.forEach { transition ->
        val fromIndex = nodeById[transition.fromScreenId] ?: return@forEach
        val source = imageRects[fromIndex]
        val point = Offset(
            source.left + source.width * transition.fromMarkerX,
            source.top + source.height * transition.fromMarkerY
        )
        drawCircle(pointerRingColor, 7f, point)
        drawCircle(connectorColor, 4f, point)
    }
    }
}
