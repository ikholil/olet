package app.olet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.olet.data.TransactionEntity
import app.olet.data.TxType
import app.olet.data.UserProfile
import app.olet.ui.MainViewModel
import app.olet.ui.components.AppCard
import app.olet.ui.components.AppTextField
import app.olet.ui.components.BackTopBar
import app.olet.ui.components.ChipSelector
import app.olet.ui.components.ConfirmDialog
import app.olet.ui.components.DateField
import app.olet.ui.components.sanitizeAmount
import app.olet.ui.theme.ExpenseRed
import app.olet.ui.theme.IncomeGreen
import app.olet.ui.theme.MoneyStyle
import app.olet.ui.util.amountInput
import app.olet.ui.util.color
import app.olet.ui.util.colorOf
import app.olet.ui.util.icon
import app.olet.ui.util.iconFor
import app.olet.ui.util.parseAmount
import app.olet.ui.util.toLocalDate
import app.olet.ui.util.toMillis
import kotlinx.coroutines.launch

@Composable
fun AddTransactionScreen(
    vm: MainViewModel,
    profile: UserProfile,
    initialType: TxType,
    editId: Long,
    onDone: () -> Unit,
) {
    val categories by vm.categories.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var type by rememberSaveable { mutableStateOf(initialType) }
    var amount by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var accountId by rememberSaveable { mutableStateOf<Long?>(null) }
    var date by rememberSaveable { mutableLongStateOf(java.time.LocalDate.now().toMillis()) }
    var loaded by rememberSaveable { mutableStateOf(editId == 0L) }
    var editing by remember { mutableStateOf<TransactionEntity?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(editId) {
        if (editId != 0L) {
            val tx = vm.getTransaction(editId)
            editing = tx
            if (tx != null && !loaded) {
                type = tx.type; amount = amountInput(tx.amount); title = tx.title; note = tx.note
                categoryId = tx.categoryId; accountId = tx.accountId; date = tx.date
                loaded = true
            }
        }
    }

    val typeCats = categories.filter { it.type == type }
    // Keep a valid default category / account selected.
    LaunchedEffect(type, typeCats, loaded) {
        if (loaded && typeCats.isNotEmpty() && typeCats.none { it.id == categoryId }) categoryId = typeCats.first().id
    }
    LaunchedEffect(accounts, loaded) {
        if (loaded && accountId == null && accounts.isNotEmpty() && editId == 0L) accountId = accounts.first().account.id
    }

    val accent = if (type == TxType.EXPENSE) ExpenseRed else IncomeGreen

    fun save() {
        val value = parseAmount(amount)
        if (value == null || value <= 0.0) {
            scope.launch { snackbar.showSnackbar("Enter a valid amount") }
            return
        }
        val cat = typeCats.firstOrNull { it.id == categoryId }
        val finalTitle = title.trim().ifEmpty { cat?.name ?: if (type == TxType.EXPENSE) "Expense" else "Income" }
        vm.saveTransaction(
            TransactionEntity(
                id = editId,
                type = type,
                title = finalTitle,
                amount = value,
                categoryId = cat?.id,
                accountId = accountId,
                date = date,
                note = note.trim(),
            )
        )
        onDone()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            BackTopBar(
                title = if (editId != 0L) "Edit transaction" else "New transaction",
                onBack = onDone,
                actions = {
                    if (editId != 0L && editing != null) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = ::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = accent),
            ) {
                Text(
                    if (editId != 0L) "Save changes" else "Save ${if (type == TxType.EXPENSE) "expense" else "income"}",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        },
    ) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(TxType.EXPENSE to "Expense", TxType.INCOME to "Income").forEachIndexed { i, (t, label) ->
                    SegmentedButton(
                        selected = type == t,
                        onClick = { type = t },
                        shape = SegmentedButtonDefaults.itemShape(i, 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = (if (t == TxType.EXPENSE) ExpenseRed else IncomeGreen).copy(alpha = 0.15f),
                            activeContentColor = if (t == TxType.EXPENSE) ExpenseRed else IncomeGreen,
                        ),
                    ) { Text(label) }
                }
            }

            AppCard(Modifier.fillMaxWidth()) {
                Text(
                    "Amount",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(profile.currency, style = MaterialTheme.typography.headlineLarge.merge(MoneyStyle), color = accent)
                    BasicTextField(
                        value = amount,
                        onValueChange = { amount = sanitizeAmount(it) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.displaySmall.merge(MoneyStyle).copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Start,
                        ),
                        cursorBrush = SolidColor(accent),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.padding(start = 4.dp).width(IntrinsicWidthFor(amount)),
                        decorationBox = { inner ->
                            if (amount.isEmpty()) {
                                Text(
                                    "0",
                                    style = MaterialTheme.typography.displaySmall.merge(MoneyStyle),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                )
                            }
                            inner()
                        },
                    )
                }
            }

            AppTextField(title, { title = it.take(60) }, "Title", leadingIcon = Icons.Rounded.Edit)

            Label("Category")
            ChipSelector(
                items = typeCats,
                selected = typeCats.firstOrNull { it.id == categoryId },
                label = { it.name },
                onSelect = { categoryId = it.id },
                icon = { iconFor(it.icon) },
                tint = { colorOf(it.color) },
            )

            if (accounts.isNotEmpty()) {
                Label("Account")
                ChipSelector(
                    items = accounts.map { it.account },
                    selected = accounts.map { it.account }.firstOrNull { it.id == accountId },
                    label = { it.name },
                    onSelect = { accountId = if (accountId == it.id) null else it.id },
                    icon = { it.type.icon() },
                    tint = { it.type.color() },
                )
            }

            DateField("Date", date, { if (it != null) date = it })
            AppTextField(note, { note = it.take(200) }, "Note (optional)", leadingIcon = Icons.AutoMirrored.Rounded.Notes, singleLine = false)
            Spacer(Modifier.height(8.dp))
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "Delete transaction?",
            message = "This can't be undone.",
            onConfirm = {
                editing?.let { vm.deleteTransaction(it) }
                onDone()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 0.dp),
    )
}

/** Width that grows with the typed amount, so the number stays visually centered. */
private fun IntrinsicWidthFor(text: String) = (maxOf(1, text.length) * 26 + 12).coerceAtMost(300).dp
