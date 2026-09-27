package com.example.fivestones.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.fivestones.data.BoardTheme

private val LightColors = lightColorScheme(
    primary = BronzeDeep,
    onPrimary = BronzeLightOn,
    primaryContainer = Color(0xFFEADBC0),
    onPrimaryContainer = Color(0xFF3A2A12),
    secondary = InkMuted,
    onSecondary = ParchmentSurface,
    background = Parchment,
    onBackground = Ink,
    surface = ParchmentSurface,
    onSurface = Ink,
    surfaceVariant = ParchmentVariant,
    onSurfaceVariant = InkMuted,
    surfaceContainerHigh = Color(0xFFEFE8DC),
    surfaceContainerHighest = Color(0xFFE6DDCE),
    outline = Color(0xFFB5A994),
    outlineVariant = Color(0xFFDCD2C2),
    error = MutedRedLight,
)

private val DarkColors = darkColorScheme(
    primary = BronzeGlow,
    onPrimary = BronzeDarkOn,
    primaryContainer = Color(0xFF3B2F1E),
    onPrimaryContainer = Color(0xFFEBD6B2),
    secondary = BoneMuted,
    onSecondary = Slate,
    background = Slate,
    onBackground = Bone,
    surface = SlateSurface,
    onSurface = Bone,
    surfaceVariant = SlateVariant,
    onSurfaceVariant = BoneMuted,
    surfaceContainerHigh = Color(0xFF212427),
    surfaceContainerHighest = Color(0xFF2A2E32),
    outline = Color(0xFF4F545A),
    outlineVariant = Color(0xFF32363B),
    error = MutedRedDark,
)

val LocalBoardPalette = staticCompositionLocalOf { BoardPalettes.ClassicWood }

@Composable
fun FiveStonesTheme(
    darkTheme: Boolean,
    boardTheme: BoardTheme = BoardTheme.CLASSIC_WOOD,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalBoardPalette provides BoardPalettes.of(boardTheme)) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            content = content,
        )
    }
}
