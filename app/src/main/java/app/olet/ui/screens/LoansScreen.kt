package app.olet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.olet.data.LoanEntity
import app.olet.data.LoanType
import app.olet.data.UserProfile
import app.olet.ui.MainViewModel
import app.olet.ui.components.AppCard
import app.olet.ui.components.AppTextField
import app.olet.ui.components.Avatar
import app.olet.ui.components.ConfirmDialog
import app.olet.ui.components.DateField
import app.olet.ui.components.EmptyState
import app.olet.ui.components.sanitizeAmount
import app.olet.ui.theme.ExpenseRed
import app.olet.ui.theme.IncomeGreen
import app.olet.ui.theme.MoneyStyle
import app.olet.ui.util.amountInput
import app.olet.ui.util.formatDate
import app.olet.ui.util.formatMoney
import app.olet.ui.util.initialsOf
import app.olet.ui.util.parseAmount
import app.olet.ui.util.toLocalDate
import app.olet.ui.util.toMillis
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(vm: MainViewModel, profile: UserProfile) {
    val loans by vm.loans.collectAsStateWithLifecycle()
    val summary by vm.loanSummary.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(0) }
    var editor by remember { mutableStateOf<LoanEntity?>(null) }
    var payFor by remember { mutableStateOf<LoanEntity?>(null) }
    var toDelete by remember { mutableStateOf<LoanEntity?>(null) }
    val cur = profile.currency
    val type = if (tab == 0) LoanType.LENT else LoanType.BORROWED
    val list = loans.filter { it.type == type }
    val accent = if (type == LoanType.LENT) IncomeGreen else ExpenseRed

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text("Loans") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editor = LoanEntity(person = "", type = type, amount = 0.0, date = LocalDate.now().toMillis()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
            ) { Icon(Icons.Rounded.Add, "Add loan") }
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            PrimaryTabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, selectedContentColor = MaterialTheme.colorScheme.primary, unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant, text = { Text("I'll receive") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, selectedContentColor = MaterialTheme.colorScheme.primary, unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant, text = { Text("I owe") })
            }
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        Text(
                            if (type == LoanType.LENT) "Total others owe you" else "Total you owe others",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            formatMoney(if (type == LoanType.LENT) summary.toReceive else summary.toPay, cur),
                            style = MaterialTheme.typography.headlineMedium.merge(MoneyStyle),
                            color = accent,
                        )
                        Text(
                            "${list.count { !it.settled }} open • ${list.count { it.settled }} settled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (list.isEmpty()) {
                    item {
                        EmptyState(
                            Icons.Rounded.Handshake,
                            if (type == LoanType.LENT) "Nobody owes you" else "You owe nobody",
                            if (type == LoanType.LENT) "Record money you lent to someone." else "Record money you borrowed from someone.",
                        )
                    }
                }
                items(list, key = { it.id }) { loan ->
                    LoanCard(
                        loan, cur,
                        onPay = { payFor = loan },
                        onEdit = { editor = loan },
                        onSettle = { vm.saveLoan(loan.copy(paid = loan.amount)) },
                        onDelete = { toDelete = loan },
                    )
                }
            }
        }
    }

    editor?.let { LoanEditor(it, cur, onDismiss = { editor = null }, onSave = { l -> vm.saveLoan(l); editor = null }) }
    payFor?.let { loan ->
        PaymentDialog(loan, cur, onDismiss = { payFor = null }, onPay = { vm.addLoanPayment(loan, it); payFor = null })
    }
    toDelete?.let { loan ->
        ConfirmDialog("Delete loan?", "Remove the loan record with ${loan.person}?", onConfirm = { vm.deleteLoan(loan) }, onDismiss = { toDelete = null })
    }
}

@Composable
private fun LoanCard(
    loan: LoanEntity,
    cur: String,
    onPay: () -> Unit,
    onEdit: () -> Unit,
    onSettle: () -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    val accent = if (loan.type == LoanType.LENT) IncomeGreen else ExpenseRed
    val overdue = !loan.settled && loan.dueDate != null && loan.dueDate.toLocalDate().isBefore(LocalDate.now())
    AppCard(Modifier.fillMaxWidth(), onClick = onEdit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(initialsOf(loan.person))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(loan.person, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val sub = buildString {
                    append(loan.date.formatDate())
                    if (loan.phone.isNotBlank()) append(" • ${loan.phone}")
                }
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, "Options") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { menu = false; onEdit() })
                    if (!loan.settled) DropdownMenuItem(text = { Text("Mark as settled") }, onClick = { menu = false; onSettle() })
                    DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, onClick = { menu = false; onDelete() })
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text("Remaining", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatMoney(loan.remaining, cur), style = MaterialTheme.typography.titleLarge.merge(MoneyStyle), color = if (loan.settled) MaterialTheme.colorScheme.onSurfaceVariant else accent)
            }
            Text(
                "of ${formatMoney(loan.amount, cur)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { if (loan.amount > 0) (loan.paid / loan.amount).toFloat().coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = accent,
            trackColor = accent.copy(alpha = 0.15f),
            drawStopIndicator = {},
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (loan.settled) {
                Icon(Icons.Rounded.CheckCircle, null, tint = IncomeGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Settled", style = MaterialTheme.typography.labelLarge, color = IncomeGreen, modifier = Modifier.weight(1f))
            } else {
                Text(
                    loan.dueDate?.let { (if (overdue) "Overdue • " else "Due ") + it.formatDate() } ?: "No due date",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (overdue) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onPay) {
                    Icon(Icons.Rounded.Payments, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (loan.type == LoanType.LENT) "Received" else "Paid back")
                }
            }
        }
        if (loan.note.isNotBlank()) {
            Text(loan.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LoanEditor(initial: LoanEntity, cur: String, onDismiss: () -> Unit, onSave: (LoanEntity) -> Unit) {
    var person by rememberSaveable { mutableStateOf(initial.person) }
    var phone by rememberSaveable { mutableStateOf(initial.phone) }
    var amount by rememberSaveable { mutableStateOf(amountInput(initial.amount)) }
    var type by rememberSaveable { mutableStateOf(initial.type) }
    var date by rememberSaveable { mutableStateOf(initial.date) }
    var due by rememberSaveable { mutableStateOf(initial.dueDate) }
    var note by rememberSaveable { mutableStateOf(initial.note) }
    val value = parseAmount(amount)
    val valid = person.isNotBlank() && value != null && value > 0

    FormSheet(
        title = if (initial.id == 0L) "New loan" else "Edit loan",
        saveEnabled = valid,
        onDismiss = onDismiss,
        onSave = {
            onSave(
                initial.copy(
                    person = person.trim(), phone = phone.trim(), amount = value!!, type = type,
                    date = date, dueDate = due, note = note.trim(),
                    paid = initial.paid.coerceAtMost(value),
                )
            )
        },
    ) {
        app.olet.ui.components.ChipSelector(
            items = listOf(LoanType.LENT, LoanType.BORROWED),
            selected = type,
            label = { if (it == LoanType.LENT) "I lent (they owe me)" else "I borrowed (I owe)" },
            onSelect = { type = it },
            tint = { if (it == LoanType.LENT) IncomeGreen else ExpenseRed },
        )
        AppTextField(person, { person = it.take(50) }, "Person name", leadingIcon = Icons.Rounded.Person)
        AppTextField(phone, { phone = it.take(20) }, "Phone (optional)", leadingIcon = Icons.Rounded.Phone, keyboardType = KeyboardType.Phone)
        AppTextField(amount, { amount = sanitizeAmount(it) }, "Amount", keyboardType = KeyboardType.Decimal, prefix = cur)
        DateField("Date", date, { if (it != null) date = it })
        DateField("Due date (optional)", due, { due = it }, clearable = true)
        AppTextField(note, { note = it.take(200) }, "Note (optional)", singleLine = false)
    }
}

@Composable
internal fun PaymentDialog(loan: LoanEntity, cur: String, onDismiss: () -> Unit, onPay: (Double) -> Unit) {
    var amount by rememberSaveable { mutableStateOf(amountInput(loan.remaining)) }
    val value = parseAmount(amount)
    FormSheet(
        title = if (loan.type == LoanType.LENT) "Payment received from ${loan.person}" else "Payment to ${loan.person}",
        saveEnabled = value != null && value > 0,
        onDismiss = onDismiss,
        onSave = { onPay(value!!) },
    ) {
        Text(
            "Remaining: ${formatMoney(loan.remaining, cur)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppTextField(amount, { amount = sanitizeAmount(it) }, "Amount", keyboardType = KeyboardType.Decimal, prefix = cur)
    }
}
