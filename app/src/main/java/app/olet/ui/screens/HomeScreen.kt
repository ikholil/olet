package app.olet.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.olet.data.AccountWithBalance
import app.olet.data.UserProfile
import app.olet.ui.MainViewModel
import app.olet.ui.components.AppCard
import app.olet.ui.components.Avatar
import app.olet.ui.components.DonutChart
import app.olet.ui.components.EmptyState
import app.olet.ui.components.IconBadge
import app.olet.ui.components.SectionHeader
import app.olet.ui.components.Slice
import app.olet.ui.components.TransactionRow
import app.olet.ui.navigation.Routes
import app.olet.ui.navigation.navigateTab
import app.olet.ui.theme.ExpenseRed
import app.olet.ui.theme.IncomeGreen
import app.olet.ui.theme.LocalExtraColors
import app.olet.ui.theme.MoneyStyle
import app.olet.ui.util.color
import app.olet.ui.util.colorOf
import app.olet.ui.util.formatMoney
import app.olet.ui.util.icon
import app.olet.ui.util.iconFor
import app.olet.ui.util.initialsOf
import java.time.LocalTime
import kotlin.math.roundToInt

private const val HIDDEN = "••••••"

@Composable
fun HomeScreen(vm: MainViewModel, profile: UserProfile, nav: NavHostController) {
    val balance by vm.totalBalance.collectAsStateWithLifecycle()
    val month by vm.thisMonth.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val txs by vm.transactions.collectAsStateWithLifecycle()
    val loans by vm.loanSummary.collectAsStateWithLifecycle()
    val spending by vm.monthSpending.collectAsStateWithLifecycle()
    var hidden by rememberSaveable { mutableStateOf(false) }
    val cur = profile.currency
    fun money(v: Double) = if (hidden) HIDDEN else formatMoney(v, cur)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.clip(CircleShape).clickable { nav.navigate(Routes.SETTINGS) }) {
                    Avatar(initialsOf(profile.name.ifBlank { "Friend" }), size = 46.dp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(greeting(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        profile.name.ifBlank { "Friend" },
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    onClick = { nav.navigate(Routes.SETTINGS) },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.size(46.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Settings, "Settings") }
                }
            }
        }

        item { BalanceCard(money(balance), money(month.income), money(month.expense), hidden) { hidden = !hidden } }

        if (spending.isNotEmpty()) {
            item { SpendingCard(spending, cur, hidden) { navigateTab(nav, Routes.TRANSACTIONS) } }
        }

        item {
            SectionHeader("My accounts", action = "Manage", onAction = { nav.navigate(Routes.ACCOUNTS) })
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(accounts, key = { it.account.id }) { AccountCard(it, money(it.balance)) { nav.navigate(Routes.ACCOUNTS) } }
                item { AddAccountCard { nav.navigate(Routes.ACCOUNTS) } }
            }
        }

        if (loans.toReceive > 0 || loans.toPay > 0) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LoanTile("To receive", money(loans.toReceive), IncomeGreen, Icons.Rounded.ArrowDownward, Modifier.weight(1f)) {
                        navigateTab(nav, Routes.LOANS)
                    }
                    LoanTile("To pay", money(loans.toPay), ExpenseRed, Icons.Rounded.ArrowUpward, Modifier.weight(1f)) {
                        navigateTab(nav, Routes.LOANS)
                    }
                }
            }
        }

        item {
            SectionHeader("Recent activity", action = if (txs.isNotEmpty()) "See all" else null) {
                navigateTab(nav, Routes.TRANSACTIONS)
            }
        }
        if (txs.isEmpty()) {
            item {
                EmptyState(
                    Icons.AutoMirrored.Rounded.ReceiptLong,
                    "No transactions yet",
                    "Tap the + button below to record your first expense or income.",
                )
            }
        } else {
            items(txs.take(5), key = { it.tx.id }) { t ->
                TransactionRow(t, cur, onClick = { nav.navigate(Routes.tx(t.tx.type, t.tx.id)) }, hideAmount = hidden, showDate = true)
            }
        }
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning 👋"
    in 12..16 -> "Good afternoon 👋"
    in 17..20 -> "Good evening 👋"
    else -> "Hello 👋"
}

@Composable
private fun BalanceCard(balance: String, income: String, expense: String, hidden: Boolean, onToggle: () -> Unit) {
    val extra = LocalExtraColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(extra.heroGradient)
    ) {
        Box(Modifier.align(Alignment.TopEnd).offset(x = 70.dp, y = (-80).dp).size(220.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.07f)))
        Box(Modifier.align(Alignment.BottomStart).offset(x = (-60).dp, y = 70.dp).size(160.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)))
        Column(Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total balance", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        if (hidden) "Show amounts" else "Hide amounts",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            AnimatedContent(balance, label = "bal") { b ->
                Text(
                    b,
                    style = MaterialTheme.typography.displaySmall.merge(MoneyStyle),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowPill(Icons.Rounded.ArrowDownward, "Income", income, Modifier.weight(1f))
                FlowPill(Icons.Rounded.ArrowUpward, "Expense", expense, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Text("This month", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun FlowPill(icon: ImageVector, label: String, value: String, modifier: Modifier) {
    Row(
        modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.14f)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(Color.White),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = if (label == "Income") IncomeGreen else ExpenseRed, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
            Text(value, style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SpendingCard(spending: List<MainViewModel.CategorySpend>, cur: String, hidden: Boolean, onClick: () -> Unit) {
    val total = spending.sumOf { it.amount }
    val top = spending.take(4)
    val slices = buildList {
        top.forEach { add(Slice(it.name, it.amount, colorOf(it.color))) }
        val rest = spending.drop(4).sumOf { it.amount }
        if (rest > 0) add(Slice("Others", rest, Color(0xFFB8BCCB)))
    }
    AppCard(Modifier.fillMaxWidth(), onClick = onClick, contentPadding = PaddingValues(18.dp)) {
        Text("Spending this month", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            DonutChart(slices, Modifier.size(124.dp), thickness = 16.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (hidden) "•••" else formatMoney(total, cur),
                        style = MaterialTheme.typography.titleSmall.merge(MoneyStyle),
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                top.forEach { c ->
                    val pct = if (total > 0) c.amount / total else 0.0
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(iconFor(c.icon), null, tint = colorOf(c.color), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(c.name, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${(pct * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { pct.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(5.dp),
                            color = colorOf(c.color),
                            trackColor = colorOf(c.color).copy(alpha = 0.12f),
                            drawStopIndicator = {},
                            gapSize = 0.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountCard(item: AccountWithBalance, balance: String, onClick: () -> Unit) {
    val tint = item.account.type.color()
    Surface(onClick = onClick, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.width(158.dp)) {
        Box {
            Box(Modifier.align(Alignment.TopEnd).offset(x = 30.dp, y = (-30).dp).size(90.dp).clip(CircleShape).background(tint.copy(alpha = 0.10f)))
            Column(Modifier.padding(16.dp)) {
                IconBadge(item.account.type.icon(), tint, size = 38.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    item.account.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(balance, style = MaterialTheme.typography.titleMedium.merge(MoneyStyle), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun AddAccountCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        modifier = Modifier.width(110.dp).height(124.dp),
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.height(8.dp))
            Text("Add account", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun LoanTile(label: String, value: String, tint: Color, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(24.dp), color = tint.copy(alpha = 0.09f), modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon, tint, size = 30.dp)
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.merge(MoneyStyle), color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Loans", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun Stat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleLarge.merge(MoneyStyle), color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
