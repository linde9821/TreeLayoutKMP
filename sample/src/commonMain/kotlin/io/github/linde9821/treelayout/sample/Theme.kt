package io.github.linde9821.treelayout.sample

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Slate50: Color = Color(0xFFF8FAFC)
private val Slate100: Color = Color(0xFFF1F5F9)
private val Slate200: Color = Color(0xFFE2E8F0)
private val Slate400: Color = Color(0xFF94A3B8)
private val Slate500: Color = Color(0xFF64748B)
private val Slate700: Color = Color(0xFF334155)
private val Slate800: Color = Color(0xFF1E293B)
private val Slate900: Color = Color(0xFF0F172A)

private val Teal400: Color = Color(0xFF2DD4BF)
private val Teal500: Color = Color(0xFF14B8A6)
private val Teal600: Color = Color(0xFF0D9488)

private val AppColors: Colors = darkColors(
    primary = Teal400,
    primaryVariant = Teal600,
    secondary = Teal500,
    background = Slate900,
    surface = Slate800,
    onPrimary = Slate900,
    onSecondary = Slate900,
    onBackground = Slate100,
    onSurface = Slate200,
    error = Color(0xFFF87171),
    onError = Slate900,
)

private val AppTypography: Typography = Typography(
    h6 = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Slate50),
    subtitle1 = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Slate200),
    subtitle2 = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Slate400),
    body1 = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, color = Slate200),
    body2 = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, color = Slate400),
    button = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp),
    caption = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp, color = Slate500),
)

@Composable
public fun TreeLayoutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = AppColors,
        typography = AppTypography,
        shapes = Shapes(),
        content = content,
    )
}
