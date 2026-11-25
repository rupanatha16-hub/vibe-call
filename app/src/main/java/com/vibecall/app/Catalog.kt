package com.vibecall.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class VTheme(
    val name: String,
    val a: Color,
    val b: Color,
    val bg: Color,
    val surface: Color,
    val pro: Boolean = false
)

val Themes = listOf(
    VTheme("Neon Pulse", Color(0xFF8B5CF6), Color(0xFF52C8FF), Color(0xFF07070C), Color(0xFF15151F)),
    VTheme("Royal Gold", Color(0xFFD4AF37), Color(0xFFF5E08A), Color(0xFF0B0905), Color(0xFF1A150B), true),
    VTheme("Rose Aura", Color(0xFFFB7185), Color(0xFFFDBA74), Color(0xFF12080B), Color(0xFF22111A)),
    VTheme("Cyber Green", Color(0xFF22C55E), Color(0xFF38BDF8), Color(0xFF030A05), Color(0xFF0B1A10)),
    VTheme("Ocean Glass", Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFF06101A), Color(0xFF0F1E2E)),
    VTheme("Obsidian", Color(0xFFF2F2F7), Color(0xFF8E8E93), Color(0xFF000000), Color(0xFF121214)),
    VTheme("Aurora", Color(0xFF8B5CF6), Color(0xFF34D399), Color(0xFF080A14), Color(0xFF131626), true)
)

fun currentTheme(): VTheme = Themes[Prefs.theme.coerceIn(0, Themes.lastIndex)]

/** 12 incoming call designs. The drawing code for each lives in CallUi.kt, same order. */
val IncomingStyles = listOf(
    "Aurora Glass", "Neon Ring", "Pulse Halo", "Liquid Orb", "Minimal Mono", "Royal Card",
    "Cyber Grid", "Starfield", "Soft Wave", "Split Light", "Ticket Pass", "Spotlight"
)

/** 12 outgoing call designs. */
val OutgoingStyles = listOf(
    "Calm Dark", "Orbit", "Glass Panel", "Gradient Fade", "Big Name", "Compact Bar",
    "Ripple", "Duo Tone", "Neon Frame", "Paper Card", "Radar", "Night Sky"
)

/** 12 call-button icon styles (speaker, mute, keypad, record, ...). */
val IconStyles = listOf(
    "Solid Circle", "Outline Ring", "Glass Orb", "Neon Glow", "Soft Square", "Squircle Fill",
    "Gradient Disc", "Minimal Line", "Pill Label", "Double Ring", "Cut Corner", "Floating Shadow"
)

data class NumStyle(
    val name: String,
    val family: FontFamily,
    val weight: FontWeight,
    val spacing: TextUnit
)

/** How the caller name / number is written on call screens. */
val NumberStyles = listOf(
    NumStyle("Bold Grotesk", FontFamily.SansSerif, FontWeight.Bold, 0.sp),
    NumStyle("Thin Wide", FontFamily.SansSerif, FontWeight.Thin, 4.sp),
    NumStyle("Mono Digital", FontFamily.Monospace, FontWeight.Medium, 2.sp),
    NumStyle("Classic Serif", FontFamily.Serif, FontWeight.SemiBold, 0.sp),
    NumStyle("Soft Cursive", FontFamily.Cursive, FontWeight.Normal, 1.sp),
    NumStyle("Spaced Caps", FontFamily.SansSerif, FontWeight.Medium, 6.sp),
    NumStyle("Heavy Tight", FontFamily.SansSerif, FontWeight.Black, (-1).sp),
    NumStyle("Light Serif", FontFamily.Serif, FontWeight.Light, 2.sp),
    NumStyle("Typewriter", FontFamily.Monospace, FontWeight.Bold, 0.sp),
    NumStyle("Round Clean", FontFamily.Default, FontWeight.Normal, 1.sp)
)
