package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// SYLO Signature Palette: Royal Violet, Electric Purple & Saffron Orange
val SyloViolet = Color(0xFF7C3AED)
val SyloVioletLight = Color(0xFFA78BFA)
val SyloPurpleDeep = Color(0xFF4C1D95)
val SyloMagenta = Color(0xFFD946EF)
val SyloSaffron = Color(0xFFFF6B2B)
val SyloSaffronWarm = Color(0xFFFF8A4C)
val SyloMarigold = Color(0xFFF59E0B)
val SyloCrimsonLive = Color(0xFFFF2E55)
val SyloEmerald = Color(0xFF10B981)
val SyloSkyVerified = Color(0xFF38BDF8)

// Dark Mode Surfaces (Rich Obsidian Plum)
val SyloDarkBg = Color(0xFF0D0916)
val SyloDarkSurface = Color(0xFF151023)
val SyloDarkSurfaceVariant = Color(0xFF211936)
val SyloDarkOutline = Color(0xFF352A52)
val SyloDarkOnBg = Color(0xFFF6F3FF)
val SyloDarkOnSurfaceVariant = Color(0xFFB7ACD4)

// Clean White Mode Surfaces
val SyloLightBg = Color(0xFFFAF8FF)
val SyloLightSurface = Color(0xFFFFFFFF)
val SyloLightSurfaceVariant = Color(0xFFF1ECFD)
val SyloLightOutline = Color(0xFFDCD3F5)
val SyloLightOnBg = Color(0xFF19112A)
val SyloLightOnSurfaceVariant = Color(0xFF5D5278)

val SyloBrandGradient = Brush.linearGradient(
    colors = listOf(SyloViolet, SyloMagenta, SyloSaffron)
)

val SyloLiveGradient = Brush.linearGradient(
    colors = listOf(SyloCrimsonLive, SyloSaffron)
)

val SyloGoldGradient = Brush.linearGradient(
    colors = listOf(SyloSaffron, SyloMarigold)
)

val SyloHeroScrim = Brush.verticalGradient(
    colors = listOf(
        Color(0x330D0916),
        Color(0xB30D0916),
        Color(0xFF0D0916)
    )
)
