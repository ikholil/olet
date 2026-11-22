package app.olet.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import app.olet.ui.components.AppCard
import app.olet.ui.components.EmptyState
import app.olet.ui.components.TransactionRow
import app.olet.ui.navigation.Routes
import app.olet.ui.theme.ExpenseRed
import app.olet.ui.theme.IncomeGreen
import app.olet.ui.util.formatMoney
import app.olet.ui.util.formatMonth
import app.olet.ui.util.friendlyDay
import app.olet.ui.util.toLocalDate
import kotlinx.coroutines.launch
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(vm: MainViewModel, profile: UserProfile, nav: NavHostController) {
    val list by vm.filteredTransactions.collectAsStateWithLifecycle()
    val month by vm.selectedMonth.collectAsStateWithLifecycle()
    val filter by vm.typeFilter.collectAsStateWithLifecycle()
    val summary by vm.selectedMonthSummary.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cur = profile.currency

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
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Activity") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { nav.navigate(Routes.tx(filter ?: TxType.EXPENSE)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
            ) { Icon(Icons.Rounded.Add, "Add transaction") }
        },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { vm.selectedMonth.value = month.minusMonths(1) }) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Previous month")
                        }
                        Text(
                            month.atDay(1).formatMonth(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                        IconButton(
                            onClick = { vm.selectedMonth.value = month.plusMonths(1) },
                            enabled = month < YearMonth.now(),
                        ) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Next month") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Stat("Income", formatMoney(summary.income, cur), IncomeGreen, Modifier.weight(1f))
                        Stat("Expense", formatMoney(summary.expense, cur), ExpenseRed, Modifier.weight(1f))
                        Stat(
                            "Net",
                            formatMoney(summary.net, cur),
                            MaterialTheme.colorScheme.onSurface,
                            Modifier.weight(1f),
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(null to "All", TxType.EXPENSE to "Expenses", TxType.INCOME to "Income").forEach { (t, label) ->
                        FilterChip(
                            selected = filter == t,
                            onClick = { vm.typeFilter.value = t },
                            label = { Text(label) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }
            if (list.isEmpty()) {
                item {
                    EmptyState(
                        Icons.AutoMirrored.Rounded.ReceiptLong,
                        "Nothing here",
                        "No transactions in ${month.atDay(1).formatMonth()}.",
                    )
                }
            }
            grouped.forEach { (day, items) ->
                item(key = "h$day") {
                    Text(
                        day.friendlyDay(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                    )
                }
                items(items, key = { it.tx.id }) { item ->
                    SwipeToDelete(onDelete = { delete(item) }) {
                        TransactionRow(item, cur, onClick = { nav.navigate(Routes.tx(item.tx.type, item.tx.id)) })
                    }
                }
            }
            if (list.isNotEmpty()) {
                item {
                    Text(
                        "Tip: swipe left on a transaction to delete it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
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
