package edu.ucne.faemaciacruz.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FarmaciaGreen,
    onPrimary = SurfaceLight,
    primaryContainer = FarmaciaGreenLight,
    onPrimaryContainer = FarmaciaGreenDark,

    secondary = FarmaciaGreenDark,
    onSecondary = SurfaceLight,
    secondaryContainer = FarmaciaGreenLight,
    onSecondaryContainer = FarmaciaGreenDark,

    tertiary = FarmaciaGreen,
    onTertiary = SurfaceLight,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,

    error = ErrorColor,
    onError = SurfaceLight,
    errorContainer = ErrorBackground,
    onErrorContainer = ErrorColor,

    outline = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFFF0F0F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = FarmaciaGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = FarmaciaGreenDark,
    onPrimaryContainer = FarmaciaGreenLight,

    secondary = FarmaciaGreenLight,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = FarmaciaGreenDark,
    onSecondaryContainer = FarmaciaGreenLight,

    tertiary = FarmaciaGreen,
    onTertiary = androidx.compose.ui.graphics.Color.White,

    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2C2C2C),
    onSurfaceVariant = TextSecondaryDark,

    error = androidx.compose.ui.graphics.Color(0xFFCF6679),
    onError = androidx.compose.ui.graphics.Color.Black,
    errorContainer = androidx.compose.ui.graphics.Color(0xFF93000A),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),

    outline = androidx.compose.ui.graphics.Color(0xFF3D3D3D),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF2C2C2C)
)


@Composable
fun FaemaciaCruzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}