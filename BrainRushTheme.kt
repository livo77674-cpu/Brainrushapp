package com.brainrush.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.brainrush.app.domain.models.AppThemeMode
import com.brainrush.app.ui.colors.BrainRushColors
import com.brainrush.app.ui.typography.BrainRushTypography

@Composable
fun BrainRushTheme(themeMode: AppThemeMode = AppThemeMode.OBSIDIAN, content: @Composable () -> Unit) {
    val background = if (themeMode == AppThemeMode.OBSIDIAN) BrainRushColors.PitchBlack else BrainRushColors.WarmEspressoBg
    val surface = if (themeMode == AppThemeMode.OBSIDIAN) BrainRushColors.DarkGraphite else BrainRushColors.DarkBrownAccent
    val colorScheme = darkColorScheme(
        primary = BrainRushColors.OrangeAccent,
        onPrimary = BrainRushColors.PitchBlack,
        primaryContainer = BrainRushColors.DarkBrownAccent,
        onPrimaryContainer = BrainRushColors.OrangeAccent,
        secondary = BrainRushColors.SecondaryText,
        onSecondary = BrainRushColors.PitchBlack,
        background = background,
        onBackground = BrainRushColors.WhiteText,
        surface = surface,
        onSurface = BrainRushColors.WhiteText,
        surfaceVariant = BrainRushColors.ElevatedGraphite,
        onSurfaceVariant = BrainRushColors.SecondaryText,
        outline = BrainRushColors.SubtleBorder,
        error = BrainRushColors.ErrorRed,
        onError = BrainRushColors.WhiteText
    )
    MaterialTheme(colorScheme = colorScheme, typography = BrainRushTypography, content = content)
}
