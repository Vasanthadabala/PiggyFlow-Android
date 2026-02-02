package com.piggylabs.piggyflow.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val container: Color,
    val text: Color,
    val red: Color,
    val green: Color,
    val blue: Color
)

val LightAppColors = AppColors(
    background = Color.White,
    container = Color.Gray.copy(alpha = 0.15f),
    text = Color.Black,
    red = Color.Red,
    green = Color(0xFF27C152),
    blue = Color(0XFF3f8efc)
)

val DarkAppColors = AppColors(
    background = Color.Black,
    container = Color.Gray.copy(alpha = 0.2f),
    text = Color.White,
    red = Color.Red,
    green = Color(0xFF27C152),
    blue = Color(0XFF3f8efc)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
