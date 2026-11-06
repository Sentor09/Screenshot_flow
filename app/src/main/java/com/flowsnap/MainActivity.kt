package com.flowsnap

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
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
import com.flowsnap.data.TransitionEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class CaptureReview(val flowId: String, val path: String, val markerX: Float, val markerY: Float)

class MainActivity : ComponentActivity() {
    private var review by mutableStateOf<CaptureReview?>(null)
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); enableEdgeToEdge(); WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true; acceptReviewIntent(intent); setContent { MaterialTheme { FlowSnapApp(review = review, dismissReview = { review = null }) } } }
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
    Column(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).windowInsetsPadding(WindowInsets.statusBars)) {
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

@Composable private fun SampleFlowTile(title: String, accent: Color) = Card(modifier = Modifier.height(244.dp).fillMaxWidth()) { Column(Modifier.padding(12.dp).fillMaxSize()) { FlowPreview(accent, Modifier.weight(1f).fillMaxWidth()); Text(title, color = Color(0xFF263F7D), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium) } }

@Composable private fun FlowTile(flow: FlowEntity, vm: FlowViewModel, onClick: () -> Unit) {
    val screens by vm.screens(flow.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val transitions by vm.transitions(flow.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val bitmaps = remember(screens.map { it.imagePath }) {
        screens.associate { screen -> screen.id to android.graphics.BitmapFactory.decodeFile(screen.imagePath).asImageBitmap() }
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().aspectRatio(.72f)) {
        Column(Modifier.padding(10.dp).fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (screens.isEmpty()) {
                    FlowPreview(Color(0xFF24519B), Modifier.fillMaxSize())
                } else {
                    FlowDiagramCanvas(screens, transitions, bitmaps, Modifier.fillMaxSize())
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(flow.name, color = Color(0xFF263F7D), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, maxLines = 2)
            Text("${screens.size} screen${if (screens.size == 1) "" else "s"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun FlowPreview(accent: Color, modifier: Modifier = Modifier) = Canvas(modifier) {
    val screenW = size.width / 4.8f; val screenH = size.height / 2.1f
    val positions = listOf(0.06f to 0.15f, 0.57f to 0.10f, 0.30f to 0.56f)
    positions.forEachIndexed { index, (x, y) ->
        val left = x * size.width; val top = y * size.height
        drawRoundRect(Color.White, topLeft = androidx.compose.ui.geometry.Offset(left, top), size = androidx.compose.ui.geometry.Size(screenW, screenH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
        drawRoundRect(accent.copy(alpha = .55f), topLeft = androidx.compose.ui.geometry.Offset(left, top), size = androidx.compose.ui.geometry.Size(screenW, screenH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f), style = Stroke(3f))
        drawRect(accent.copy(alpha = .75f), topLeft = androidx.compose.ui.geometry.Offset(left + 8f, top + 14f), size = androidx.compose.ui.geometry.Size(screenW - 16f, 6f))
        if (index < 2) drawLine(accent.copy(alpha = .65f), androidx.compose.ui.geometry.Offset(left + screenW, top + screenH / 2), androidx.compose.ui.geometry.Offset(left + screenW + 20f, top + screenH / 2), strokeWidth = 3f)
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
    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars).diagramGrid()) {
        EditorCircle(Icons.Default.Home, "Home", goBack, modifier = Modifier.align(Alignment.TopStart).padding(20.dp))
        EditorCircle(Icons.Default.Settings, "Settings", onClick = { }, enabled = hasScreens, modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp))
        if (screens.isEmpty()) Text("Create a diagram with the + option below", Modifier.align(Alignment.Center), color = Color(0xFF24519B), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        else DiagramPreview(screens, transitions, flow, Modifier.align(Alignment.Center).fillMaxWidth(.88f))
        Row(Modifier.align(Alignment.BottomStart).padding(22.dp), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
            EditorCircle(Icons.Default.Save, "Save", onClick = { }, enabled = hasScreens)
            EditorCircle(Icons.Default.CloudUpload, "Export", onClick = { }, enabled = hasScreens)
            EditorCircle(Icons.Default.Delete, "Delete", onClick = { }, enabled = hasScreens)
        }
        Column(Modifier.align(Alignment.BottomEnd).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.End) {
            EditorCircle(Icons.Default.AddPhotoAlternate, "Add image", onClick = { })
            EditorCircle(Icons.Default.Add, "Capture", beginCapture, large = true, enabled = !captureLaunchInProgress)
        }
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

@Composable private fun EditorCircle(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, large: Boolean = false, enabled: Boolean = true, modifier: Modifier = Modifier) {
    val size = if (large) 74.dp else 48.dp
    val tint = if (enabled) Color(0xFF24519B) else Color(0xFFB8B8B8)
    Surface(modifier = modifier.size(size), shape = CircleShape, color = Color.Transparent, border = BorderStroke(if (large) 3.dp else 2.dp, tint)) { Box(Modifier.fillMaxSize().clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) { Icon(icon, label, tint = tint, modifier = Modifier.size(if (large) 42.dp else 27.dp)) } }
}

@Composable private fun DiagramPreview(screens: List<ScreenEntity>, transitions: List<TransitionEntity>, flow: FlowEntity, modifier: Modifier = Modifier) {
    val bitmaps = remember(screens.map { it.imagePath }) {
        screens.associate { screen -> screen.id to android.graphics.BitmapFactory.decodeFile(screen.imagePath).asImageBitmap() }
    }
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Column(Modifier.padding(12.dp)) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val columns = minOf(4, screens.size)
                val rows = (screens.size + columns - 1) / columns
                val horizontalPadding = 20.dp
                val horizontalGap = 8.dp
                val nodeWidth = (maxWidth - horizontalPadding - horizontalGap * (columns - 1)) / columns
                // Phone screenshots are portrait. Reserve a correctly shaped
                // slot instead of forcing them into a short, squashed box.
                val nodeHeight = nodeWidth * 2.15f
                val canvasHeight = nodeHeight * rows + 16.dp + 12.dp * (rows - 1)
                FlowDiagramCanvas(
                    screens = screens,
                    transitions = transitions,
                    bitmaps = bitmaps,
                    modifier = Modifier.fillMaxWidth().height(canvasHeight)
                )
            }
            Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth().background(Color(0xFFF8F8F8)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Title", fontSize = 9.sp, color = Color.Gray); Text(flow.name, fontSize = 12.sp) }; Column { Text("Version", fontSize = 9.sp, color = Color.Gray); Text("v1.0", fontSize = 12.sp) }; Column { Text("Created by", fontSize = 9.sp, color = Color.Gray); Text("You", fontSize = 12.sp) } }
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
    val columns = minOf(4, screens.size)
    val margin = 10.dp.toPx()
    val gap = 8.dp.toPx()
    val nodeWidth = (size.width - margin * 2 - gap * (columns - 1)) / columns
    val nodeHeight = nodeWidth * 2.15f
    val rowGap = 12.dp.toPx()
    val leftInset = (size.width - (nodeWidth * columns + gap * (columns - 1))) / 2f
    val positions = screens.mapIndexed { index, _ ->
        val row = index / columns
        val column = index % columns
        Offset(leftInset + column * (nodeWidth + gap), 8f + row * (nodeHeight + rowGap))
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
