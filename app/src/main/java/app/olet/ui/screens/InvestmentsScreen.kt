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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.ContactPhone
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import app.olet.ui.theme.LocalExtraColors
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
import app.olet.data.InvestmentEntity
import app.olet.data.UserProfile
import app.olet.ui.MainViewModel
import app.olet.ui.components.AppCard
import app.olet.ui.components.AppTextField
import app.olet.ui.components.ConfirmDialog
import app.olet.ui.components.DateField
import app.olet.ui.components.EmptyState
import app.olet.ui.components.IconBadge
import app.olet.ui.components.sanitizeAmount
import app.olet.ui.theme.MoneyStyle
import app.olet.ui.util.amountInput
import app.olet.ui.util.formatDate
import app.olet.ui.util.formatMoney
import app.olet.ui.util.parseAmount
import app.olet.ui.util.toMillis
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(vm: MainViewModel, profile: UserProfile) {
    val list by vm.investments.collectAsStateWithLifecycle()
    var editor by remember { mutableStateOf<InvestmentEntity?>(null) }
    var toDelete by remember { mutableStateOf<InvestmentEntity?>(null) }
    val cur = profile.currency
    val activeTotal = list.filter { it.active }.sumOf { it.amount }
    val allTotal = list.sumOf { it.amount }

    val pending by vm.pendingAdd.collectAsStateWithLifecycle()
    LaunchedEffect(pending) {
        if (pending == "investment") {
            vm.pendingAdd.value = null
            editor = InvestmentEntity(name = "", amount = 0.0, date = LocalDate.now().toMillis())
        }
    }
    val names = remember(list) { list.groupBy { it.name.trim().lowercase() }.values.map { it.first() } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            ScreenHeader("Investments", "Add investment") {
                editor = InvestmentEntity(name = "", amount = 0.0, date = LocalDate.now().toMillis())
            }
        },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(LocalExtraColors.current.heroGradient).padding(20.dp)
                ) {
                    Column {
                        Text("Currently invested", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
                        Text(formatMoney(activeTotal, cur), style = MaterialTheme.typography.headlineLarge.merge(MoneyStyle), color = Color.White)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "${list.count { it.active }} active • ${list.count { !it.active }} closed • ${formatMoney(allTotal, cur)} all time",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                }
            }
            if (list.isEmpty()) {
                item {
                    EmptyState(
                        Icons.AutoMirrored.Rounded.TrendingUp,
                        "No investments yet",
                        "Track money you've invested in a company, business or with a person.",
                    )
                }
            }
            items(list, key = { it.id }) { inv ->
                InvestmentCard(
                    inv, cur,
                    onEdit = { editor = inv },
                    onToggle = { vm.saveInvestment(inv.copy(active = !inv.active)) },
                    onDelete = { toDelete = inv },
                )
            }
        }
    }

    editor?.let { androidx.compose.runtime.key(System.identityHashCode(it)) { InvestmentEditor(it, cur, names, onDismiss = { editor = null }, onSave = { i -> vm.saveInvestment(i); editor = null }) } }
    toDelete?.let { inv ->
        ConfirmDialog("Delete investment?", "Remove \"${inv.name}\"?", onConfirm = { vm.deleteInvestment(inv) }, onDismiss = { toDelete = null })
    }
}

@Composable
private fun InvestmentCard(inv: InvestmentEntity, cur: String, onEdit: () -> Unit, onToggle: () -> Unit, onDelete: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val tint = if (inv.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    AppCard(Modifier.fillMaxWidth(), onClick = onEdit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Rounded.Business, tint)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(inv.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    inv.date.formatDate() + if (inv.active) " • Active" else " • Closed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(formatMoney(inv.amount, cur), style = MaterialTheme.typography.titleMedium.merge(MoneyStyle), color = tint)
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, "Options") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { menu = false; onEdit() })
                    DropdownMenuItem(text = { Text(if (inv.active) "Mark as closed" else "Mark as active") }, onClick = { menu = false; onToggle() })
                    DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, onClick = { menu = false; onDelete() })
                }
            }
        }
        if (inv.contact.isNotBlank() || inv.note.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            if (inv.contact.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.ContactPhone, null, Modifier.padding(end = 6.dp).height(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(inv.contact, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (inv.note.isNotBlank()) {
                Text(inv.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InvestmentEditor(initial: InvestmentEntity, cur: String, names: List<InvestmentEntity>, onDismiss: () -> Unit, onSave: (InvestmentEntity) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var contact by rememberSaveable { mutableStateOf(initial.contact) }
    var amount by rememberSaveable { mutableStateOf(amountInput(initial.amount)) }
    var date by rememberSaveable { mutableStateOf(initial.date) }
    var note by rememberSaveable { mutableStateOf(initial.note) }
    var active by rememberSaveable { mutableStateOf(initial.active) }
    val value = parseAmount(amount)

    FormSheet(
        title = if (initial.id == 0L) "New investment" else "Edit investment",
        saveEnabled = name.isNotBlank() && value != null && value > 0,
        onDismiss = onDismiss,
        onSave = {
            onSave(initial.copy(name = name.trim(), contact = contact.trim(), amount = value!!, date = date, note = note.trim(), active = active))
        },
    ) {
        AppTextField(name, { name = it.take(60) }, "Company or person", leadingIcon = Icons.Rounded.Business)
        val matches = names.filter {
            (name.isBlank() || it.name.contains(name.trim(), true)) && !it.name.equals(name.trim(), true)
        }.take(6)
        app.olet.ui.components.SuggestionRow(
            items = matches.map { app.olet.ui.components.Suggestion(it.name, it.name, it.contact.ifBlank { null }) },
            header = if (name.isBlank()) "Previous" else "Suggestions",
            onPick = { s -> names.firstOrNull { it.name == s.key }?.let { name = it.name; if (contact.isBlank()) contact = it.contact } },
        )
        AppTextField(contact, { contact = it.take(100) }, "Contact info (phone, email, address)", leadingIcon = Icons.Rounded.ContactPhone)
        AppTextField(amount, { amount = sanitizeAmount(it) }, "Amount invested", keyboardType = KeyboardType.Decimal, prefix = cur)
        DateField("Date", date, { if (it != null) date = it })
        AppTextField(note, { note = it.take(200) }, "Note (optional)", singleLine = false)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Active", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = active, onCheckedChange = { active = it })
        }
    }
}
