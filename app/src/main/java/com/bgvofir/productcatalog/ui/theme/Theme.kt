package com.bgvofir.productcatalog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
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

// The spec only defines a light palette. This dark one keeps the same blue/grey identity, using
// the lighter tone of each color so text and accents stay readable on dark surfaces.
private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = Color(0xFFD3E3FD),
    secondary = Color(0xFFC4C7C5),
    onSecondary = Color(0xFF2D312F),
    secondaryContainer = Color(0xFF444746),
    onSecondaryContainer = Color(0xFFE3E3E3),
    tertiary = Color(0xFFC4C7C5),
    onTertiary = Color(0xFF2D312F),
    tertiaryContainer = Color(0xFF444746),
    onTertiaryContainer = Color(0xFFE3E3E3),
    background = Color(0xFF131314),
    onBackground = Color(0xFFE3E3E3),
    surface = Color(0xFF1E1F20),
    onSurface = Color(0xFFE3E3E3),
    surfaceVariant = Color(0xFF2D2F31),
    onSurfaceVariant = Color(0xFFC4C7C5),
    outline = Color(0xFF8E918F),
    outlineVariant = Color(0xFF444746),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    inversePrimary = Color(0xFF1A73E8),
    inverseSurface = Color(0xFFE3E3E3),
    inverseOnSurface = Color(0xFF303134),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFA8C7FA),
    surfaceBright = Color(0xFF37393B),
    surfaceDim = Color(0xFF131314),
    surfaceContainerLowest = Color(0xFF0E0E0E),
    surfaceContainerLow = Color(0xFF1B1B1C),
    surfaceContainer = Color(0xFF1E1F20),
    surfaceContainerHigh = Color(0xFF282A2C),
    surfaceContainerHighest = Color(0xFF333537),
    // "Fixed" roles are the same in light and dark by definition.
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
// The same amber reads well on both light and dark surfaces.
val ColorScheme.starAmber: Color
    get() = Color(0xFFFFC107)

@Composable
fun ProductCatalogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
