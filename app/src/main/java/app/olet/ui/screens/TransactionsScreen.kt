package app.olet.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.olet.data.TransactionDetail
import app.olet.data.TxType
import app.olet.data.UserProfile
import app.olet.ui.MainViewModel
import app.olet.ui.components.EmptyState
import app.olet.ui.components.PillTabs
import app.olet.ui.components.SearchField
import app.olet.ui.components.TransactionRow
import app.olet.ui.navigation.Routes
import app.olet.ui.theme.ExpenseRed
import app.olet.ui.theme.IncomeGreen
import app.olet.ui.theme.MoneyStyle
import app.olet.ui.util.formatMoney
import app.olet.ui.util.formatMonth
import app.olet.ui.util.friendlyDay
import app.olet.ui.util.toLocalDate
import kotlinx.coroutines.launch
import java.time.YearMonth

@Composable
fun TransactionsScreen(vm: MainViewModel, profile: UserProfile, nav: NavHostController) {
    val list by vm.filteredTransactions.collectAsStateWithLifecycle()
    val month by vm.selectedMonth.collectAsStateWithLifecycle()
    val filter by vm.typeFilter.collectAsStateWithLifecycle()
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val summary by vm.selectedMonthSummary.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cur = profile.currency
    val searching = query.isNotBlank()

    val grouped = remember(list) { list.groupBy { it.tx.date.toLocalDate() } }

    fun delete(item: TransactionDetail) {
        vm.deleteTransaction(item.tx)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val res = snackbar.showSnackbar("\"${item.tx.title}\" deleted", "Undo", duration = SnackbarDuration.Short)
            if (res == SnackbarResult.ActionPerformed) vm.saveTransaction(item.tx)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column(Modifier.statusBarsPadding().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Activity", style = MaterialTheme.typography.headlineMedium)
                    SearchField(query, { vm.searchQuery.value = it }, "Search title, category, note…")
                }
            }
            item {
                Column(Modifier.animateContentSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!searching) MonthCard(month, summary, cur, onPrev = { vm.selectedMonth.value = month.minusMonths(1) }, onNext = {
                        vm.selectedMonth.value = month.plusMonths(1)
                    })
                    PillTabs(
                        listOf("All", "Expenses", "Income"),
                        selected = when (filter) { null -> 0; TxType.EXPENSE -> 1; TxType.INCOME -> 2 },
                        onSelect = { vm.typeFilter.value = listOf(null, TxType.EXPENSE, TxType.INCOME)[it] },
                        activeColors = listOf(MaterialTheme.colorScheme.primary, ExpenseRed, IncomeGreen),
                    )
                }
            }
            if (list.isEmpty()) {
                item {
                    if (searching) EmptyState(Icons.Rounded.SearchOff, "No results", "Nothing matches \"$query\".")
                    else EmptyState(
                        Icons.AutoMirrored.Rounded.ReceiptLong,
                        "Nothing here",
                        "No transactions in ${month.atDay(1).formatMonth()}. Tap + to add one.",
                    )
                }
            }
            grouped.forEach { (day, items) ->
                item(key = "h$day") {
                    val net = items.sumOf { if (it.tx.type == TxType.INCOME) it.tx.amount else -it.tx.amount }
                    Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(day.friendlyDay(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text(
                            formatMoney(net, cur, signed = true),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(items, key = { it.tx.id }) { item ->
                    SwipeToDelete(onDelete = { delete(item) }) {
                        TransactionRow(item, cur, onClick = { nav.navigate(Routes.tx(item.tx.type, item.tx.id)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthCard(month: YearMonth, summary: MainViewModel.MonthSummary, cur: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundIcon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Previous month", true, onPrev)
                Text(
                    month.atDay(1).formatMonth(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                RoundIcon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Next month", month < YearMonth.now(), onNext)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStat("Income", formatMoney(summary.income, cur), IncomeGreen, Modifier.weight(1f))
                MiniStat("Expense", formatMoney(summary.expense, cur), ExpenseRed, Modifier.weight(1f))
                MiniStat("Net", formatMoney(summary.net, cur), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RoundIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, cd: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.size(36.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, cd, tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(color.copy(alpha = 0.08f)).padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall.merge(MoneyStyle), color = color, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDelete(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        },
        positionalThreshold = { it * 0.35f },
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                if (state.targetValue == SwipeToDismissBoxValue.EndToStart) ExpenseRed else ExpenseRed.copy(alpha = 0.5f),
                label = "swipe",
            )
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(color).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) { Icon(Icons.Rounded.Delete, "Delete", tint = Color.White) }
        },
    ) { content() }
}
