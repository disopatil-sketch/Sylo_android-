package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SyloDarkColorScheme = darkColorScheme(
    primary = SyloVioletLight,
    onPrimary = Color(0xFF1A0836),
    primaryContainer = SyloPurpleDeep,
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = SyloSaffronWarm,
    onSecondary = Color(0xFF2B0E00),
    secondaryContainer = Color(0xFF4F200A),
    onSecondaryContainer = Color(0xFFFFDBC9),
    tertiary = SyloMagenta,
    onTertiary = Color(0xFF2E0538),
    background = SyloDarkBg,
    onBackground = SyloDarkOnBg,
    surface = SyloDarkSurface,
    onSurface = SyloDarkOnBg,
    surfaceVariant = SyloDarkSurfaceVariant,
    onSurfaceVariant = SyloDarkOnSurfaceVariant,
    outline = SyloDarkOutline,
    error = Color(0xFFFF6B81),
    onError = Color(0xFF33000B)
)

private val SyloLightColorScheme = lightColorScheme(
    primary = SyloViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF2E1065),
    secondary = SyloSaffron,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE4D6),
    onSecondaryContainer = Color(0xFF4A1800),
    tertiary = SyloMagenta,
    onTertiary = Color.White,
    background = SyloLightBg,
    onBackground = SyloLightOnBg,
    surface = SyloLightSurface,
    onSurface = SyloLightOnBg,
    surfaceVariant = SyloLightSurfaceVariant,
    onSurfaceVariant = SyloLightOnSurfaceVariant,
    outline = SyloLightOutline,
    error = Color(0xFFDC2626),
    onError = Color.White
)

private val SyloShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun SyloTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SyloDarkColorScheme else SyloLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SyloTypography,
        shapes = SyloShapes,
        content = content
    )
}
