package com.bgvofir.productcatalog.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// The raw palette stays private: composables use MaterialTheme.colorScheme roles only.
private val LightColors = lightColorScheme(
    // Values from the spec's design system.
    primary = Color(0xFF1A73E8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF5F6368),
    secondaryContainer = Color(0xFFE8EAED),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFD93025),
    errorContainer = Color(0xFFFCE8E6),

    // Not in the spec. Derived from the palette above so that no role falls back to the
    // Material 3 baseline purple (cards, bottom bar, dialogs and snackbars use these).
    onSecondary = Color(0xFFFFFFFF),
    onSecondaryContainer = Color(0xFF1C1B1F),
    tertiary = Color(0xFF5F6368),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE8EAED),
    onTertiaryContainer = Color(0xFF1C1B1F),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410E0B),
    inversePrimary = Color(0xFFA8C7FA),
    surfaceTint = Color(0xFF1A73E8),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE8EAED),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFAFAFA),
    surfaceContainerHigh = Color(0xFFF1F3F4),
    surfaceContainerHighest = Color(0xFFE8EAED),
    inverseSurface = Color(0xFF303134),
    inverseOnSurface = Color(0xFFF1F3F4),
    scrim = Color(0xFF000000),
    primaryFixed = Color(0xFFD3E3FD),
    primaryFixedDim = Color(0xFFA8C7FA),
    onPrimaryFixed = Color(0xFF001D36),
    onPrimaryFixedVariant = Color(0xFF0B57D0),
    secondaryFixed = Color(0xFFE8EAED),
    secondaryFixedDim = Color(0xFFCAC4D0),
    onSecondaryFixed = Color(0xFF1C1B1F),
    onSecondaryFixedVariant = Color(0xFF49454F),
    tertiaryFixed = Color(0xFFE8EAED),
    tertiaryFixedDim = Color(0xFFCAC4D0),
    onTertiaryFixed = Color(0xFF1C1B1F),
    onTertiaryFixedVariant = Color(0xFF49454F),
)

// The rating star color is not a Material 3 role, so it is exposed as an extension on the scheme.
val ColorScheme.starAmber: Color
    get() = Color(0xFFFFC107)

@Composable
fun ProductCatalogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(),
        content = content,
    )
}
