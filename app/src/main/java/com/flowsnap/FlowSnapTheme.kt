package com.flowsnap

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The app's visual roles. Components consume roles instead of palette values so
 * a dark scheme can be introduced by swapping this scheme and these extras.
 */
val FlowSnapLightColorScheme = lightColorScheme(
    primary = Color(0xFF5B5CEB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEEEFF),
    onPrimaryContainer = Color(0xFF303166),
    secondary = Color(0xFF45465A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F3FF),
    onSecondaryContainer = Color(0xFF202B3B),
    // Green is reserved for the flow itself: connectors, markers and endpoints.
    tertiary = Color(0xFF57C84D),
    onTertiary = Color(0xFF09280E),
    error = Color(0xFFC83B42),
    onError = Color.White,
    background = Color(0xFFF5F6FA),
    onBackground = Color(0xFF202B3B),
    surface = Color.White,
    onSurface = Color(0xFF202B3B),
    surfaceVariant = Color(0xFFF1F3F8),
    onSurfaceVariant = Color(0xFF58677A),
    outline = Color(0xFFD2D8E2),
    outlineVariant = Color(0xFFE3E7EF),
    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFF8988F5),
    scrim = Color(0xCC05070B)
)

val FlowSnapDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA9A8FF),
    onPrimary = Color(0xFF202052),
    primaryContainer = Color(0xFF34336B),
    onPrimaryContainer = Color(0xFFE8E7FF),
    secondary = Color(0xFFD7D6E8),
    onSecondary = Color(0xFF1A1A25),
    secondaryContainer = Color(0xFF292936),
    onSecondaryContainer = Color(0xFFE6E5F4),
    tertiary = Color(0xFF67D95D),
    onTertiary = Color(0xFF08250D),
    error = Color(0xFFFF6B72),
    onError = Color(0xFF3A0E14),
    background = Color(0xFF0A0A0A),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF171717),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF242424),
    onSurfaceVariant = Color(0xFFB8B8B8),
    outline = Color(0xFF565656),
    outlineVariant = Color(0xFF303030),
    inverseSurface = Color(0xFFF5F5F5),
    inverseOnSurface = Color(0xFF202020),
    inversePrimary = Color(0xFF5B5CEB),
    scrim = Color(0xFF000000)
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
    canvasBackground = Color(0xFFF1F4FA),
    canvasMinorGrid = Color(0xFFDCE3EF).copy(alpha = .52f),
    canvasMajorGrid = Color(0xFFC5D0E2).copy(alpha = .56f),
    canvasNodeBorder = Color(0xFF475569),
    screenshotPlaceholder = Color(0xFFE2E8F0),
    screenshotPlaceholderIcon = Color(0xFF64748B),
    marker = Color(0xFF57C84D),
    warning = Color(0xFF9A6700),
    disabled = Color(0xFFB8B8B8),
    darkSurface = Color(0xFF191B20),
    darkSurfaceVariant = Color(0xFF131519),
    darkOutline = Color(0xFF30343C),
    onDarkSurface = Color(0xFFF4F6FA),
    darkOnSurfaceMuted = Color(0xFFBAC1CE),
    destructiveContainer = Color(0xFFFCE8EA),
    statusBar = Color(0xFF101114),
    editorControl = Color(0xFFA9A8FF),
    editorIcon = Color(0xFFA9A8FF)
)

val FlowSnapDarkExtraColors = FlowSnapExtraColors(
    canvasBackground = Color(0xFF101114),
    canvasMinorGrid = Color(0xFF282B31).copy(alpha = .54f),
    canvasMajorGrid = Color(0xFF3A3E47).copy(alpha = .58f),
    canvasNodeBorder = Color(0xFF5A606B),
    screenshotPlaceholder = Color(0xFF282B31),
    screenshotPlaceholderIcon = Color(0xFFB5BCC8),
    marker = Color(0xFF67D95D),
    warning = Color(0xFFD6D6D6),
    disabled = Color(0xFF777777),
    darkSurface = Color(0xFF191B20),
    darkSurfaceVariant = Color(0xFF131519),
    darkOutline = Color(0xFF30343C),
    onDarkSurface = Color(0xFFF4F6FA),
    darkOnSurfaceMuted = Color(0xFFBAC1CE),
    destructiveContainer = Color(0xFF462126),
    statusBar = Color(0xFF101114),
    editorControl = Color(0xFFA9A8FF),
    editorIcon = Color(0xFFA9A8FF)
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
