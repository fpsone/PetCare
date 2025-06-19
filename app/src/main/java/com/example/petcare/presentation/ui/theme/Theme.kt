package com.example.petcare.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightPetCareColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = OnPrimaryLight,
    primaryContainer = OrangePrimaryDark, // Or a lighter shade if preferred for container
    onPrimaryContainer = OnPrimaryLight,
    secondary = OrangeSecondary,
    onSecondary = OnSecondaryLight,
    secondaryContainer = OrangeTertiary, // Example: using tertiary for some containers
    onSecondaryContainer = OnTertiaryLight,
    tertiary = OrangeTertiary,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = OrangeSecondary, // Example
    onTertiaryContainer = OnSecondaryLight,
    error = ErrorLight,
    onError = OnErrorLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceLight, // Or a more distinct color
    outline = OutlineLight
)

private val DarkPetCareColorScheme = darkColorScheme(
    primary = OrangePrimaryDark, // Often a slightly desaturated or darker primary for dark theme
    onPrimary = OnPrimaryDark,
    primaryContainer = OrangePrimary,
    onPrimaryContainer = OnPrimaryDark,
    secondary = OrangeSecondary, // Or a darker variant
    onSecondary = OnSecondaryDark,
    secondaryContainer = OrangeTertiary,
    onSecondaryContainer = OnTertiaryDark,
    tertiary = OrangeTertiary, // Or a darker variant
    onTertiary = OnTertiaryDark,
    tertiaryContainer = OrangeSecondary,
    onTertiaryContainer = OnSecondaryDark,
    error = ErrorDark,
    onError = OnErrorDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceDark, // Or a more distinct color
    outline = OutlineDark
)

@Composable
fun PetCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Enable Material You
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkPetCareColorScheme
        else -> LightPetCareColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb() // Or colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme // Or based on background color
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}