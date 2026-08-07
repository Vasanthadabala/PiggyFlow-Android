package com.piggylabs.piggyflow.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val container: Color,
    val text: Color,
    val red: Color,
    val green: Color,
    val blue: Color,
    /** Card surface. Sits one step above [background] so cards read without a border. */
    val surface: Color,
    /** Subtle fill for rows/chips nested inside a card. One step above [surface]. */
    val surfaceMuted: Color,
    /** Secondary text. Replaces hardcoded Color.Gray so it stays legible in both themes. */
    val textMuted: Color,
    /** Brand green, tuned per theme for contrast. */
    val accent: Color,
    /** Tinted accent fill for pills and action bars. */
    val accentSoft: Color,
    /** Text/icon colour to use on top of [accentSoft]. */
    val onAccentSoft: Color,
    val positive: Color,
    val positiveSoft: Color,
    val negative: Color,
    val negativeSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val info: Color,
    val infoSoft: Color,
    /** Hero gradient stops used by the balance / net-worth cards. */
    val heroGradient: List<Color>,
    /** Categorical series colours for donuts and legends, ordered by prominence. */
    val chartPalette: List<Color>
)

val LightAppColors = AppColors(
    background = Color(0xFFF7F8F7),
    // Kept close to the original translucent grey so screens still on `container` don't flatten.
    container = Color(0xFFECEFED),
    text = Color(0xFF111813),
    red = Color(0xFFDC2626),
    green = Color(0xFF27C152),
    blue = Color(0XFF3f8efc),
    surface = Color.White,
    surfaceMuted = Color(0xFFF1F4F2),
    textMuted = Color(0xFF6B7280),
    accent = Color(0xFF15803D),
    accentSoft = Color(0xFFE8F5E9),
    onAccentSoft = Color(0xFF15803D),
    positive = Color(0xFF15803D),
    positiveSoft = Color(0xFFE8F5E9),
    negative = Color(0xFFDC2626),
    negativeSoft = Color(0xFFFEE2E2),
    warning = Color(0xFFD97706),
    warningSoft = Color(0xFFFEF3C7),
    info = Color(0xFF0284C7),
    infoSoft = Color(0xFFE0F2FE),
    heroGradient = listOf(Color(0xFF0F5A3E), Color(0xFF15803D)),
    chartPalette = listOf(
        Color(0xFF15803D),
        Color(0xFF3B82F6),
        Color(0xFF8B5CF6),
        Color(0xFFF97316),
        Color(0xFF0EA5E9),
        Color(0xFFEC4899),
        Color(0xFF84CC16),
        Color(0xFF6B7280)
    )
)

val DarkAppColors = AppColors(
    background = Color(0xFF0B0F0D),
    container = Color(0xFF232A26),
    text = Color(0xFFF2F5F3),
    red = Color(0xFFF87171),
    green = Color(0xFF34D399),
    blue = Color(0XFF60A5FA),
    surface = Color(0xFF161C18),
    surfaceMuted = Color(0xFF1F2723),
    textMuted = Color(0xFF9AA5A0),
    accent = Color(0xFF34D399),
    accentSoft = Color(0xFF15291F),
    onAccentSoft = Color(0xFF6EE7B7),
    positive = Color(0xFF34D399),
    positiveSoft = Color(0xFF15291F),
    negative = Color(0xFFF87171),
    negativeSoft = Color(0xFF31191A),
    warning = Color(0xFFFBBF24),
    warningSoft = Color(0xFF2E2515),
    info = Color(0xFF60A5FA),
    infoSoft = Color(0xFF16232F),
    heroGradient = listOf(Color(0xFF0C4230), Color(0xFF12603A)),
    chartPalette = listOf(
        Color(0xFF34D399),
        Color(0xFF60A5FA),
        Color(0xFFA78BFA),
        Color(0xFFFB923C),
        Color(0xFF38BDF8),
        Color(0xFFF472B6),
        Color(0xFFA3E635),
        Color(0xFF9AA5A0)
    )
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
