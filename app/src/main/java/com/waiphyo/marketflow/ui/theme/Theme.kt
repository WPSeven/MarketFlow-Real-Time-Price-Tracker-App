package com.waiphyo.marketflow.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary            = Primary,
    onPrimary          = OnPrimary,
    primaryContainer   = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary          = Secondary,
    onSecondary        = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    surface            = Surface,
    onSurface          = OnSurface,
    surfaceVariant     = SurfaceVariant,
    onSurfaceVariant   = OnSurfaceVariant,
    background         = Background,
    onBackground       = OnBackground,
    error              = Error,
    onError            = OnError,
    outlineVariant     = OutlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary            = PrimaryDark,
    onPrimary          = OnPrimaryDark,
    primaryContainer   = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary          = SecondaryDark,
    onSecondary        = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    surface            = SurfaceDark,
    onSurface          = OnSurfaceDark,
    surfaceVariant     = SurfaceVariantDark,
    onSurfaceVariant   = OnSurfaceVariantDark,
    background         = BackgroundDark,
    onBackground       = OnBackgroundDark,
    error              = ErrorDark,
    onError            = OnErrorDark,
    outlineVariant     = OutlineVariantDark,
)

@Composable
fun PriceTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic colour requires Android 12+; falls back to our palette below.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content,
    )
}
