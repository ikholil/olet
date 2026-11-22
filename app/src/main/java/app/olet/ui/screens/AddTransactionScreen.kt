package app.olet.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.olet.data.CategoryEntity
import app.olet.data.TransactionEntity
import app.olet.data.TxType
import app.olet.data.UserProfile
import app.olet.ui.MainViewModel
import app.olet.ui.components.AppTextField
import app.olet.ui.components.ChipSelector
import app.olet.ui.components.ConfirmDialog
import app.olet.ui.components.PillTabs
import app.olet.ui.components.Suggestion
import app.olet.ui.components.SuggestionRow
import app.olet.ui.components.sanitizeAmount
import app.olet.ui.theme.ExpenseRed
import app.olet.ui.theme.IncomeGreen
import app.olet.ui.theme.MoneyStyle
import app.olet.ui.util.amountInput
import app.olet.ui.util.color
import app.olet.ui.util.colorOf
import app.olet.ui.util.formatDate
import app.olet.ui.util.formatMoney
import app.olet.ui.util.icon
import app.olet.ui.util.iconFor
import app.olet.ui.util.parseAmount
import app.olet.ui.util.toLocalDate
import app.olet.ui.util.toMillis
import app.olet.ui.util.toUtcMillis
import app.olet.ui.util.utcToLocalDate
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AddTransactionScreen(
    vm: MainViewModel,
    profile: UserProfile,
    initialType: TxType,
    editId: Long,
    onDone: () -> Unit,
    onManageCategories: () -> Unit = {},
) {
    val categories by vm.categories.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val history by vm.transactions.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var type by rememberSaveable { mutableStateOf(initialType) }
    var amount by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var categoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var accountId by rememberSaveable { mutableStateOf<Long?>(null) }
    var date by rememberSaveable { mutableLongStateOf(LocalDate.now().toMillis()) }
    var loaded by rememberSaveable { mutableStateOf(editId == 0L) }
    var userPickedCategory by rememberSaveable { mutableStateOf(editId != 0L) }
    var titleFocused by remember { mutableStateOf(false) }
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

    // Categories ordered by how often they're used.
    val usage = remember(history, type) { vm.categoryUsage(history, type) }
    val typeCats = remember(categories, type, usage) {
        categories.filter { it.type == type }.sortedByDescending { usage[it.id] ?: 0 }
    }
    LaunchedEffect(type, typeCats, loaded) {
        if (loaded && typeCats.isNotEmpty() && typeCats.none { it.id == categoryId }) categoryId = typeCats.first().id
    }
    LaunchedEffect(accounts, loaded) {
        if (loaded && accountId == null && accounts.isNotEmpty() && editId == 0L) accountId = accounts.first().account.id
    }

    val suggestions = remember(history, type, title) { vm.titleSuggestions(history, type, title) }
    val amountHints = remember(history, type, title) { vm.amountsFor(history, type, title) }
    val accent by animateColorAsState(if (type == TxType.EXPENSE) ExpenseRed else IncomeGreen, label = "accent")

    fun applySuggestion(s: MainViewModel.TxSuggestion) {
        title = s.title
        if (s.categoryId != null && typeCats.any { it.id == s.categoryId }) { categoryId = s.categoryId; userPickedCategory = true }
        if (s.accountId != null && accounts.any { it.account.id == s.accountId }) accountId = s.accountId
        if (parseAmount(amount) == null || amount.isBlank()) amount = amountInput(s.amount)
    }

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
                id = editId, type = type, title = finalTitle, amount = value,
                categoryId = cat?.id, accountId = accountId, date = date, note = note.trim(),
            )
        )
        onDone()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        topBar = {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onDone) { Icon(Icons.Rounded.Close, "Back") }
                Text(
                    if (editId != 0L) "Edit transaction" else "New transaction",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                if (editId != 0L && editing != null) {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Rounded.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        bottomBar = {
            Button(
                onClick = ::save,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White),
            ) {
                Text(
                    if (editId != 0L) "Save changes" else "Save ${if (type == TxType.EXPENSE) "expense" else "income"}",
                    style = MaterialTheme.typography.titleMedium,
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            PillTabs(
                listOf("Expense", "Income"),
                selected = if (type == TxType.EXPENSE) 0 else 1,
                onSelect = { type = if (it == 0) TxType.EXPENSE else TxType.INCOME; userPickedCategory = false },
                activeColors = listOf(ExpenseRed, IncomeGreen),
                height = 48.dp,
            )

            // Amount hero
            Surface(shape = RoundedCornerShape(28.dp), color = accent.copy(alpha = 0.08f)) {
                Column(Modifier.fillMaxWidth().padding(vertical = 22.dp, horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Amount", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(profile.currency, style = MaterialTheme.typography.headlineLarge.merge(MoneyStyle), color = accent)
                        Spacer(Modifier.width(4.dp))
                        BasicTextField(
                            value = amount,
                            onValueChange = { amount = sanitizeAmount(it) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.displayMedium.merge(MoneyStyle).copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(accent),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.width(amountWidth(amount)),
                            decorationBox = { inner ->
                                if (amount.isEmpty()) {
                                    Text("0", style = MaterialTheme.typography.displayMedium.merge(MoneyStyle), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                                }
                                inner()
                            },
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    // Remembered amounts for this title first, then quick increments.
                    val chips = amountHints.map { "=" + amountInput(it) } + listOf("+50", "+100", "+500", "+1000")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(chips.distinct()) { chip ->
                            val isSet = chip.startsWith("=")
                            Surface(
                                onClick = {
                                    val cur = parseAmount(amount) ?: 0.0
                                    val v = chip.drop(1).toDouble()
                                    amount = amountInput(if (isSet) v else cur + v)
                                },
                                shape = CircleShape,
                                color = if (isSet) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                            ) {
                                Text(
                                    if (isSet) formatMoney(chip.drop(1).toDouble(), profile.currency) else chip,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSet) accent else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(
                    title, { title = it.take(60) }, "What was it for?",
                    leadingIcon = Icons.Rounded.Edit,
                    modifier = Modifier.onFocusChanged { titleFocused = it.isFocused },
                )
                SuggestionRow(
                    items = if (titleFocused || title.isNotEmpty()) suggestions.map { s ->
                        Suggestion(
                            key = s.title,
                            title = s.title,
                            subtitle = listOfNotNull(formatMoney(s.amount, profile.currency), s.categoryName).joinToString(" · "),
                            icon = iconFor(s.categoryIcon),
                            tint = colorOf(s.categoryColor),
                        )
                    } else emptyList(),
                    header = if (title.isBlank()) "Recent" else "Suggestions",
                    onPick = { picked -> suggestions.firstOrNull { it.title == picked.key }?.let(::applySuggestion) },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Label("Category", Modifier.weight(1f))
                    TextButton(onClick = onManageCategories) {
                        Icon(Icons.Rounded.Tune, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Manage")
                    }
                }
                CategoryGrid(typeCats, categoryId) { categoryId = it.id; userPickedCategory = true }
            }

            if (accounts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Label(if (type == TxType.EXPENSE) "Paid from" else "Received in")
                    ChipSelector(
                        items = accounts.map { it.account },
                        selected = accounts.map { it.account }.firstOrNull { it.id == accountId },
                        label = { it.name },
                        onSelect = { accountId = if (accountId == it.id) null else it.id },
                        icon = { it.type.icon() },
                        tint = { it.type.color() },
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Label("Date")
                DateChips(date) { date = it }
            }

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
private fun Label(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleSmall, modifier = modifier.padding(start = 4.dp))
}

@Composable
private fun CategoryGrid(cats: List<CategoryEntity>, selectedId: Long?, onPick: (CategoryEntity) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cats.chunked(4).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { c ->
                    val sel = c.id == selectedId
                    val tint = colorOf(c.color)
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (sel) tint.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(if (sel) 1.5.dp else 0.dp, if (sel) tint else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable { onPick(c) }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.size(40.dp).clip(CircleShape).background(if (sel) tint else tint.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(iconFor(c.icon), null, tint = if (sel) Color.White else tint, modifier = Modifier.size(20.dp)) }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            c.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (sel) tint else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateChips(millis: Long, onChange: (Long) -> Unit) {
    val today = LocalDate.now()
    val day = millis.toLocalDate()
    var picker by remember { mutableStateOf(false) }
    val custom = day != today && day != today.minusDays(1)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DateChip("Today", day == today) { onChange(today.toMillis()) }
        DateChip("Yesterday", day == today.minusDays(1)) { onChange(today.minusDays(1).toMillis()) }
        DateChip(if (custom) millis.formatDate() else "Pick date", custom, icon = true) { picker = true }
    }
    if (picker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = day.toUtcMillis())
        DatePickerDialog(
            onDismissRequest = { picker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onChange(it.utcToLocalDate().toMillis()) }
                    picker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { picker = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun DateChip(label: String, selected: Boolean, icon: Boolean = false, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) primary else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon) {
                Icon(Icons.Rounded.CalendarMonth, null, Modifier.size(16.dp), tint = if (selected) primary else MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
            }
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) primary else MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** Width that grows with the typed amount so the number stays centered. */
private fun amountWidth(text: String) = (maxOf(1, text.length) * 30 + 8).coerceAtMost(280).dp
