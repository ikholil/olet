package app.olet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.olet.data.ThemeMode

val Brand = Color(0xFF5B4CF0)
val BrandDeep = Color(0xFF3B2BB8)
val BrandSoft = Color(0xFF8E83FF)
val IncomeGreen = Color(0xFF16B47E)
val ExpenseRed = Color(0xFFF04E5E)
val Amber = Color(0xFFFFA928)

private val LightColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7E4FF),
    onPrimaryContainer = Color(0xFF1C1366),
    secondary = Color(0xFF6B6F86),
    secondaryContainer = Color(0xFFECEDF5),
    onSecondaryContainer = Color(0xFF22243A),
    background = Color(0xFFF6F6FB),
    onBackground = Color(0xFF14152A),
    surface = Color(0xFFF6F6FB),
    onSurface = Color(0xFF14152A),
    surfaceVariant = Color(0xFFECEDF5),
    onSurfaceVariant = Color(0xFF6B6F86),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFF0F0F8),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFE9E9F3),
    outline = Color(0xFFD3D4E2),
    outlineVariant = Color(0xFFE6E7F0),
    error = ExpenseRed,
)

private val DarkColors = darkColorScheme(
    primary = BrandSoft,
    onPrimary = Color(0xFF14105A),
    primaryContainer = Color(0xFF2E2680),
    onPrimaryContainer = Color(0xFFE7E4FF),
    secondary = Color(0xFFA6A9C0),
    secondaryContainer = Color(0xFF23243A),
    onSecondaryContainer = Color(0xFFE2E3F0),
    background = Color(0xFF0F0F17),
    onBackground = Color(0xFFEDEDF7),
    surface = Color(0xFF0F0F17),
    onSurface = Color(0xFFEDEDF7),
    surfaceVariant = Color(0xFF1E1F2E),
    onSurfaceVariant = Color(0xFFA6A9C0),
    surfaceContainerLowest = Color(0xFF14141F),
    surfaceContainerLow = Color(0xFF181824),
    surfaceContainer = Color(0xFF1C1C2A),
    surfaceContainerHigh = Color(0xFF1C1C2A),
    surfaceContainerHighest = Color(0xFF272838),
    outline = Color(0xFF3A3B52),
    outlineVariant = Color(0xFF2A2B3D),
    error = Color(0xFFFF6B78),
)

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Immutable
data class ExtraColors(val heroGradient: Brush, val isDark: Boolean)

val LocalExtraColors = staticCompositionLocalOf {
    ExtraColors(Brush.linearGradient(listOf(Brand, BrandDeep)), false)
}

val MoneyStyle = TextStyle(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp)

@Composable
fun OletTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val extra = ExtraColors(
        heroGradient = if (dark) Brush.linearGradient(listOf(Color(0xFF4A3DD6), Color(0xFF241A86)))
        else Brush.linearGradient(listOf(Color(0xFF6D5DF6), BrandDeep)),
        isDark = dark,
    )
    androidx.compose.runtime.CompositionLocalProvider(LocalExtraColors provides extra) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
