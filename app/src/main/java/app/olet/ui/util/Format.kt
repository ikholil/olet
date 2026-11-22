package app.olet.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import app.olet.data.AccountType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val moneyFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))

fun formatMoney(amount: Double, currency: String, signed: Boolean = false): String {
    val sign = when {
        signed && amount > 0 -> "+"
        amount < 0 -> "-"
        else -> ""
    }
    return "$sign$currency${moneyFormat.format(abs(amount))}"
}

/** Parses user-entered amount; returns null when invalid. */
fun parseAmount(text: String): Double? =
    text.replace(",", "").trim().toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 }

fun amountInput(d: Double): String = if (d == 0.0) "" else moneyFormat.format(d).replace(",", "")

private val zone: ZoneId get() = ZoneId.systemDefault()

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()
fun LocalDate.toMillis(): Long = atStartOfDay(zone).toInstant().toEpochMilli()

/** Material DatePicker uses UTC millis. */
fun Long.utcToLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC")).toLocalDate()
fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

private val dateFmt = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)
private val dayFmt = DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.US)
private val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)

fun Long.formatDate(): String = toLocalDate().format(dateFmt)
fun LocalDate.formatMonth(): String = format(monthFmt)

fun LocalDate.friendlyDay(): String {
    val today = LocalDate.now()
    return when (this) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> if (year == today.year) format(dayFmt) else format(dateFmt)
    }
}

val categoryIcons: Map<String, ImageVector> = linkedMapOf(
    "food" to Icons.Rounded.Restaurant,
    "transport" to Icons.Rounded.DirectionsCar,
    "utilities" to Icons.Rounded.Bolt,
    "entertainment" to Icons.Rounded.Movie,
    "shopping" to Icons.Rounded.ShoppingBag,
    "health" to Icons.Rounded.LocalHospital,
    "education" to Icons.Rounded.School,
    "home" to Icons.Rounded.Home,
    "travel" to Icons.Rounded.Flight,
    "cafe" to Icons.Rounded.LocalCafe,
    "clothes" to Icons.Rounded.Checkroom,
    "games" to Icons.Rounded.SportsEsports,
    "pets" to Icons.Rounded.Pets,
    "phone" to Icons.Rounded.PhoneAndroid,
    "salary" to Icons.Rounded.Payments,
    "business" to Icons.Rounded.Work,
    "gift" to Icons.Rounded.CardGiftcard,
    "invest" to Icons.AutoMirrored.Rounded.TrendingUp,
    "savings" to Icons.Rounded.Savings,
    "other" to Icons.Rounded.Category,
)

fun iconFor(key: String?): ImageVector = categoryIcons[key] ?: Icons.Rounded.Category

val paletteColors: List<Long> = listOf(
    0xFFFF7A59, 0xFF3D8BFF, 0xFFFFB020, 0xFFE5508C, 0xFF9B59F6,
    0xFF1FB57A, 0xFF00B8D9, 0xFFFF5A5F, 0xFF5B4CF0, 0xFF7C8598,
)

fun colorOf(value: Long?): Color = Color(value ?: 0xFF7C8598)

fun AccountType.icon(): ImageVector = when (this) {
    AccountType.CASH -> Icons.Rounded.AccountBalanceWallet
    AccountType.BANK -> Icons.Rounded.AccountBalance
    AccountType.MOBILE -> Icons.Rounded.PhoneAndroid
    AccountType.CARD -> Icons.Rounded.CreditCard
    AccountType.OTHER -> Icons.Rounded.Savings
}

fun AccountType.color(): Color = when (this) {
    AccountType.CASH -> Color(0xFF1FB57A)
    AccountType.BANK -> Color(0xFF3D8BFF)
    AccountType.MOBILE -> Color(0xFFE5508C)
    AccountType.CARD -> Color(0xFF9B59F6)
    AccountType.OTHER -> Color(0xFFFFB020)
}

fun initialsOf(name: String): String =
    name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2)
        .joinToString("") { it.first().uppercase() }.ifEmpty { "O" }
