package com.flowsnap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.media.projection.MediaProjectionConfig
import android.provider.Settings
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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flowsnap.capture.CaptureService
import com.flowsnap.data.FlowDatabase
import com.flowsnap.data.FlowEntity
import com.flowsnap.data.ScreenEntity
import com.flowsnap.data.ScreenshotStorage
import com.flowsnap.data.TransitionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

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
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); enableEdgeToEdge(); WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true; acceptReviewIntent(intent); setContent { MaterialTheme(typography = FlowSnapTypography) { FlowSnapApp(review = review, dismissReview = { review = null }) } } }
    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); acceptReviewIntent(intent) }
    private fun acceptReviewIntent(intent: Intent) { val path = intent.getStringExtra(CaptureService.EXTRA_PATH) ?: return; review = CaptureReview(intent.getStringExtra(CaptureService.EXTRA_FLOW_ID).orEmpty(), path, intent.getFloatExtra(CaptureService.EXTRA_MARKER_X, .5f), intent.getFloatExtra(CaptureService.EXTRA_MARKER_Y, .5f)) }
}

class FlowViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val dao = FlowDatabase.create(application).flowDao()
    // Keep the gallery query alive for the app lifetime. This prevents a
    // transient empty Home grid when returning from a completed capture.
    val flows = dao.observeAll().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun createFlow(name: String) = viewModelScope.launch { val now = System.currentTimeMillis(); dao.upsert(FlowEntity(UUID.randomUUID().toString(), name.trim(), now, now)) }
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
    private suspend fun persistCapture(review: CaptureReview) {
        val previous = dao.lastScreen(review.flowId)
        val now = System.currentTimeMillis()
        val position = dao.screenCount(review.flowId)
        val screen = ScreenEntity(UUID.randomUUID().toString(), review.flowId, review.path, review.markerX, review.markerY, position, .08f + (position % 2) * .52f, .12f + (position / 2) * .48f, now)
        dao.insertScreen(screen)
        if (previous != null) dao.insertTransition(TransitionEntity(UUID.randomUUID().toString(), review.flowId, previous.id, screen.id, previous.markerX, previous.markerY, now))
        dao.flow(review.flowId)?.let { dao.upsert(it.copy(updatedAt = now)) }
    }
    fun keepCapture(review: CaptureReview) = viewModelScope.launch { persistCapture(review) }
    fun keepCaptureAndOpenDiagram(review: CaptureReview, onSaved: (FlowEntity?) -> Unit) = viewModelScope.launch {
        persistCapture(review)
        onSaved(dao.flow(review.flowId))
    }
    fun deleteFlow(flow: FlowEntity, onDeleted: () -> Unit) = viewModelScope.launch {
        // Delete the locally stored screenshot files before removing their Room records.
        dao.screensForFlow(flow.id).forEach { screen -> runCatching { java.io.File(screen.imagePath).delete() } }
        dao.deleteTransitionsForFlow(flow.id)
        dao.deleteScreensForFlow(flow.id)
        dao.deleteFlow(flow.id)
        ScreenshotStorage.deleteFlowFolder(getApplication(), flow.id)
        onDeleted()
    }
    fun screens(flowId: String) = dao.observeScreens(flowId)
    fun transitions(flowId: String) = dao.observeTransitions(flowId)
    fun flow(flowId: String) = dao.observeFlow(flowId)
}

private sealed interface Screen { data object Home : Screen; data class Diagram(val flow: FlowEntity) : Screen }

@Composable private fun FlowSnapApp(review: CaptureReview?, dismissReview: () -> Unit, vm: FlowViewModel = viewModel()) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    if (review != null) CaptureReviewScreen(review, vm, dismissReview, onFinish = { savedFlow ->
        if (savedFlow != null) screen = Screen.Diagram(savedFlow)
        dismissReview()
    }) else when (val current = screen) { Screen.Home -> HomeScreen(vm, { screen = Screen.Diagram(it) }); is Screen.Diagram -> DiagramScreen(current.flow, vm) { screen = Screen.Home } }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            Icon(Icons.Default.Lightbulb, "Tips", Modifier.size(28.dp), tint = Color(0xFFE8D372))
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
    val bitmaps = remember(screens.map { it.imagePath }) {
        screens.associate { screen -> screen.id to android.graphics.BitmapFactory.decodeFile(screen.imagePath).asImageBitmap() }
    }
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

@Composable private fun NewFlowDialog(onDismiss: () -> Unit, create: (String) -> Unit) { var name by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = onDismiss, title = { Text("New flow") }, text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Flow name") }, singleLine = true) }, confirmButton = { Button(enabled = name.isNotBlank(), onClick = { create(name) }) { Text("Create") } }, dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }) }

@OptIn(ExperimentalMaterial3Api::class)
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
    val bitmap = remember(review.path) { android.graphics.BitmapFactory.decodeFile(review.path) }
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
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().height(52.dp).background(Color.White))
        Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF1F1F1)), contentAlignment = Alignment.Center) {
          Box(Modifier.fillMaxWidth().fillMaxSize().padding(horizontal = 38.dp, vertical = 18.dp).background(Color.White)) {
            if (bitmap != null) androidx.compose.foundation.Image(bitmap.asImageBitmap(), "Captured screen", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
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
        Row(Modifier.fillMaxWidth().height(118.dp).background(Color.White).padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { java.io.File(review.path).delete(); returnToCapturedApp() }, border = BorderStroke(2.dp, Color(0xFFB3261E))) { Text("DISCARD", color = Color(0xFFB3261E), fontSize = 12.sp) }
            OutlinedButton(onClick = { vm.keepCapture(reviewedCapture); returnToCapturedApp() }, border = BorderStroke(2.dp, Color(0xFF1F8B31))) { Text("KEEP\n+ NEXT", color = Color(0xFF1F8B31), fontSize = 11.sp) }
            OutlinedButton(onClick = {
                context.startService(Intent(context, CaptureService::class.java).setAction(CaptureService.ACTION_STOP))
                vm.keepCaptureAndOpenDiagram(reviewedCapture, onFinish)
            }, border = BorderStroke(2.dp, Color(0xFF1F8B31))) { Text("KEEP\n✓ FINISH", color = Color(0xFF1F8B31), fontSize = 11.sp) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun DiagramScreen(flow: FlowEntity, vm: FlowViewModel, goBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tipPreferences = remember { context.getSharedPreferences("flowsnap", Context.MODE_PRIVATE) }
    val projectionManager = remember { context.getSystemService(MediaProjectionManager::class.java) }
    val storedFlow by vm.flow(flow.id).collectAsStateWithLifecycle(initialValue = flow)
    // Room can emit null briefly if this diagram is being deleted. Keep the
    // last route argument as a safe rendering fallback.
    val currentFlow = storedFlow ?: flow
    val screens by vm.screens(flow.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val transitions by vm.transitions(flow.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var pendingResult by remember { mutableStateOf<androidx.activity.result.ActivityResult?>(null) }
    var showTips by remember { mutableStateOf(false) }
    var captureLaunchInProgress by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showDiagramSettings by remember { mutableStateOf(false) }
    val exportPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { exportFlowPdf(context, it, screens, transitions, currentFlow) }
    }
    val captureConsent = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        captureLaunchInProgress = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            pendingResult = result
                    if (tipPreferences.getBoolean("hide_capture_tip", false)) ContextCompat.startForegroundService(context, Intent(context, CaptureService::class.java).putExtra(CaptureService.EXTRA_FLOW_ID, currentFlow.id).putExtra(CaptureService.EXTRA_RESULT_CODE, result.resultCode).putExtra(CaptureService.EXTRA_RESULT_DATA, result.data))
            else showTips = true
        }
    }
    val beginCapture = {
        when {
            captureLaunchInProgress -> Unit
            !Settings.canDrawOverlays(context) -> {
                // Do not launch projection consent until the user has returned
                // from overlay permission settings. Launching both at once can
                // leave the activity looking frozen on some devices.
                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${context.packageName}")))
            }
            else -> {
                captureLaunchInProgress = true
                captureConsent.launch(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        projectionManager.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())
                    } else {
                        projectionManager.createScreenCaptureIntent()
                    }
                )
            }
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
                flow = currentFlow,
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
            EditorCircle(Icons.Default.Save, "Save", onClick = { }, enabled = hasScreens)
            EditorCircle(Icons.Default.CloudUpload, "Export PDF", onClick = { exportPdf.launch("${currentFlow.name}.pdf") }, enabled = hasScreens)
            EditorCircle(Icons.Default.Delete, "Delete diagram", onClick = { showDeleteConfirmation = true })
        }
        Column(Modifier.align(Alignment.BottomEnd).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.End) {
            EditorCircle(Icons.Default.AddPhotoAlternate, "Add image", onClick = { })
            EditorCircle(Icons.Default.Add, "Capture", beginCapture, large = true, enabled = !captureLaunchInProgress)
        }
    }
    if (showDiagramSettings) {
        DiagramSettingsDialog(
            flow = currentFlow,
            onDismiss = { showDiagramSettings = false },
            onSave = { title, version, createdBy, showTitle, showVersion, showCreatedAt, showCreatedBy, showAppVersion, showMobileStencils, connectorColor ->
                vm.updateFlowMetadata(currentFlow, title, version, createdBy, showTitle, showVersion, showCreatedAt, showCreatedBy, showAppVersion) { savedFlow ->
                    vm.updateDiagramAppearance(savedFlow, showMobileStencils, connectorColor) { showDiagramSettings = false }
                }
            }
        )
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
                    vm.deleteFlow(currentFlow) {
                        showDeleteConfirmation = false
                        goBack()
                    }
                }) { Text("OK", color = Color(0xFF263F7D)) }
            }
        )
    }
    if (showTips) CaptureTipDialog(onConfirm = { dontShowAgain -> if (dontShowAgain) tipPreferences.edit().putBoolean("hide_capture_tip", true).apply(); val result = pendingResult!!; ContextCompat.startForegroundService(context, Intent(context, CaptureService::class.java).putExtra(CaptureService.EXTRA_FLOW_ID, flow.id).putExtra(CaptureService.EXTRA_RESULT_CODE, result.resultCode).putExtra(CaptureService.EXTRA_RESULT_DATA, result.data)); showTips = false })
}

@Composable private fun CaptureTipDialog(onConfirm: (Boolean) -> Unit) {
    var dontShowAgain by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = { }, containerColor = Color.White, titleContentColor = Color(0xFF202020), textContentColor = Color(0xFF666666), title = { Text("🤓 Tips to get you started!", fontSize = 22.sp) }, text = {
        Column { Text("When creating a screenshot diagram, a floating widget will show over the top of your screen.\n\nDrag it to the position where you want to place an arrow connecting to the next screen.", fontSize = 14.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(8.dp)); DragTapIllustration(); Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = dontShowAgain, onCheckedChange = { dontShowAgain = it }, modifier = Modifier.size(34.dp)); Spacer(Modifier.width(4.dp)); Text("Ok, got it. Don't show me this again.", color = Color(0xFF222222), fontSize = 13.sp) }
        }
    }, confirmButton = { TextButton(onClick = { onConfirm(dontShowAgain) }) { Text("OK", color = Color(0xFFE0C85B)) } })
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
    var connectorColor by remember(flow.id) { mutableStateOf(flow.connectorColor) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        // Keep the canvas visible without the heavy, muddy system dim overlay.
        val dialogView = LocalView.current
        SideEffect {
            (dialogView.parent as? DialogWindowProvider)?.window?.setDimAmount(0.12f)
        }
        Surface(
            // The settings panel is deliberately a single, complete screen.
            // Keep its actions inside the system safe area instead of requiring
            // the user to scroll to reach SAVE.
            modifier = Modifier.fillMaxWidth(.90f).fillMaxHeight(.90f),
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E7EF)),
            shadowElevation = 12.dp
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp)) {
                Text("DIAGRAM LABELS", color = Color(0xFF1F2D43), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE3E7EF)))
                Spacer(Modifier.height(10.dp))
                EditableDiagramSetting("Title", title, { title = it }, showTitle, { showTitle = !showTitle })
                EditableDiagramSetting("Diagram Version", version, { version = it }, showVersion, { showVersion = !showVersion })
                ReadOnlyDiagramSetting("Created At", java.text.SimpleDateFormat("yyyy MMM dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(flow.createdAt)), showCreatedAt, { showCreatedAt = !showCreatedAt })
                EditableDiagramSetting("Created By", createdBy, { createdBy = it }, showCreatedBy, { showCreatedBy = !showCreatedBy })
                ReadOnlyDiagramSetting("App Version", "v1.0", showAppVersion, { showAppVersion = !showAppVersion })

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
                Spacer(Modifier.weight(1f))
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

@Composable private fun ProOnlySetting(symbol: String, label: String, value: String) {
    // Disabled settings should still be legible and belong to the same visual
    // palette as the rest of the editor, rather than fading into white.
    // Use FlowSnap's blue palette even for unavailable controls.  A softened
    // navy keeps the disabled state clear without looking washed out.
    val disabledColor = Color(0xFF647AA8)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(symbol, color = disabledColor, fontSize = 26.sp)
        Spacer(Modifier.width(18.dp))
        Column { Text(label, color = disabledColor, fontSize = 16.sp, fontWeight = FontWeight.Medium); Text(value, color = disabledColor, fontSize = 14.sp) }
    }
}

@Composable private fun DragTapIllustration() = Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    Text("╭──↘", color = Color(0xFF314A7D), fontSize = 18.sp)
    Box(Modifier.size(86.dp).background(Color(0xFFE8D979), CircleShape), contentAlignment = Alignment.Center) { Box(Modifier.size(58.dp).background(Color.Black, CircleShape), contentAlignment = Alignment.Center) { Text("drag\n& tap", color = Color.White, fontSize = 14.sp) } }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("╭↗\nTap on it to take a\nscreenshot.", fontSize = 10.sp); Text("╰↖\nTap ✓ to come\nback to the app.", fontSize = 10.sp) }
}

private fun Modifier.diagramGrid() = this.drawBehind {
    drawRect(Color(0xFFF1F1F1))
    val spacing = 105f
    var x = 0f; while (x <= size.width) { drawLine(Color.White, androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), 3f); x += spacing }
    var y = 0f; while (y <= size.height) { drawLine(Color.White, androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 3f); y += spacing }
}

/** Writes the same paged 7 x 4 flow arrangement to a user-selected PDF file. */
private fun exportFlowPdf(
    context: Context,
    destination: Uri,
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    flow: FlowEntity
) {
    // Bitmap decoding and PDF encoding are intentionally off the UI thread.
    // A flow can contain many full-resolution screenshots.
    Thread {
        val result = runCatching {
        // PdfDocument is not a java.io.Closeable on every Android API stub,
        // so close it explicitly instead of Kotlin's use { } helper.
        val document = PdfDocument()
        try {
            screens.chunked(28).forEachIndexed { pageNumber, pageScreens ->
                val page = document.startPage(PdfDocument.PageInfo.Builder(1200, 1200, pageNumber + 1).create())
                val canvas = page.canvas
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                canvas.drawColor(android.graphics.Color.WHITE)
                val columns = minOf(7, pageScreens.size)
                val rows = (pageScreens.size + columns - 1) / columns
                val margin = 48f
                val footerHeight = 92f
                val gap = 18f
                val availableHeight = 1200f - margin * 2 - footerHeight - gap * (rows - 1)
                val nodeWidth = minOf(
                    (1200f - margin * 2 - gap * (columns - 1)) / columns,
                    availableHeight / (rows * 2.15f)
                )
                val nodeHeight = nodeWidth * 2.15f
                val positions = pageScreens.mapIndexed { index, _ ->
                    val row = index / columns
                    val column = index % columns
                    android.graphics.RectF(
                        margin + column * (nodeWidth + gap),
                        margin + row * (nodeHeight + gap),
                        margin + column * (nodeWidth + gap) + nodeWidth,
                        margin + row * (nodeHeight + gap) + nodeHeight
                    )
                }
                val indexes = pageScreens.withIndex().associate { it.value.id to it.index }
                paint.color = android.graphics.Color.rgb(80, 80, 80)
                paint.strokeWidth = 4f
                transitions.filter { it.fromScreenId in indexes && it.toScreenId in indexes }.forEach { edge ->
                    val from = positions[indexes.getValue(edge.fromScreenId)]
                    val to = positions[indexes.getValue(edge.toScreenId)]
                    val startX = from.left + from.width() * edge.fromMarkerX
                    val startY = from.top + from.height() * edge.fromMarkerY
                    canvas.drawLine(startX, startY, to.centerX(), to.centerY(), paint)
                }
                pageScreens.forEachIndexed { index, screen ->
                    android.graphics.BitmapFactory.decodeFile(screen.imagePath)?.let { bitmap ->
                        val target = positions[index]
                        val scale = minOf(target.width() / bitmap.width, target.height() / bitmap.height)
                        val width = bitmap.width * scale
                        val height = bitmap.height * scale
                        val imageRect = android.graphics.RectF(target.centerX() - width / 2, target.centerY() - height / 2, target.centerX() + width / 2, target.centerY() + height / 2)
                        canvas.drawBitmap(bitmap, null, imageRect, paint)
                        bitmap.recycle()
                    }
                }
                paint.color = android.graphics.Color.rgb(245, 245, 245)
                canvas.drawRect(36f, 1100f, 1164f, 1164f, paint)
                paint.color = android.graphics.Color.DKGRAY
                paint.textSize = 22f
                canvas.drawText(flow.name, 54f, 1140f, paint)
                canvas.drawText("v1.0", 720f, 1140f, paint)
                canvas.drawText("Page ${pageNumber + 1}", 960f, 1140f, paint)
                document.finishPage(page)
            }
            val output = context.contentResolver.openOutputStream(destination)
                ?: error("Unable to open the selected PDF destination")
            output.use { stream -> document.writeTo(stream) }
        } finally {
            document.close()
        }
        }
        val showResult: () -> Unit = {
            result.onSuccess {
                android.widget.Toast.makeText(context, "PDF saved", android.widget.Toast.LENGTH_SHORT).show()
                openPdfPreview(context, destination)
            }.onFailure { error ->
                android.util.Log.e("FlowSnapPdf", "PDF export failed", error)
                android.widget.Toast.makeText(context, "Could not save PDF: ${error.message ?: "unknown error"}", android.widget.Toast.LENGTH_LONG).show()
            }
            Unit
        }
        (context as? Activity)?.runOnUiThread(showResult) ?: android.os.Handler(android.os.Looper.getMainLooper()).post(showResult)
    }.start()
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

@Composable private fun EditorCircle(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, large: Boolean = false, enabled: Boolean = true, modifier: Modifier = Modifier) {
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
    val bitmaps = remember(screens.map { it.imagePath }) {
        screens.associate { screen -> screen.id to android.graphics.BitmapFactory.decodeFile(screen.imagePath).asImageBitmap() }
    }
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
