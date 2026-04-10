package com.rickendy.sideloader.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)

private val DeepIndigoDarkScheme = darkColorScheme(
    primary = PrimaryIndigo,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryIndigoDark,
    onPrimaryContainer = PrimaryIndigoLight,

    secondary = PurpleAccent,
    onSecondary = TextPrimary,
    secondaryContainer = PurpleAccentLight,
    onSecondaryContainer = TextPrimary,

    background = Indigo900,
    onBackground = TextPrimary,

    surface = Indigo800,
    onSurface = TextPrimary,
    surfaceVariant = Indigo700,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = TextPrimary,

    outline = Indigo600,
    outlineVariant = SurfaceElevated,
)

private val DeepIndigoLightScheme = lightColorScheme(
    primary = PrimaryIndigoDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryIndigoLight,
    onPrimaryContainer = Indigo900,

    secondary = PurpleAccent,
    onSecondary = Color.White,
    secondaryContainer = PurpleAccentLight,
    onSecondaryContainer = Indigo900,

    background = Color(0xFFF8F7FF),
    onBackground = Indigo900,

    surface = Color.White,
    onSurface = Indigo900,
    surfaceVariant = Color(0xFFEEEDFE),
    onSurfaceVariant = Indigo700,

    error = ErrorRed,
    onError = Color.White,

    outline = Color(0xFFBBB9E8),
    outlineVariant = Color(0xFFDDDBF5),
)

@Composable
fun SideLoaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DeepIndigoDarkScheme
        else -> DeepIndigoLightScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}