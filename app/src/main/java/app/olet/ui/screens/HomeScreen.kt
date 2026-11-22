package app.olet.ui.screens

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
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import app.olet.data.TxType
import app.olet.data.UserProfile
import app.olet.ui.MainViewModel
import app.olet.ui.components.AppCard
import app.olet.ui.components.Avatar
import app.olet.ui.components.EmptyState
import app.olet.ui.components.IconBadge
import app.olet.ui.components.SectionHeader
import app.olet.ui.components.TransactionRow
import app.olet.ui.navigation.Routes
import app.olet.ui.navigation.navigateTab
import app.olet.ui.theme.Amber
import app.olet.ui.theme.ExpenseRed
import app.olet.ui.theme.IncomeGreen
import app.olet.ui.theme.LocalExtraColors
import app.olet.ui.theme.MoneyStyle
import app.olet.ui.util.color
import app.olet.ui.util.formatMoney
import app.olet.ui.util.icon
import app.olet.ui.util.initialsOf
import java.time.LocalTime

@Composable
fun HomeScreen(vm: MainViewModel, profile: UserProfile, nav: NavHostController) {
    val balance by vm.totalBalance.collectAsStateWithLifecycle()
    val month by vm.thisMonth.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val txs by vm.transactions.collectAsStateWithLifecycle()
    val loans by vm.loanSummary.collectAsStateWithLifecycle()
    val cur = profile.currency

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.tx(TxType.EXPENSE)) },
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("Add") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
            )
        },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            greeting(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            profile.name.ifBlank { "Friend" },
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Surface(onClick = { navigateTab(nav, Routes.SETTINGS) }, shape = CircleShape) {
                        Avatar(initialsOf(profile.name.ifBlank { "Friend" }))
                    }
                }
            }
            item { BalanceCard(balance, month.income, month.expense, cur) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    QuickAction("Expense", Icons.Rounded.Remove, ExpenseRed) { nav.navigate(Routes.tx(TxType.EXPENSE)) }
                    QuickAction("Income", Icons.Rounded.Add, IncomeGreen) { nav.navigate(Routes.tx(TxType.INCOME)) }
                    QuickAction("Loans", Icons.Rounded.Handshake, Amber) { navigateTab(nav, Routes.LOANS) }
                    QuickAction("Invest", Icons.AutoMirrored.Rounded.TrendingUp, MaterialTheme.colorScheme.primary) {
                        navigateTab(nav, Routes.INVESTMENTS)
                    }
                }
            }
            item {
                SectionHeader("Accounts", action = "Manage", onAction = { nav.navigate(Routes.ACCOUNTS) })
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(accounts, key = { it.account.id }) { AccountChip(it, cur) { nav.navigate(Routes.ACCOUNTS) } }
                    item { AddAccountChip { nav.navigate(Routes.ACCOUNTS) } }
                }
            }
            if (loans.toReceive > 0 || loans.toPay > 0) {
                item {
                    AppCard(Modifier.fillMaxWidth(), onClick = { navigateTab(nav, Routes.LOANS) }) {
                        Text("Loans", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Row {
                            Stat("You'll receive", formatMoney(loans.toReceive, cur), IncomeGreen, Modifier.weight(1f))
                            Stat("You owe", formatMoney(loans.toPay, cur), ExpenseRed, Modifier.weight(1f))
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
                        "Tap Add to record your first expense or income.",
                    )
                }
            } else {
                items(txs.take(6), key = { it.tx.id }) { t ->
                    TransactionRow(t, cur, onClick = { nav.navigate(Routes.tx(t.tx.type, t.tx.id)) })
                }
            }
        }
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..20 -> "Good evening"
    else -> "Hello"
}

@Composable
private fun BalanceCard(balance: Double, income: Double, expense: Double, cur: String) {
    val extra = LocalExtraColors.current
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(extra.heroGradient)
    ) {
        // decorative circles
        Box(
            Modifier.align(Alignment.TopEnd).offset(x = 60.dp, y = (-70).dp).size(200.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.06f))
        )
        Column(Modifier.padding(22.dp)) {
            Text("Total balance", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(6.dp))
            Text(
                formatMoney(balance, cur),
                style = MaterialTheme.typography.displaySmall.merge(MoneyStyle),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(20.dp))
            Text("This month", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowPill(Icons.Rounded.ArrowDownward, "Income", formatMoney(income, cur), Modifier.weight(1f))
                FlowPill(Icons.Rounded.ArrowUpward, "Expense", formatMoney(expense, cur), Modifier.weight(1f))
            }
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
            Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(22.dp)).clickable(onClick = onClick).padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(64.dp).clip(RoundedCornerShape(22.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow),
            contentAlignment = Alignment.Center,
        ) { IconBadge(icon, tint, size = 40.dp) }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun AccountChip(item: AccountWithBalance, cur: String, onClick: () -> Unit) {
    AppCard(Modifier.width(160.dp), onClick = onClick) {
        IconBadge(item.account.type.icon(), item.account.type.color(), size = 38.dp)
        Spacer(Modifier.height(12.dp))
        Text(
            item.account.name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            formatMoney(item.balance, cur),
            style = MaterialTheme.typography.titleMedium.merge(MoneyStyle),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AddAccountChip(onClick: () -> Unit) {
    AppCard(Modifier.width(120.dp).height(118.dp), onClick = onClick) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text("Add account", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun Stat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.merge(MoneyStyle),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
