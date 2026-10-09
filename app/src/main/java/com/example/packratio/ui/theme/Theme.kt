package com.example.packratio.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.packratio.model.AppThemeMode
import com.example.packratio.model.PackStatus

private val DarkColorScheme = darkColorScheme(
    primary = TealDarkPrimary,
    onPrimary = TealDarkOnPrimary,
    primaryContainer = TealDarkPrimaryContainer,
    onPrimaryContainer = TealDarkOnPrimaryContainer,
    secondary = SlateDarkSecondary,
    onSecondary = SlateDarkOnSecondary,
    secondaryContainer = SlateDarkSecondaryContainer,
    onSecondaryContainer = SlateDarkOnSecondaryContainer,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = TealLightPrimary,
    onPrimary = TealLightOnPrimary,
    primaryContainer = TealLightPrimaryContainer,
    onPrimaryContainer = TealLightOnPrimaryContainer,
    secondary = SlateLightSecondary,
    onSecondary = SlateLightOnSecondary,
    secondaryContainer = SlateLightSecondaryContainer,
    onSecondaryContainer = SlateLightOnSecondaryContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

object PackStatusColors {
    data class Colors(
        val text: Color,
        val container: Color,
        val onContainer: Color
    )

    fun forStatus(status: PackStatus, isDark: Boolean): Colors {
        return when (status) {
            PackStatus.FULLY_PACKED -> if (isDark) {
                Colors(StatusPackedDark, StatusPackedContainerDark, StatusPackedOnContainerDark)
            } else {
                Colors(StatusPackedLight, StatusPackedContainerLight, StatusPackedOnContainerLight)
            }
            PackStatus.LEFTOVER -> if (isDark) {
                Colors(StatusLeftoverDark, StatusLeftoverContainerDark, StatusLeftoverOnContainerDark)
            } else {
                Colors(StatusLeftoverLight, StatusLeftoverContainerLight, StatusLeftoverOnContainerLight)
            }
            PackStatus.PACKAGE_TOO_LARGE -> if (isDark) {
                Colors(StatusTooLargeDark, StatusTooLargeContainerDark, StatusTooLargeOnContainerDark)
            } else {
                Colors(StatusTooLargeLight, StatusTooLargeContainerLight, StatusTooLargeOnContainerLight)
            }
        }
    }
}

@Composable
fun PackRatioTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}