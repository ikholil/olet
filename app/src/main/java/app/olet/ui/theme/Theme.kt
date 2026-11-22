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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.olet.R
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

val Jakarta = FontFamily(
    Font(R.font.jakarta_regular, FontWeight.Normal),
    Font(R.font.jakarta_medium, FontWeight.Medium),
    Font(R.font.jakarta_semibold, FontWeight.SemiBold),
    Font(R.font.jakarta_bold, FontWeight.Bold),
    Font(R.font.jakarta_extrabold, FontWeight.ExtraBold),
)

private fun TextStyle.j(weight: FontWeight? = null, spacing: Float? = null) =
    copy(fontFamily = Jakarta, fontWeight = weight ?: fontWeight, letterSpacing = spacing?.sp ?: letterSpacing)

private val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.j(FontWeight.ExtraBold, -1f),
        displayMedium = displayMedium.j(FontWeight.ExtraBold, -1f),
        displaySmall = displaySmall.j(FontWeight.ExtraBold, -0.8f),
        headlineLarge = headlineLarge.j(FontWeight.ExtraBold, -0.6f),
        headlineMedium = headlineMedium.j(FontWeight.Bold, -0.4f),
        headlineSmall = headlineSmall.j(FontWeight.Bold, -0.3f),
        titleLarge = titleLarge.j(FontWeight.Bold, -0.2f),
        titleMedium = titleMedium.j(FontWeight.SemiBold, 0f),
        titleSmall = titleSmall.j(FontWeight.SemiBold, 0f),
        bodyLarge = bodyLarge.j(FontWeight.Medium, 0f),
        bodyMedium = bodyMedium.j(FontWeight.Medium, 0f),
        bodySmall = bodySmall.j(FontWeight.Medium, 0f),
        labelLarge = labelLarge.j(FontWeight.SemiBold, 0f),
        labelMedium = labelMedium.j(FontWeight.SemiBold, 0f),
        labelSmall = labelSmall.j(FontWeight.SemiBold, 0.2f),
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

val MoneyStyle = TextStyle(fontFamily = Jakarta, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)

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
