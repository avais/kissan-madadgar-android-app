package pk.kissanmadadgar.mobile.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AgriGreenTertiary,
    secondary = AgriGreenSecondary,
    tertiary = AgriGreenLight,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color(0xFF0F3B11),
    onSecondary = Color(0xFF0F3B11),
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFE0E0E0)
)

private val LightColorScheme = lightColorScheme(
    primary = AgriGreenPrimary,
    secondary = AgriGreenSecondary,
    tertiary = AgriGreenTertiary,
    background = AgriBackground,
    surface = AgriSurface,
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = TextSecondaryDark,
    onSurface = TextSecondaryDark
)

@Composable
fun KissanMadadgarTheme(
    // Always false, not isSystemInDarkTheme() — every screen in this app hardcodes light-mode
    // colors (white cards, black/dark-gray text) with no actual dark-mode-adapted design anywhere.
    // Letting the theme auto-switch to DarkColorScheme on a system-dark-mode device left any
    // component that relies on theme-inherited colors (instead of an explicit color) rendering
    // DarkColorScheme's light onSurface/onBackground tones against those hardcoded light
    // backgrounds — e.g. near-invisible light-gray text in a white DropdownMenu popup.
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
