package com.flowsnap

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The app's visual roles. Components consume roles instead of palette values so
 * the Material scheme and app-specific extras stay consistent.
 */
val FlowSnapLightColorScheme = lightColorScheme(
    primary = Color(0xFF234CF9),
    onPrimary = Color(0xFFFFFFFC),
    primaryContainer = Color(0xFFF1F3F8),
    onPrimaryContainer = Color(0xFF1C277B),
    secondary = Color(0xFF1C277B),
    onSecondary = Color(0xFFFFFFFC),
    secondaryContainer = Color(0xFFF1F3F8),
    onSecondaryContainer = Color(0xFF1C277B),
    // Green is reserved for the flow itself: connectors, markers and endpoints.
    tertiary = Color(0xFF57C84D),
    onTertiary = Color(0xFF09280E),
    tertiaryContainer = Color(0xFFD3F4CE),
    onTertiaryContainer = Color(0xFF103C15),
    error = Color(0xFFC83B42),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD9),
    onErrorContainer = Color(0xFF410009),
    background = Color(0xFFF4F7FD),
    onBackground = Color(0xFF1C277B),
    surface = Color(0xFFFFFFFC),
    surfaceDim = Color(0xFFCCD7ED),
    surfaceBright = Color(0xFFFFFFFC),
    surfaceContainerLowest = Color(0xFFFFFFFC),
    surfaceContainerLow = Color(0xFFF4F7FD),
    surfaceContainer = Color(0xFFEDF2FB),
    surfaceContainerHigh = Color(0xFFE5EDFA),
    surfaceContainerHighest = Color(0xFFF1F3F8),
    onSurface = Color(0xFF1C277B),
    surfaceVariant = Color(0xFFF1F3F8),
    onSurfaceVariant = Color(0xFF465787),
    outline = Color(0xFFA9B8D8),
    outlineVariant = Color(0xFFCBD7ED),
    inverseSurface = Color(0xFF1C277B),
    inverseOnSurface = Color(0xFFFFFFFC),
    inversePrimary = Color(0xFFB9C7FF),
    surfaceTint = Color(0xFF234CF9),
    scrim = Color(0xCC05070B)
)

data class FlowSnapExtraColors(
    val canvasBackground: Color,
    val canvasMinorGrid: Color,
    val canvasMajorGrid: Color,
    val canvasNodeBorder: Color,
    val screenshotPlaceholder: Color,
    val screenshotPlaceholderIcon: Color,
    val marker: Color,
    val warning: Color,
    val disabled: Color,
    val darkSurface: Color,
    val darkSurfaceVariant: Color,
    val darkOutline: Color,
    val onDarkSurface: Color,
    val darkOnSurfaceMuted: Color,
    val destructiveContainer: Color,
    val statusBar: Color,
    val editorControl: Color,
    val editorIcon: Color
)

val FlowSnapLightExtraColors = FlowSnapExtraColors(
    canvasBackground = Color(0xFFF4F7FD),
    canvasMinorGrid = Color(0xFFDFE3EB).copy(alpha = .52f),
    canvasMajorGrid = Color(0xFFCBD3E2).copy(alpha = .50f),
    canvasNodeBorder = Color(0xFF465787),
    screenshotPlaceholder = Color(0xFFF1F3F8),
    screenshotPlaceholderIcon = Color(0xFF465787),
    marker = Color(0xFF57C84D),
    warning = Color(0xFF9A6700),
    disabled = Color(0xFF91A0C0),
    darkSurface = Color(0xFF191B20),
    darkSurfaceVariant = Color(0xFF131519),
    darkOutline = Color(0xFF30343C),
    onDarkSurface = Color(0xFFF4F6FA),
    darkOnSurfaceMuted = Color(0xFFBAC1CE),
    destructiveContainer = Color(0xFFFCE8EA),
    statusBar = Color(0xFF101114),
    editorControl = Color(0xFF234CF9),
    editorIcon = Color(0xFF234CF9)
)

val LocalFlowSnapExtraColors = staticCompositionLocalOf { FlowSnapLightExtraColors }

/** Short semantic accessors used throughout Compose UI. */
object UiColor {
    val background: Color @Composable get() = MaterialTheme.colorScheme.background
    val surface: Color @Composable get() = MaterialTheme.colorScheme.surface
    val surfaceVariant: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    val onSurface: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val primary: Color @Composable get() = MaterialTheme.colorScheme.primary
    val onPrimary: Color @Composable get() = MaterialTheme.colorScheme.onPrimary
    val primaryContainer: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer: Color @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
    val secondary: Color @Composable get() = MaterialTheme.colorScheme.secondary
    val onSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSecondary
    val secondaryContainer: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
    val error: Color @Composable get() = MaterialTheme.colorScheme.error
    val onError: Color @Composable get() = MaterialTheme.colorScheme.onError
    val outline: Color @Composable get() = MaterialTheme.colorScheme.outline
    val outlineVariant: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val inverseSurface: Color @Composable get() = MaterialTheme.colorScheme.inverseSurface
    val inverseOnSurface: Color @Composable get() = MaterialTheme.colorScheme.inverseOnSurface
    val scrim: Color @Composable get() = MaterialTheme.colorScheme.scrim
    val transparent: Color @Composable get() = Color.Transparent
    val extra: FlowSnapExtraColors @Composable get() = LocalFlowSnapExtraColors.current
}

/** Connector choices are user data, not the app chrome palette. */
object ConnectorPalette {
    const val Green = 0xFF57C84DL
    const val Indigo = 0xFF5B5CEBL
    const val Red = 0xFFE05252L
    const val Charcoal = 0xFF4D4D4DL
    const val Orange = 0xFFF39C12L
    const val Teal = 0xFF00A6A6L
    const val Purple = 0xFF8E44ADL
    const val Pink = 0xFFE91E63L
}
