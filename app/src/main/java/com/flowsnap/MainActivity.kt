package com.flowsnap

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.Intent
import android.content.ContentValues
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class CaptureReview(val flowId: String, val path: String, val markerX: Float, val markerY: Float)

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
            startActivity(Intent(this@MainActivity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        restoreReview(savedInstanceState)
        acceptReviewIntent(intent)
        ContextCompat.registerReceiver(this, captureReadyReceiver, IntentFilter(CaptureService.ACTION_CAPTURE_READY), ContextCompat.RECEIVER_NOT_EXPORTED)
        setContent { MaterialTheme(typography = FlowSnapTypography) { FlowSnapApp(review = review, dismissReview = { review = null }) } }
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

class FlowViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val dao = FlowDatabase.create(application).flowDao()
    private val _exportInProgress = MutableStateFlow(false)
    val exportInProgress: StateFlow<Boolean> = _exportInProgress.asStateFlow()
    // Keep the gallery query alive for the app lifetime. This prevents a
    // transient empty Home grid when returning from a completed capture.
    val flows = dao.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun createFlow(open: (FlowEntity) -> Unit) = viewModelScope.launch { val now = System.currentTimeMillis(); val flow = FlowEntity(UUID.randomUUID().toString(), "My new app flow", now, now); dao.upsert(flow); open(flow) }
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
        dao.upsert(updated)
        onSaved(updated)
    }
    fun updateDiagramAppearance(
        flow: FlowEntity,
        showMobileStencils: Boolean,
        connectorColor: Long,
        onSaved: (FlowEntity) -> Unit
    ) = viewModelScope.launch {
        val updated = flow.copy(
            showMobileStencils = showMobileStencils,
            connectorColor = connectorColor,
            updatedAt = System.currentTimeMillis()
        )
        dao.upsert(updated)
        onSaved(updated)
    }
    private suspend fun persistCapture(review: CaptureReview) = dao.persistCapture(
        flowId = review.flowId,
        imagePath = review.path,
        markerX = review.markerX,
        markerY = review.markerY,
        now = System.currentTimeMillis()
    )
    fun keepCapture(review: CaptureReview) = viewModelScope.launch { persistCapture(review) }
    fun keepCaptureAndOpenDiagram(review: CaptureReview, onSaved: (FlowEntity?) -> Unit) = viewModelScope.launch {
        persistCapture(review)
        onSaved(dao.flow(review.flowId))
    }
    fun deleteFlow(flow: FlowEntity, onDeleted: () -> Unit) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        // Commit the cascaded Room delete first. Any failed file deletes are removed by startup reconciliation.
        val paths = dao.deleteFlowAndGetImagePaths(flow.id)
        paths.forEach { path -> runCatching { java.io.File(path).delete() } }
        ScreenshotStorage.deleteFlowFolder(getApplication(), flow.id)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onDeleted() }
    }
    fun screens(flowId: String) = dao.observeScreens(flowId)
    fun transitions(flowId: String) = dao.observeTransitions(flowId)
    fun flow(flowId: String) = dao.observeFlow(flowId)

    fun cleanupOrphanedCaptures(pendingReviewPath: String?) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        ScreenshotStorage.deleteUnreferencedFiles(getApplication(), dao.referencedImagePaths().toSet() + listOfNotNull(pendingReviewPath))
    }

    private fun <T> launchExport(work: () -> Result<T>, onResult: (Result<T>) -> Unit) =
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _exportInProgress.value = true
            val result = try {
                work()
            } catch (error: Throwable) {
                Result.failure(error)
            }
            _exportInProgress.value = false
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(result) }
        }

    fun exportPdf(destination: Uri, screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Unit>) -> Unit) =
        launchExport({ writeFlowPdf(getApplication(), destination, screens, transitions, flow) }, onResult)
    fun exportJpeg(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Uri>) -> Unit) =
        launchExport({ writeFlowJpeg(getApplication(), screens, transitions, flow) }, onResult)
    fun exportPng(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Uri>) -> Unit) =
        launchExport({ writeFlowPng(getApplication(), screens, transitions, flow) }, onResult)
    fun exportDrawio(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Uri>) -> Unit) =
        launchExport({ writeFlowDrawio(getApplication(), screens, transitions, flow) }, onResult)
    fun exportSvg(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Uri>) -> Unit) =
        launchExport({ writeFlowSvg(getApplication(), screens, transitions, flow) }, onResult)
    fun exportSketch(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, onResult: (Result<Uri>) -> Unit) =
        launchExport({ writeFlowSketch(getApplication(), screens, transitions, flow) }, onResult)

}

@Composable private fun FlowSnapApp(review: CaptureReview?, dismissReview: () -> Unit, vm: FlowViewModel = viewModel()) {
    var selectedFlowId by rememberSaveable { mutableStateOf<String?>(null) }
    androidx.compose.runtime.LaunchedEffect(review?.path) { vm.cleanupOrphanedCaptures(review?.path) }
    if (review != null) CaptureReviewScreen(review, vm, dismissReview, onFinish = { savedFlow ->
        if (savedFlow != null) selectedFlowId = savedFlow.id
        dismissReview()
    }) else if (selectedFlowId == null) {
        HomeScreen(vm) { selectedFlowId = it.id }
    } else {
        // The preceding null branch guarantees this route has an id.
        val flowId = requireNotNull(selectedFlowId)
        DiagramScreen(flowId, vm) { selectedFlowId = null }
    }
}

@Composable private fun HomeScreen(vm: FlowViewModel, openFlow: (FlowEntity) -> Unit) {
    val flows by vm.flows.collectAsStateWithLifecycle()
    // Keep gallery cards above the Android gesture/three-button navigation area.
    Column(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).windowInsetsPadding(WindowInsets.systemBars)) {
        Row(Modifier.fillMaxWidth().height(64.dp).background(Color.White).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Menu, "Menu", Modifier.size(28.dp), tint = Color(0xFF656565))
            Spacer(Modifier.width(20.dp))
            Text("Screenshot", color = Color(0xFF274786), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Light, maxLines = 1)
            Text(" Flow", color = Color(0xFF5A91B8), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Light, maxLines = 1)
            Spacer(Modifier.weight(1f))
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CreateFlowTile(Modifier.fillMaxWidth().aspectRatio(.72f)) {
                    vm.createFlow(openFlow)
                }
            }
            items(flows, key = { it.id }) { flow ->
                FlowTile(flow, vm) { openFlow(flow) }
            }
        }
    }
}

@Composable private fun CreateFlowTile(modifier: Modifier, onClick: () -> Unit) = Card(onClick = onClick, modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF344B93))) { Box(Modifier.fillMaxSize().padding(18.dp)) { Text("+", Modifier.align(Alignment.Center), color = Color.White, style = MaterialTheme.typography.displayLarge.copy(fontSize = 94.sp), fontWeight = FontWeight.Light); Text("Create new diagram", Modifier.align(Alignment.BottomStart), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false) } }

@Composable private fun FlowTile(flow: FlowEntity, vm: FlowViewModel, onClick: () -> Unit) {
    val screens by vm.screens(flow.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val transitions by vm.transitions(flow.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val bitmaps = rememberScreenBitmaps(screens, maxDimension = 360)
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(.72f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(10.dp).fillMaxSize()) {
            Column(
                Modifier.weight(1f).fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(1.dp))
                    .padding(4.dp)
            ) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                if (screens.isNotEmpty()) {
                    FlowDiagramCanvas(
                        screens = screens,
                        transitions = transitions,
                        bitmaps = bitmaps,
                        showMobileStencils = flow.showMobileStencils,
                        // FlowEntity stores a normal ARGB value. Use the Int overload;
                        // the ULong overload expects Compose's internal packed Color format.
                        connectorColor = Color(flow.connectorColor.toInt()),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                }
                Row(
                    Modifier.fillMaxWidth().height(15.dp)
                        .border(1.dp, Color(0xFFE6E6E6), RoundedCornerShape(0.dp))
                        .padding(horizontal = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(flow.name, fontSize = 5.sp, color = Color(0xFF555555), maxLines = 1)
                    Text("v1.0", fontSize = 5.sp, color = Color(0xFF555555))
                    Text("You", fontSize = 5.sp, color = Color(0xFF555555))
                }
            }
            Spacer(Modifier.height(9.dp))
            Text(
                flow.name,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF314A87),
                fontSize = 17.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable private fun CaptureReviewScreen(review: CaptureReview, vm: FlowViewModel, dismiss: () -> Unit, onFinish: (FlowEntity?) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val returnToCapturedApp = {
        val activity = context as? Activity
        // Re-attach the overlay before leaving FlowSnap. ACTION_RESUME never
        // opens HOME: Android then exposes the task that was behind this
        // review activity (the exact app/screen the user was capturing).
        context.startService(Intent(context, CaptureService::class.java).setAction(CaptureService.ACTION_RESUME))
        dismiss()
        // Let Compose remove the review destination first. Moving the task on
        // the next UI frame avoids a race that can otherwise leave FlowSnap in
        // front of the previous external app on newer Android releases.
        activity?.window?.decorView?.postDelayed({
            activity.moveTaskToBack(true)
        }, 180L)
    }
    val bitmap = rememberScreenBitmaps(listOf(ScreenEntity("review", review.flowId, review.path, 0f, 0f, 0, 0L)), maxDimension = 1600)["review"]
    var draggedMarker by remember { mutableStateOf<Offset?>(null) }
    var reviewCanvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    // A user can reposition the marker on this review screen. Persist that
    // final normalized location, rather than the original overlay location.
    val reviewedCapture = draggedMarker?.let { marker ->
        if (reviewCanvasSize.width > 0 && reviewCanvasSize.height > 0) {
            review.copy(
                markerX = (marker.x / reviewCanvasSize.width).coerceIn(0f, 1f),
                markerY = (marker.y / reviewCanvasSize.height).coerceIn(0f, 1f)
            )
        } else review
    } ?: review
    // Keep the review actions above Android's gesture/navigation area.
    val primaryBlue = Color(0xFF24519B)
    Column(
        Modifier.fillMaxSize().background(Color(0xFFF4F6F9)).windowInsetsPadding(WindowInsets.systemBars),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.fillMaxWidth().height(52.dp).background(Color.White))
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
          Surface(
              // Preserve the original review-canvas bounds so the screenshot,
              // marker, and connector retain their established placement.
              modifier = Modifier.fillMaxWidth().fillMaxSize().padding(horizontal = 38.dp, vertical = 18.dp),
              color = Color(0xFFE9EEF5),
              shape = RoundedCornerShape(20.dp),
              shadowElevation = 8.dp
          ) {
           Box(Modifier.fillMaxSize()) {
            if (bitmap != null) androidx.compose.foundation.Image(bitmap, "Captured screen", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            Canvas(Modifier.fillMaxSize().onSizeChanged { reviewCanvasSize = it }.pointerInput(Unit) { detectDragGestures(onDragStart = { draggedMarker = it }) { change, amount -> change.consume(); draggedMarker = (draggedMarker ?: change.position) + amount } }) {
                val marker = draggedMarker ?: Offset(size.width * review.markerX, size.height * review.markerY)
                // Keep the transition marker deliberately minimal: no label or
                // coloured halo over the captured screen.
                drawCircle(Color.White, 26f, marker)
                drawCircle(Color(0xFF4D4D4D), 9f, marker)
                drawLine(Color(0xFF5C5C5C), marker, Offset(size.width, marker.y), 6f)
            }
           }
          }
        }
        Surface(color = Color.White, shadowElevation = 4.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { java.io.File(review.path).delete(); returnToCapturedApp() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFB3261E)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB3261E))
                ) { Text("DISCARD", fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                Button(
                    onClick = { vm.keepCapture(reviewedCapture); returnToCapturedApp() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = Color.White)
                ) { Text("KEEP + NEXT", fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                Button(
                    onClick = {
                        context.startService(Intent(context, CaptureService::class.java).setAction(CaptureService.ACTION_STOP))
                        vm.keepCaptureAndOpenDiagram(reviewedCapture, onFinish)
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = Color.White)
                ) { Text("KEEP FINISH", fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
            }
        }
    }
}

@Composable private fun DiagramScreen(flowId: String, vm: FlowViewModel, goBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tipPreferences = remember { context.getSharedPreferences("flowsnap", Context.MODE_PRIVATE) }
    val projectionManager = remember { context.getSystemService(MediaProjectionManager::class.java) }
    val currentFlow by vm.flow(flowId).collectAsStateWithLifecycle(initialValue = null)
    val screens by vm.screens(flowId).collectAsStateWithLifecycle(initialValue = emptyList())
    val transitions by vm.transitions(flowId).collectAsStateWithLifecycle(initialValue = emptyList())
    val exportInProgress by vm.exportInProgress.collectAsStateWithLifecycle()
    // Room emits the supplied null initial value before its first query result.
    // Navigating Home from that transient state made recent-diagram taps flash.
    if (currentFlow == null) {
        Box(Modifier.fillMaxSize().background(Color(0xFFF1F1F1)))
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
    var exportSketchAfterPermission by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showDiagramSettings by remember { mutableStateOf(false) }
    var showExportFormats by remember { mutableStateOf(false) }
    val exportPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { destination ->
            vm.exportPdf(destination, screens, transitions, flow) { result ->
                result.onSuccess {
                    android.widget.Toast.makeText(context, "PDF saved", android.widget.Toast.LENGTH_SHORT).show()
                    openPdfPreview(context, destination)
                }.onFailure { error ->
                    android.util.Log.e("FlowSnapPdf", "PDF export failed", error)
                    android.widget.Toast.makeText(context, "Could not save PDF: ${error.message ?: "unknown error"}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    val startJpegExport = {
        vm.exportJpeg(screens, transitions, flow) { result ->
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
        vm.exportPng(screens, transitions, flow) { result ->
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
    val startSketchExport = {
        vm.exportSvg(screens, transitions, flow) { result ->
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
    // The bottom controls must stay inside the safe area on both gesture and
    // three-button navigation devices.
    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars).diagramGrid()) {
        // The diagram is one tall document: screenshots flow downward in five
        // columns and this area scrolls independently of the fixed controls.
        // The padding keeps document content clear of those controls.
        EditorCircle(Icons.Default.Home, "Home", goBack, modifier = Modifier.align(Alignment.TopStart).padding(20.dp))
        EditorCircle(Icons.Default.Settings, "Settings", onClick = { showDiagramSettings = true }, enabled = hasScreens, modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp))
        if (screens.isEmpty()) Text("Create a diagram with the + option below", Modifier.align(Alignment.Center), color = Color(0xFF24519B), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        else {
            DiagramPreview(
                screens = screens,
                transitions = transitions,
                flow = flow,
                // The white card itself stays in this protected viewport. Only
                // its document content scrolls, so its rounded top and bottom
                // corners remain visible at every scroll position.
                modifier = Modifier.align(Alignment.TopCenter)
                    .padding(top = 108.dp, bottom = 190.dp)
                    .fillMaxWidth(.90f)
                    .fillMaxHeight()
            )
        }
        Row(Modifier.align(Alignment.BottomStart).padding(22.dp), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
            EditorCircle(Icons.Default.Save, "Save", onClick = {
                showExportFormats = true
            }, enabled = hasScreens)
            EditorCircle(Icons.Default.CloudUpload, "Export PDF", onClick = { exportPdf.launch("${flow.name}.pdf") }, enabled = hasScreens)
            EditorCircle(Icons.Default.Delete, "Delete diagram", onClick = { showDeleteConfirmation = true })
        }
        Column(Modifier.align(Alignment.BottomEnd).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.End) {
            EditorCircle(Icons.Default.Add, "Capture", beginCapture, large = true, enabled = !captureLaunchInProgress)
        }
    }
    if (showDiagramSettings) {
        DiagramSettingsDialog(
            flow = flow,
            onDismiss = { showDiagramSettings = false },
            onSave = { title, version, createdBy, showTitle, showVersion, showCreatedAt, showCreatedBy, showAppVersion, showMobileStencils, connectorColor ->
                    vm.updateFlowMetadata(flow, title, version, createdBy, showTitle, showVersion, showCreatedAt, showCreatedBy, showAppVersion) { savedFlow ->
                    vm.updateDiagramAppearance(savedFlow, showMobileStencils, connectorColor) { showDiagramSettings = false }
                }
            }
        )
    }
    if (showExportFormats) {
        ExportFormatDialog(
            onDismiss = { showExportFormats = false },
            onExportPdf = {
                showExportFormats = false
                exportPdf.launch("${flow.name}.pdf")
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
    if (exportInProgress) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp,
                        color = Color(0xFF24519B)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Saving export…",
                        color = Color(0xFF202B3B),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠", color = Color(0xFFE8D372), fontSize = 32.sp)
                    Spacer(Modifier.width(12.dp))
                    Text("Deleting diagram", color = Color(0xFF263F7D), fontWeight = FontWeight.Medium)
                }
            },
            text = {
                Text(
                    "Your diagram will be permanently deleted. Are you sure you want to do this?",
                    color = Color(0xFF222222), fontSize = 17.sp
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("CANCEL", color = Color(0xFF263F7D))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteFlow(flow) {
                        showDeleteConfirmation = false
                        goBack()
                    }
                }) { Text("OK", color = Color(0xFF263F7D)) }
            }
        )
    }
    if (showTips) CaptureTipDialog(onConfirm = { dontShowAgain -> if (dontShowAgain) tipPreferences.edit { putBoolean("hide_capture_tip", true) }; val result = pendingResult!!; ContextCompat.startForegroundService(context, Intent(context, CaptureService::class.java).putExtra(CaptureService.EXTRA_FLOW_ID, flowId).putExtra(CaptureService.EXTRA_RESULT_CODE, result.resultCode).putExtra(CaptureService.EXTRA_RESULT_DATA, result.data)); showTips = false })
}

@Composable private fun CaptureTipDialog(onConfirm: (Boolean) -> Unit) {
    val primaryBlue = Color(0xFF24519B)
    var dontShowAgain by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        titleContentColor = Color(0xFF1F2D43),
        textContentColor = Color(0xFF66758A),
        title = {
            Text("Tips to get you started", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("A floating widget will appear on your screen.", fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(4.dp))
                Text("Drag it where you want the arrow to go.", fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(16.dp))
                DragTapIllustration(primaryBlue)
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it },
                        modifier = Modifier.size(26.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = primaryBlue,
                            uncheckedColor = primaryBlue,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Don't show this again", color = Color(0xFF334155), fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(dontShowAgain) }) {
                Text("OK", color = primaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    )
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
    val primaryBlue = Color(0xFF24519B)
    var selected by remember { mutableStateOf("JPEG") }
    val formats = listOf(
        "JPEG" to "Static uneditable JPEG (Joint Photographic Experts Group) image",
        "PNG" to "Portable Network Graphics image",
        "PDF" to "Portable Document Format",
        "SVG / FIGMA (.svg)" to "Figma-compatible SVG with embedded screenshots and editable arrows"
    )
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(.88f),
            color = Color.White,
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F2)),
            shadowElevation = 14.dp
        ) {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 26.dp)) {
                Text(
                    "EXPORT DIAGRAM",
                    color = Color(0xFF1F2D43),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Choose a format for your flow",
                    color = Color(0xFF6A778C),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(18.dp))
                formats.forEach { (format, description) ->
                    val isSelected = selected == format
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(if (isSelected) Color(0x0D24519B) else Color.Transparent, RoundedCornerShape(16.dp))
                            .clickable { selected = format }
                            .padding(horizontal = 12.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExportFormatRadio(selected = isSelected, primaryBlue = primaryBlue)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(format, color = Color(0xFF202B3B), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            if (isSelected) {
                                Spacer(Modifier.height(3.dp))
                                Text(description, color = Color(0xFF708096), fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 22.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss) { Text("CANCEL", color = primaryBlue, fontWeight = FontWeight.SemiBold) }
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
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue, contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp, vertical = 12.dp)
                    ) { Text("SAVE AS", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun ExportFormatRadio(selected: Boolean, primaryBlue: Color) {
    Box(
        modifier = Modifier.size(28.dp)
            .background(if (selected) Color(0x1A24519B) else Color.Transparent, CircleShape)
            .border(2.dp, if (selected) primaryBlue else Color(0xFF7A8493), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) Box(Modifier.size(12.dp).background(primaryBlue, CircleShape))
    }
}

/** Diagram label and appearance settings, opened by the editor gear button. */
@Composable private fun DiagramSettingsDialog(
    flow: FlowEntity,
    onDismiss: () -> Unit,
    onSave: (title: String, version: String, createdBy: String, showTitle: Boolean, showVersion: Boolean, showCreatedAt: Boolean, showCreatedBy: Boolean, showAppVersion: Boolean, showMobileStencils: Boolean, connectorColor: Long) -> Unit
) {
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
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        // Keep the canvas visible without the heavy, muddy system dim overlay.
        val dialogView = LocalView.current
        SideEffect {
            (dialogView.parent as? DialogWindowProvider)?.window?.setDimAmount(0.12f)
        }
        Surface(
            // Wrap content naturally so the actions follow the swatches instead
            // of being pushed to the bottom of a near-full-screen dialog.
            modifier = Modifier.fillMaxWidth(.90f),
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E7EF)),
            shadowElevation = 12.dp
        ) {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                Text("DIAGRAM LABELS", color = Color(0xFF1F2D43), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE3E7EF)))
                Spacer(Modifier.height(10.dp))
                EditableDiagramSetting("Title", title, { title = it }, showTitle) { showTitle = !showTitle }
                EditableDiagramSetting("Diagram Version", version, { version = it }, showVersion) { showVersion = !showVersion }
                ReadOnlyDiagramSetting("Created At", java.text.SimpleDateFormat("yyyy MMM dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(flow.createdAt)), showCreatedAt) { showCreatedAt = !showCreatedAt }
                EditableDiagramSetting("Created By", createdBy, { createdBy = it }, showCreatedBy) { showCreatedBy = !showCreatedBy }
                ReadOnlyDiagramSetting("App Version", "v1.0", showAppVersion) { showAppVersion = !showAppVersion }

                Spacer(Modifier.height(2.dp))
                Text("DIAGRAM LOOK & FEEL", color = Color(0xFF1F2D43), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE3E7EF)))
        Spacer(Modifier.height(5.dp))
                AppearanceToggle(
                    checked = showMobileStencils,
                    onCheckedChange = { showMobileStencils = it }
                )
        Spacer(Modifier.height(5.dp))
                ConnectorColorSetting(
                    selectedColor = connectorColor,
                    onColorSelected = { connectorColor = it }
                )
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(end = 14.dp)) {
                        Text("CANCEL", color = Color(0xFF5E6F8E), fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { onSave(title, version, createdBy, showTitle, showVersion, showCreatedAt, showCreatedBy, showAppVersion, showMobileStencils, connectorColor) }) {
                        Text("SAVE", color = Color(0xFF274D89), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable private fun EditableDiagramSetting(label: String, value: String, onValueChange: (String) -> Unit, included: Boolean, toggleIncluded: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = Color(0xFF53627A), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(3.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.weight(1f).height(44.dp)
                    .border(1.dp, Color(0xFFD2D8E2), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        color = Color(0xFF202B3B)
                    )
                )
            }
            Spacer(Modifier.width(10.dp))
            IncludedMark(included, toggleIncluded)
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable private fun ReadOnlyDiagramSetting(label: String, value: String, included: Boolean, toggleIncluded: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = Color(0xFF53627A), fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(3.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.weight(1f).height(44.dp)
                    .border(1.dp, Color(0xFFD2D8E2), RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(value, fontSize = 15.sp, color = Color(0xFF202B3B), maxLines = 1)
            }
            Spacer(Modifier.width(10.dp))
            IncludedMark(included, toggleIncluded)
        }
    }
    Spacer(Modifier.height(4.dp))
}

/** Controls whether the transition path and the saved tap marker are visible. */
@Composable private fun AppearanceToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(36.dp)
                .border(1.5.dp, Color(0xFF647A9E), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = "Mobile stencil",
                tint = Color(0xFF315789),
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Show mobile stencils", color = Color(0xFF202B3B), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("Show connection lines and pointers", color = Color(0xFF63728A), fontSize = 11.sp)
        }
        IncludedMark(checked) { onCheckedChange(!checked) }
    }
}

/** A compact preset picker keeps connector colors readable and consistent. */
@Composable private fun ConnectorColorSetting(selectedColor: Long, onColorSelected: (Long) -> Unit) {
    val colors = listOf(
        0xFF69B83AL to "Green",
        0xFF24519BL to "Blue",
        0xFFE05252L to "Red",
        0xFF4D4D4DL to "Charcoal",
        0xFFF39C12L to "Orange",
        0xFF8E44ADL to "Purple",
        0xFF00A6A6L to "Teal",
        0xFFE91E63L to "Pink"
    )
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).background(Color(selectedColor.toInt()), CircleShape))
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Color of arrows", color = Color(0xFF202B3B), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("#%06X".format(selectedColor and 0xFFFFFF), color = Color(0xFF63728A), fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        // Eight compact swatches fit in one horizontal line, leaving room for
        // the dialog actions at the bottom on normal phone screens.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colors.forEach { (color, _) ->
                val selected = color == selectedColor
                Box(
                    Modifier.size(25.dp)
                        .background(Color(color.toInt()), CircleShape)
                        .border(if (selected) 2.5.dp else 1.dp, if (selected) Color(0xFF1F2D43) else Color(0xFFD2D8E2), CircleShape)
                        .clickable { onColorSelected(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable private fun IncludedMark(included: Boolean, onClick: (() -> Unit)? = null) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val borderColor by animateColorAsState(
        targetValue = when {
            included -> Color(0xFF5D79A8)
            hovered -> Color(0xFF627A9D)
            else -> Color(0xFF9AA6B8)
        },
        animationSpec = tween(durationMillis = 150),
        label = "checkboxBorder"
    )
    val backgroundColor by animateColorAsState(
        targetValue = when {
            included -> Color(0xFFEAF0FA)
            hovered -> Color(0x14314A87)
            else -> Color.White
        },
        animationSpec = tween(durationMillis = 150),
        label = "checkboxBackground"
    )
    Box(
        Modifier.size(30.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .hoverable(interactionSource)
            // The default indication draws the chunky grey block seen in the old UI.
            .clickable(interactionSource = interactionSource, indication = null, enabled = onClick != null) { onClick?.invoke() },
        contentAlignment = Alignment.Center
    ) { if (included) Text("✓", color = Color(0xFF315789), fontSize = 20.sp, fontWeight = FontWeight.Bold) }
}

@Composable private fun DragTapIllustration(primaryBlue: Color) = Column(
    Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text("↘     ↙", color = primaryBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Box(
        Modifier.size(82.dp)
            .background(Color(0xFFF1F6FD), CircleShape)
            .border(1.5.dp, primaryBlue, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(58.dp).background(primaryBlue, CircleShape), contentAlignment = Alignment.Center) {
            Text("Drag\n& tap", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth()) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("CAPTURE", color = primaryBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Tap to take a screenshot", color = Color(0xFF66758A), fontSize = 10.sp, textAlign = TextAlign.Center)
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("RETURN", color = primaryBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Tap ✓ to return to FlowSnap", color = Color(0xFF66758A), fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

private fun Modifier.diagramGrid() = this.drawBehind {
    drawRect(Color(0xFFF1F1F1))
    val spacing = 105f
    var x = 0f; while (x <= size.width) { drawLine(Color.White, Offset(x, 0f), Offset(x, size.height), 3f); x += spacing }
    var y = 0f; while (y <= size.height) { drawLine(Color.White, Offset(0f, y), Offset(size.width, y), 3f); y += spacing }
}

/**
 * Decodes only the pixels an export can actually display. Screenshot captures
 * are commonly 1080 x 2400, while an export node is usually 300px wide.
 */
private const val EDITABLE_EXPORT_IMAGE_WIDTH = 720
private const val EDITABLE_EXPORT_IMAGE_HEIGHT = 1600

private fun decodeExportBitmap(path: String, maxWidth: Int, maxHeight: Int): android.graphics.Bitmap? {
    if (maxWidth <= 0 || maxHeight <= 0) return null
    val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    android.graphics.BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (
        bounds.outWidth / (sampleSize * 2) >= maxWidth &&
        bounds.outHeight / (sampleSize * 2) >= maxHeight
    ) {
        sampleSize *= 2
    }
    val decoded = android.graphics.BitmapFactory.decodeFile(
        path,
        android.graphics.BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
    ) ?: return null
    if (decoded.width <= maxWidth && decoded.height <= maxHeight) return decoded

    val scale = minOf(maxWidth.toFloat() / decoded.width, maxHeight.toFloat() / decoded.height)
    val scaled = android.graphics.Bitmap.createScaledBitmap(
        decoded,
        (decoded.width * scale).toInt().coerceAtLeast(1),
        (decoded.height * scale).toInt().coerceAtLeast(1),
        true
    )
    decoded.recycle()
    return scaled
}

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
                val columns = minOf(7, pageScreens.size)
                val rows = (pageScreens.size + columns - 1) / columns
                val margin = 48f
                val metadataHeight = 132f
                val gap = 18f
                val availableHeight = 1200f - margin * 2 - metadataHeight - gap * (rows - 1)
                val nodeWidth = minOf(
                    (1200f - margin * 2 - gap * (columns - 1)) / columns,
                    availableHeight / (rows * 2.15f)
                )
                val nodeHeight = nodeWidth * 2.15f
                // Height can constrain node size on long flows. Center the resulting
                // widest row instead of retaining the fixed left export margin.
                val diagramWidth = columns * nodeWidth + (columns - 1) * gap
                val startX = ((pageInfo.pageWidth - diagramWidth) / 2f).coerceAtLeast(margin)
                val positions = List(pageScreens.size) { index ->
                    val row = index / columns
                    val column = index % columns
                    android.graphics.RectF(
                        startX + column * (nodeWidth + gap),
                        margin + row * (nodeHeight + gap),
                        startX + column * (nodeWidth + gap) + nodeWidth,
                        margin + row * (nodeHeight + gap) + nodeHeight
                    )
                }
                val indexes = pageScreens.withIndex().associate { it.value.id to it.index }
                pageScreens.forEachIndexed { index, screen ->
                    val target = positions[index]
                    // Keep multiple source pixels per PDF pixel. PdfDocument can
                    // then downsample smoothly instead of embedding a tiny preview.
                    decodeExportBitmap(screen.imagePath, (target.width() * 3).toInt(), (target.height() * 3).toInt())?.let { bitmap ->
                        val scale = minOf(target.width() / bitmap.width, target.height() / bitmap.height)
                        val width = bitmap.width * scale
                        val height = bitmap.height * scale
                        val imageRect = android.graphics.RectF(target.centerX() - width / 2, target.centerY() - height / 2, target.centerX() + width / 2, target.centerY() + height / 2)
                        canvas.drawBitmap(bitmap, null, imageRect, paint)
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
            val output = context.contentResolver.openOutputStream(destination)
                ?: error("Unable to open the selected PDF destination")
            output.use { stream -> document.writeTo(stream) }
        } finally {
            document.close()
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
    val columns = minOf(5, screens.size)
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
            decodeExportBitmap(screen.imagePath, (target.width() * 2).toInt(), (target.height() * 2).toInt())?.let { source ->
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
        val displayName = flow.name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "flowsnap" } + ".jpg"
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
    val columns = minOf(5, screens.size)
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
            decodeExportBitmap(screen.imagePath, (target.width() * 2).toInt(), (target.height() * 2).toInt())?.let { source ->
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
        val displayName = flow.name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "flowsnap" } + ".png"
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
    flow: FlowEntity
): Result<Uri> = runCatching {
    require(screens.isNotEmpty()) { "The diagram has no screenshots" }
    val columns = minOf(5, screens.size)
    val margin = 40f
    val gap = 28f
    val nodeWidth = 300f
    val nodeHeight = 645f
    val connectorColor = String.format(java.util.Locale.US, "#%06X", flow.connectorColor and 0xFFFFFF)
    val screenIndex = screens.withIndex().associate { it.value.id to it.index }
    val resolver = context.contentResolver
    val displayName = flow.name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "flowsnap" } + ".drawio"
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
    val uri = resolver.insert(collection, values) ?: error("Unable to create Draw.io file in MediaStore")
    try {
        resolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            // Stream each 720px high-density preview immediately. Keeping all Base64 images in a
            // StringBuilder caused large diagrams to stall during garbage collection.
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            writer.write("<mxfile host=\"app.diagrams.net\" modified=\"${System.currentTimeMillis()}\" agent=\"FlowSnap\" version=\"24.7.17\">\n")
            writer.write("  <diagram id=\"${xmlEscape(flow.id)}\" name=\"${xmlEscape(flow.name)}\">\n")
            writer.write("    <mxGraphModel dx=\"1200\" dy=\"800\" grid=\"1\" gridSize=\"10\" guides=\"1\" tooltips=\"1\" connect=\"1\" arrows=\"1\" fold=\"1\" page=\"1\" pageScale=\"1\" pageWidth=\"1169\" pageHeight=\"827\" math=\"0\" shadow=\"0\">\n")
            writer.write("      <root><mxCell id=\"0\"/><mxCell id=\"1\" parent=\"0\"/>\n")
            val writtenScreens = mutableSetOf<Int>()
            screens.forEachIndexed { index, screen ->
                val imageBase64 = screenshotPngBase64(screen.imagePath) ?: return@forEachIndexed
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
                val style = "edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;endArrow=block;endFill=1;strokeColor=$connectorColor;exitX=${edge.fromMarkerX.coerceIn(0f, 1f)};exitY=${edge.fromMarkerY.coerceIn(0f, 1f)};exitPerimeter=0;entryX=$entryX;entryY=$entryY;entryPerimeter=0;"
                writer.write("        <mxCell id=\"edge_$edgeIndex\" value=\"\" style=\"${xmlEscape(style)}\" edge=\"1\" parent=\"1\" source=\"screen_$fromIndex\" target=\"screen_$toIndex\">\n")
                writer.write("          <mxGeometry relative=\"1\" as=\"geometry\"/>\n        </mxCell>\n")
            }
            writer.write("      </root>\n    </mxGraphModel>\n  </diagram>\n</mxfile>\n")
        }
            ?: error("Unable to open Draw.io output stream")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        }
        uri
    } catch (error: Throwable) {
        resolver.delete(uri, null, null)
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
    flow: FlowEntity
): Result<Uri> = runCatching {
    require(screens.isNotEmpty()) { "The diagram has no screenshots" }
    val columns = minOf(5, screens.size)
    val margin = 40.0
    val gap = 28.0
    val nodeWidth = 300.0
    val nodeHeight = 645.0
    val rows = (screens.size + columns - 1) / columns
    val width = margin * 2 + columns * nodeWidth + (columns - 1) * gap
    val height = margin * 2 + rows * nodeHeight + (rows - 1) * gap
    val positions = screens.mapIndexed { index, _ ->
        val row = index / columns
        val column = index % columns
        android.graphics.RectF(
            (margin + column * (nodeWidth + gap)).toFloat(),
            (margin + row * (nodeHeight + gap)).toFloat(),
            (margin + column * (nodeWidth + gap) + nodeWidth).toFloat(),
            (margin + row * (nodeHeight + gap) + nodeHeight).toFloat()
        )
    }
    val screenIndex = screens.withIndex().associate { it.value.id to it.index }
    val connectorColor = String.format(java.util.Locale.US, "#%06X", flow.connectorColor and 0xFFFFFF)
    val resolver = context.contentResolver
    val displayName = flow.name.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "flowsnap" } + "-${System.currentTimeMillis()}.svg"
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
    val uri = resolver.insert(collection, values) ?: error("Unable to create SVG file in MediaStore")
    try {
        resolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            writer.write("<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"$width\" height=\"$height\" viewBox=\"0 0 $width $height\">\n")
            writer.write("  <title>${xmlEscape(flow.name)}</title>\n  <rect width=\"100%\" height=\"100%\" fill=\"white\"/>\n")
            // Write and release one compact image at a time instead of retaining
            // a whole diagram worth of Base64 strings in memory.
            screens.forEachIndexed { index, screen ->
                val imageBase64 = screenshotPngBase64(screen.imagePath) ?: return@forEachIndexed
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
                val (route, finalStartX, finalStartY) = if (sameRow) {
                    val laneX = (startX + endX) / 2f
                    Triple("M $startX $startY L $laneX $startY L $laneX $endY", laneX, endY)
                } else {
                    val laneX = maxOf(from.right, to.right) + 10f
                    val laneY = endY - 20f
                    Triple("M $startX $startY L $laneX $startY L $laneX $laneY L $endX $laneY", endX, laneY)
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
                val points = "$endX,$endY ${baseX + perpendicularX},${baseY + perpendicularY} ${baseX - perpendicularX},${baseY - perpendicularY}"
                writer.write("  <path d=\"$route L $baseX $baseY\" fill=\"none\" stroke=\"$connectorColor\" stroke-width=\"3.5\" stroke-linecap=\"butt\" stroke-linejoin=\"round\"/>\n")
                writer.write("  <circle cx=\"$startX\" cy=\"$startY\" r=\"5\" fill=\"$connectorColor\"/>\n  <polygon points=\"$points\" fill=\"$connectorColor\"/>\n")
            }
            writer.write("</svg>\n")
        }
            ?: error("Unable to open SVG output stream")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        uri
    } catch (error: Throwable) {
        resolver.delete(uri, null, null)
        throw error
    }
}

/** Encodes the compact preview used by Draw.io/SVG without Base64 line wraps. */
private fun screenshotPngBase64(path: String): String? = runCatching {
    // Keep a 2.4x asset for the 300px SVG/Draw.io node: it stays crisp in Figma
    // when zoomed or enlarged without retaining full device-resolution captures.
    val bitmap = decodeExportBitmap(path, EDITABLE_EXPORT_IMAGE_WIDTH, EDITABLE_EXPORT_IMAGE_HEIGHT) ?: return@runCatching null
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

private data class SketchImageAsset(val index: Int, val screen: ScreenEntity, val id: String, val file: java.io.File)

/** Re-encode to a conventional PNG so the asset is readable by Sketch/Figma. */
private fun sketchPngBytes(file: java.io.File): ByteArray? =
    decodeExportBitmap(file.absolutePath, EDITABLE_EXPORT_IMAGE_WIDTH, EDITABLE_EXPORT_IMAGE_HEIGHT)?.let { bitmap ->
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
        java.io.File(screen.imagePath).takeIf { it.isFile }?.let { file ->
            sketchPngBytes(file)?.let { pngBytes ->
                SketchImageAsset(index, screen, sketchImageId(pngBytes), file)
            }
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
    val screenIndex = screens.withIndex().associate { it.value.id to it.index }

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
                    val pngBytes = sketchPngBytes(asset.file)
                        ?: error("Unable to decode screenshot asset: ${asset.file.name}")
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
    // Make the name unique before MediaStore inserts it. Otherwise some providers append
    // " (1)" after the extension, yielding `flow.sketch (1)`, which Figma will not filter.
    return "$sanitized-${System.currentTimeMillis()}.sketch"
}

private fun ZipOutputStream.writeZipText(name: String, content: String) {
    putNextEntry(ZipEntry(name))
    write(content.toByteArray(Charsets.UTF_8))
    closeEntry()
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
        val laneY = endY - 20f
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
    // The connector ends at the base of the triangle. The triangle tip alone
    // reaches the screenshot edge, so there is no rounded stroke stub.
    path.lineTo(baseX, baseY)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = connectorColor; style = android.graphics.Paint.Style.STROKE; strokeWidth = 3.5f
        strokeJoin = android.graphics.Paint.Join.ROUND; strokeCap = android.graphics.Paint.Cap.BUTT
    }
    canvas.drawPath(path, paint)
    paint.style = android.graphics.Paint.Style.FILL
    canvas.drawCircle(startX, startY, 5f, paint)
    val arrow = android.graphics.Path().apply {
        moveTo(endX, endY)
        lineTo(baseX + perpendicularX, baseY + perpendicularY)
        lineTo(baseX - perpendicularX, baseY - perpendicularY)
        close()
    }
    canvas.drawPath(arrow, paint)
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

/** Opens the SAF document URI after a successful export. It remains usable from Downloads. */
private fun openPdfPreview(context: Context, uri: Uri) {
    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(Intent.createChooser(viewIntent, "Open FlowSnap PDF"))
    } catch (_: android.content.ActivityNotFoundException) {
        android.widget.Toast.makeText(context, "PDF saved. Install a PDF viewer to open it.", android.widget.Toast.LENGTH_LONG).show()
    }
}

@Composable private fun EditorCircle(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, large: Boolean = false, enabled: Boolean = true) {
    val size = if (large) 74.dp else 48.dp
    val tint = if (enabled) Color(0xFF24519B) else Color(0xFFB8B8B8)
    Surface(modifier = modifier.size(size), shape = CircleShape, color = Color.Transparent, border = BorderStroke(if (large) 3.dp else 2.dp, tint)) { Box(Modifier.fillMaxSize().clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) { Icon(icon, label, tint = tint, modifier = Modifier.size(if (large) 42.dp else 27.dp)) } }
}

@Composable private fun DiagramPreview(
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    flow: FlowEntity,
    modifier: Modifier = Modifier
) {
    val bitmaps = rememberScreenBitmaps(screens, maxDimension = 1600)
    // One scrollable document, with five screenshots per row. The outer
    // editor owns scrolling; no diagram-page toggler is required.
    val columns = minOf(5, screens.size.coerceAtLeast(1))
    val rows = (screens.size + columns - 1) / columns
    val canvasHeight = (rows * 132).dp
    val contentScroll = rememberScrollState()
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(12.dp).fillMaxSize()) {
            // This viewport clips only the document body. The Card remains
            // stationary and keeps its rounded outline while the user scrolls.
            Box(Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
                Box(Modifier.fillMaxSize().verticalScroll(contentScroll)) {
                    FlowDiagramCanvas(
                        screens = screens,
                        transitions = transitions,
                        bitmaps = bitmaps,
                        showMobileStencils = flow.showMobileStencils,
                        connectorColor = Color(flow.connectorColor.toInt()),
                        modifier = Modifier.fillMaxWidth().height(canvasHeight)
                    )
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
                Row(Modifier.fillMaxWidth().background(Color(0xFFF8F8F8)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    metadata.forEach { (label, value) ->
                        Column(Modifier.weight(1f, fill = false)) {
                            Text(label, fontSize = 9.sp, color = Color.Gray)
                            Text(value, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

/** Draws screenshot nodes and the persisted marker-to-next-screen transitions. */
@Composable private fun FlowDiagramCanvas(
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    bitmaps: Map<String, androidx.compose.ui.graphics.ImageBitmap>,
    showMobileStencils: Boolean,
    connectorColor: Color,
    modifier: Modifier = Modifier
) = Canvas(modifier) {
    // All values are in pixels here. Convert dp explicitly to keep the canvas
    // geometry in sync with the Compose layout above on every screen density.
    val margin = 10.dp.toPx()
    val gap = 8.dp.toPx()
    val rowGap = 12.dp.toPx()
    val aspectRatio = 2.15f
    // Five columns match the vertically scrolling reference layout. Rows are
    // added below, rather than shrinking a whole diagram page or creating a
    // second page.
    val columns = minOf(5, screens.size)
    val rows = (screens.size + columns - 1) / columns
    val widthBound = (size.width - margin * 2 - gap * (columns - 1)) / columns
    val heightBound = (size.height - margin * 2 - rowGap * (rows - 1)) / (rows * aspectRatio)
    val nodeWidth = minOf(widthBound, heightBound).coerceAtLeast(1f)
    val nodeHeight = nodeWidth * aspectRatio
    val contentHeight = rows * nodeHeight + (rows - 1) * rowGap
    val topInset = ((size.height - contentHeight) / 2f).coerceAtLeast(0f)
    val positions = screens.mapIndexed { index, _ ->
        val row = index / columns
        val column = index % columns
        val itemsInThisRow = minOf(columns, screens.size - row * columns)
        val rowWidth = itemsInThisRow * nodeWidth + (itemsInThisRow - 1) * gap
        val rowInset = (size.width - rowWidth) / 2f
        Offset(rowInset + column * (nodeWidth + gap), topInset + row * (nodeHeight + rowGap))
    }
    val nodeById = screens.withIndex().associate { it.value.id to it.index }
    // FlowSnap's diagram connectors: the marker is placed by the user on a
    // screenshot and the green orthogonal path leads to the next screen.
    val stroke = connectorColor
    val visibleTransitions = if (showMobileStencils) transitions else emptyList()
    // Each screenshot gets its own fitted rectangle. This preserves its source
    // aspect ratio even when a capture came from a device with a different size.
    val imageRects = screens.mapIndexed { index, screen ->
        val slot = positions[index]
        val image = bitmaps[screen.id]
        if (image == null) {
            androidx.compose.ui.geometry.Rect(slot.x, slot.y, slot.x + nodeWidth, slot.y + nodeHeight)
        } else {
            val aspect = image.width.toFloat() / image.height.toFloat()
            var width = nodeWidth
            var height = width / aspect
            if (height > nodeHeight) {
                height = nodeHeight
                width = height * aspect
            }
            androidx.compose.ui.geometry.Rect(
                slot.x + (nodeWidth - width) / 2f,
                slot.y + (nodeHeight - height) / 2f,
                slot.x + (nodeWidth + width) / 2f,
                slot.y + (nodeHeight + height) / 2f
            )
        }
    }

    // Edges are drawn first so screenshots stay on top. The start point uses
    // the exact normalized marker saved when the user tapped CAPTURE.
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
                val laneY = end.y - 10f
                lineTo(laneX, start.y)
                lineTo(laneX, laneY)
                lineTo(end.x, laneY)
            }
            lineTo(end.x, end.y)
        }
        drawPath(path, stroke, style = Stroke(2.5f))
        drawCircle(stroke, 5f, start)
        if (sameRow) {
            drawLine(stroke, end, end + Offset(-8f, -5f), 2.5f)
            drawLine(stroke, end, end + Offset(-8f, 5f), 2.5f)
        } else {
            drawLine(stroke, end, end + Offset(-5f, -8f), 2.5f)
            drawLine(stroke, end, end + Offset(5f, -8f), 2.5f)
        }
    }

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
                val laneY = end.y - 10f
                lineTo(laneX, start.y)
                lineTo(laneX, laneY)
                lineTo(end.x, laneY)
            }
            lineTo(end.x, end.y)
        }
        drawPath(path, stroke, style = Stroke(2.5f))
        drawCircle(stroke, 5f, start)
        if (sameRow) {
            drawLine(stroke, end, end + Offset(-8f, -5f), 2.5f)
            drawLine(stroke, end, end + Offset(-8f, 5f), 2.5f)
        } else {
            drawLine(stroke, end, end + Offset(-5f, -8f), 2.5f)
            drawLine(stroke, end, end + Offset(5f, -8f), 2.5f)
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
        drawCircle(Color.White, 7f, point)
        drawCircle(stroke, 4f, point)
    }
}
