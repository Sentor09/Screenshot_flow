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
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF312E81),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F3FF),
    onSecondaryContainer = Color(0xFF202B3B),
    tertiary = Color(0xFF69B83A),
    onTertiary = Color.White,
    error = Color(0xFFB3261E),
    onError = Color.White,
    background = Color(0xFFF5F6FA),
    onBackground = Color(0xFF202B3B),
    surface = Color.White,
    onSurface = Color(0xFF202B3B),
    surfaceVariant = Color(0xFFF1F3F8),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFD2D8E2),
    outlineVariant = Color(0xFFE3E7EF),
    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFFA5B4FC),
    scrim = Color(0xCC05070B)
)

val FlowSnapDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF2F2F2),
    onPrimary = Color(0xFF101010),
    primaryContainer = Color(0xFF2B2B2B),
    onPrimaryContainer = Color(0xFFF2F2F2),
    secondary = Color(0xFFE0E0E0),
    onSecondary = Color(0xFF171717),
    secondaryContainer = Color(0xFF242424),
    onSecondaryContainer = Color(0xFFE0E0E0),
    tertiary = Color(0xFFD0D0D0),
    onTertiary = Color(0xFF171717),
    error = Color(0xFFE6E6E6),
    onError = Color(0xFF171717),
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
    inversePrimary = Color(0xFFF2F2F2),
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
    val darkOnSurfaceMuted: Color,
    val destructiveContainer: Color,
    val statusBar: Color,
    val editorControl: Color,
    val editorIcon: Color
)

val FlowSnapLightExtraColors = FlowSnapExtraColors(
    canvasBackground = Color(0xFFF1F4FA),
    canvasMinorGrid = Color(0xFFDCE3EF).copy(alpha = .72f),
    canvasMajorGrid = Color(0xFFC5D0E2).copy(alpha = .70f),
    canvasNodeBorder = Color(0xFF475569),
    screenshotPlaceholder = Color(0xFFE2E8F0),
    screenshotPlaceholderIcon = Color(0xFF64748B),
    marker = Color(0xFF4F46E5),
    warning = Color(0xFFE8D372),
    disabled = Color(0xFFB8B8B8),
    darkSurface = Color(0xFF1A1A1A),
    darkSurfaceVariant = Color(0xFF101116),
    darkOutline = Color(0xFF2A2A2A),
    darkOnSurfaceMuted = Color(0xFFCBD5E1),
    destructiveContainer = Color(0xD9111111),
    statusBar = Color(0xFF171717),
    editorControl = Color(0xFF4F46E5),
    editorIcon = Color(0xFF4F46E5)
)

val FlowSnapDarkExtraColors = FlowSnapExtraColors(
    canvasBackground = Color(0xFF111111),
    canvasMinorGrid = Color(0xFF282828).copy(alpha = .72f),
    canvasMajorGrid = Color(0xFF3C3C3C).copy(alpha = .70f),
    canvasNodeBorder = Color(0xFF565656),
    screenshotPlaceholder = Color(0xFF2A2A2A),
    screenshotPlaceholderIcon = Color(0xFFB8B8B8),
    marker = Color(0xFFF2F2F2),
    warning = Color(0xFFD6D6D6),
    disabled = Color(0xFF777777),
    darkSurface = Color(0xFF171717),
    darkSurfaceVariant = Color(0xFF101010),
    darkOutline = Color(0xFF303030),
    darkOnSurfaceMuted = Color(0xFFB8B8B8),
    destructiveContainer = Color(0xFF292929),
    statusBar = Color(0xFF0A0A0A),
    editorControl = Color(0xFF4A4A4A),
    editorIcon = Color(0xFFF5F5F5)
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
    const val Green = 0xFF69B83AL
    const val Indigo = 0xFF4F46E5L
    const val Red = 0xFFE05252L
    const val Charcoal = 0xFF4D4D4DL
    const val Orange = 0xFFF39C12L
    const val Teal = 0xFF00A6A6L
    const val Purple = 0xFF8E44ADL
    const val Pink = 0xFFE91E63L
}
