package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.repository.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF9F9F9),
    surface = Color(0xFFFFFFFF)
)

private val EyeCareColorScheme = lightColorScheme(
    primary = Color(0xFF8D6E63),
    secondary = Color(0xFFBCAAA4),
    background = Color(0xFFFDF5E6), // warm sepia/cream background
    surface = Color(0xFFFFFDD0)
)

private val GlassmorphismColorScheme = darkColorScheme(
    primary = Color(0xFFC084FC),
    secondary = Color(0xFF8B5CF6),
    background = Color(0xFF0C091E), // Rich, premium deep navy-purple dark background
    surface = Color(0x25241C42)     // Ultra-soft frosted glass border background
)

@Composable
fun AmarTakaTheme(
    themeMode: ThemeMode = ThemeMode.GLASSMORPHISM,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.DAY -> LightColorScheme
        ThemeMode.NIGHT -> DarkColorScheme
        ThemeMode.EYE_CARE -> EyeCareColorScheme
        ThemeMode.GLASSMORPHISM -> GlassmorphismColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            if (context is Activity) {
                val window = context.window
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = 
                    (themeMode == ThemeMode.DAY || themeMode == ThemeMode.EYE_CARE)
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
