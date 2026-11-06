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
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    val flows = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun createFlow(name: String) = viewModelScope.launch { val now = System.currentTimeMillis(); dao.upsert(FlowEntity(UUID.randomUUID().toString(), name.trim(), now, now)) }
    fun createFlow(open: (FlowEntity) -> Unit) = viewModelScope.launch { val now = System.currentTimeMillis(); val flow = FlowEntity(UUID.randomUUID().toString(), "My new app flow", now, now); dao.upsert(flow); open(flow) }
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
                    FlowDiagramCanvas(screens, transitions, bitmaps, Modifier.fillMaxSize())
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
        context.startService(Intent(context, CaptureService::class.java).setAction(CaptureService.ACTION_RESUME))
        dismiss()
        // Reveals the app/Home screen that was behind FlowSnap when capture occurred.
        (context as? Activity)?.moveTaskToBack(true)
    }
    val bitmap = remember(review.path) { android.graphics.BitmapFactory.decodeFile(review.path) }
    var draggedMarker by remember { mutableStateOf<Offset?>(null) }
    // Keep the review actions above Android's gesture/navigation area.
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().height(52.dp).background(Color.White))
        Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF1F1F1)), contentAlignment = Alignment.Center) {
          Box(Modifier.fillMaxWidth().fillMaxSize().padding(horizontal = 38.dp, vertical = 18.dp).background(Color.White)) {
            if (bitmap != null) androidx.compose.foundation.Image(bitmap.asImageBitmap(), "Captured screen", Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
            Canvas(Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures(onDragStart = { draggedMarker = it }) { change, amount -> change.consume(); draggedMarker = (draggedMarker ?: change.position) + amount } }) {
                val marker = draggedMarker ?: Offset(size.width * review.markerX, size.height * review.markerY)
                drawCircle(Color(0x55FFE16A), 62f, marker)
                drawCircle(Color(0xFFFFE16A), 62f, marker, style = Stroke(4f))
                drawCircle(Color.White, 44f, marker)
                drawCircle(Color(0xFF4D4D4D), 10f, marker)
                drawLine(Color(0xFF5C5C5C), marker, Offset(size.width, marker.y), 6f)
                drawContext.canvas. nativeCanvas.drawText("Now click the highlighted target", marker.x + 72f, marker.y - 18f, android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 28f; isAntiAlias = true; setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK) })
            }
          }
        }
        Row(Modifier.fillMaxWidth().height(118.dp).background(Color.White).padding(horizontal = 22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { java.io.File(review.path).delete(); returnToCapturedApp() }, border = BorderStroke(2.dp, Color(0xFFB3261E))) { Text("DISCARD", color = Color(0xFFB3261E), fontSize = 12.sp) }
            OutlinedButton(onClick = { vm.keepCapture(review); returnToCapturedApp() }, border = BorderStroke(2.dp, Color(0xFF1F8B31))) { Text("KEEP\n+ NEXT", color = Color(0xFF1F8B31), fontSize = 11.sp) }
            OutlinedButton(onClick = {
                context.startService(Intent(context, CaptureService::class.java).setAction(CaptureService.ACTION_STOP))
                vm.keepCaptureAndOpenDiagram(review, onFinish)
            }, border = BorderStroke(2.dp, Color(0xFF1F8B31))) { Text("KEEP\n✓ FINISH", color = Color(0xFF1F8B31), fontSize = 11.sp) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun DiagramScreen(flow: FlowEntity, vm: FlowViewModel, goBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tipPreferences = remember { context.getSharedPreferences("flowsnap", Context.MODE_PRIVATE) }
    val projectionManager = remember { context.getSystemService(MediaProjectionManager::class.java) }
    val screens by vm.screens(flow.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val transitions by vm.transitions(flow.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var pendingResult by remember { mutableStateOf<androidx.activity.result.ActivityResult?>(null) }
    var showTips by remember { mutableStateOf(false) }
    var captureLaunchInProgress by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showExpandedPreview by remember { mutableStateOf(false) }
    val exportPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { exportFlowPdf(context, it, screens, transitions, flow) }
    }
    val captureConsent = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        captureLaunchInProgress = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            pendingResult = result
            if (tipPreferences.getBoolean("hide_capture_tip", false)) ContextCompat.startForegroundService(context, Intent(context, CaptureService::class.java).putExtra(CaptureService.EXTRA_FLOW_ID, flow.id).putExtra(CaptureService.EXTRA_RESULT_CODE, result.resultCode).putExtra(CaptureService.EXTRA_RESULT_DATA, result.data))
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
    BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars).diagramGrid()) {
        // The document lives only in this protected middle area. This prevents
        // it from ever sitting below the Home/Settings controls or beneath the
        // image-add / capture / bottom action controls.
        // Keep a deliberately generous exclusion zone around the fixed editor
        // controls. The document is content, not a layer over the controls.
        // In particular, 220dp reserves room for the Add Image button above
        // the large capture button and the three actions at the lower left.
        val topControlsClearance = 112.dp
        val bottomControlsClearance = 220.dp
        val safeDiagramHeight = (maxHeight - topControlsClearance - bottomControlsClearance).coerceAtLeast(0.dp)
        // 600 is expressed as physical pixels so this page looks like the
        // reference on phones of different densities. It is also capped by
        // the safe middle area and width of the actual device.
        val requestedPageSide = with(LocalDensity.current) { 600.toDp() }
        val pageSide = minOf(requestedPageSide, maxWidth * .88f, safeDiagramHeight)
        val pageTopPadding = topControlsClearance + ((safeDiagramHeight - pageSide) / 2)
        EditorCircle(Icons.Default.Home, "Home", goBack, modifier = Modifier.align(Alignment.TopStart).padding(20.dp))
        EditorCircle(Icons.Default.Settings, "Settings", onClick = { }, enabled = hasScreens, modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp))
        if (screens.isEmpty()) Text("Create a diagram with the + option below", Modifier.align(Alignment.Center), color = Color(0xFF24519B), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        else DiagramPreview(
            screens,
            transitions,
            flow,
            // Fixed document page: same size for every diagram, independent of
            // screenshot count, and constrained to the protected middle area.
            Modifier.align(Alignment.TopCenter).padding(top = pageTopPadding)
                // This is a strict, square 600px page (or the safe maximum on
                // a smaller screen). Its size is independent of node count.
                .size(pageSide)
                .clipToBounds(),
            onOpenPreview = { showExpandedPreview = true }
        )
        Row(Modifier.align(Alignment.BottomStart).padding(22.dp), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
            EditorCircle(Icons.Default.Save, "Save", onClick = { }, enabled = hasScreens)
            EditorCircle(Icons.Default.CloudUpload, "Export PDF", onClick = { exportPdf.launch("${flow.name}.pdf") }, enabled = hasScreens)
            EditorCircle(Icons.Default.Delete, "Delete diagram", onClick = { showDeleteConfirmation = true })
        }
        Column(Modifier.align(Alignment.BottomEnd).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.End) {
            EditorCircle(Icons.Default.AddPhotoAlternate, "Add image", onClick = { })
            EditorCircle(Icons.Default.Add, "Capture", beginCapture, large = true, enabled = !captureLaunchInProgress)
        }
    }
    if (showExpandedPreview) {
        Dialog(
            onDismissRequest = { showExpandedPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(Modifier.fillMaxSize().background(Color(0xCC000000)), contentAlignment = Alignment.Center) {
                DiagramPreview(
                    screens = screens,
                    transitions = transitions,
                    flow = flow,
                    modifier = Modifier.fillMaxWidth(.94f).height(520.dp),
                    onOpenPreview = null
                )
                TextButton(
                    onClick = { showExpandedPreview = false },
                    modifier = Modifier.align(Alignment.TopEnd).padding(20.dp)
                ) { Text("CLOSE", color = Color.White) }
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
    runCatching {
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
    }.onSuccess {
        android.widget.Toast.makeText(context, "PDF saved", android.widget.Toast.LENGTH_SHORT).show()
    }.onFailure {
        android.widget.Toast.makeText(context, "Could not save PDF", android.widget.Toast.LENGTH_SHORT).show()
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
    modifier: Modifier = Modifier,
    onOpenPreview: (() -> Unit)? = null
) {
    val bitmaps = remember(screens.map { it.imagePath }) {
        screens.associate { screen -> screen.id to android.graphics.BitmapFactory.decodeFile(screen.imagePath).asImageBitmap() }
    }
    // A document page holds at most seven columns by four rows: 28 screens.
    // Further screens are shown on another fixed-size page via the toggler.
    val pages = remember(screens.map { it.id }) { screens.chunked(28) }
    var selectedPage by remember(screens.map { it.id }) { mutableStateOf(0) }
    val pageIndex = selectedPage.coerceIn(0, pages.lastIndex)
    val pageScreens = pages[pageIndex]
    val pageIds = pageScreens.mapTo(mutableSetOf()) { it.id }
    val pageTransitions = transitions.filter {
        it.fromScreenId in pageIds && it.toScreenId in pageIds
    }
    val cardModifier = if (onOpenPreview == null) modifier else modifier.clickable { onOpenPreview() }
    Card(
        modifier = cardModifier.clipToBounds(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(12.dp).fillMaxSize()) {
            FlowDiagramCanvas(
                screens = pageScreens,
                transitions = pageTransitions,
                bitmaps = bitmaps,
                // The canvas gets the remaining fixed page space. It cannot
                // paint or measure outside the page when the flow grows.
                modifier = Modifier.fillMaxWidth().weight(1f).clipToBounds()
            )
            if (pages.size > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { selectedPage = (pageIndex - 1).coerceAtLeast(0) }, enabled = pageIndex > 0) { Text("‹") }
                    Text("Page ${pageIndex + 1} / ${pages.size}", fontSize = 9.sp, color = Color(0xFF4D4D4D))
                    TextButton(onClick = { selectedPage = (pageIndex + 1).coerceAtMost(pages.lastIndex) }, enabled = pageIndex < pages.lastIndex) { Text("›") }
                }
            }
            Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth().background(Color(0xFFF8F8F8)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Title", fontSize = 9.sp, color = Color.Gray); Text(flow.name, fontSize = 12.sp) }; Column { Text("Version", fontSize = 9.sp, color = Color.Gray); Text("v1.0", fontSize = 12.sp) }; Column { Text("Created by", fontSize = 9.sp, color = Color.Gray); Text("You", fontSize = 12.sp) } }
        }
    }
}

/** Draws screenshot nodes and the persisted marker-to-next-screen transitions. */
@Composable private fun FlowDiagramCanvas(
    screens: List<ScreenEntity>,
    transitions: List<TransitionEntity>,
    bitmaps: Map<String, androidx.compose.ui.graphics.ImageBitmap>,
    modifier: Modifier = Modifier
) = Canvas(modifier) {
    // All values are in pixels here. Convert dp explicitly to keep the canvas
    // geometry in sync with the Compose layout above on every screen density.
    val margin = 10.dp.toPx()
    val gap = 8.dp.toPx()
    val rowGap = 12.dp.toPx()
    val aspectRatio = 2.15f
    // Select the widest thumbnail layout that still fits every captured
    // screenshot inside this fixed-size diagram page (up to seven per row).
    // This is deliberately not a "best fit" choice. The product rule is
    // exactly seven nodes per row whenever seven or more screenshots exist.
    // With paging at 28 nodes, this also guarantees no more than four rows.
    val columns = minOf(7, screens.size)
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
    val stroke = Color(0xFF555555)
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
    transitions.forEach { transition ->
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
}
