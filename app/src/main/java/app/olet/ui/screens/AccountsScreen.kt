package app.olet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import app.olet.data.AccountEntity
import app.olet.data.AccountType
import app.olet.data.UserProfile
import app.olet.ui.MainViewModel
import app.olet.ui.components.AppCard
import app.olet.ui.components.AppTextField
import app.olet.ui.components.BackTopBar
import app.olet.ui.components.ChipSelector
import app.olet.ui.components.ConfirmDialog
import app.olet.ui.components.EmptyState
import app.olet.ui.components.IconBadge
import app.olet.ui.theme.ExpenseRed
import app.olet.ui.theme.MoneyStyle
import app.olet.ui.util.color
import app.olet.ui.util.formatMoney
import app.olet.ui.util.icon

@Composable
fun AccountsScreen(vm: MainViewModel, profile: UserProfile, onBack: () -> Unit) {
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val total by vm.totalBalance.collectAsStateWithLifecycle()
    var editor by remember { mutableStateOf<AccountEntity?>(null) }
    var toDelete by remember { mutableStateOf<AccountEntity?>(null) }
    val cur = profile.currency

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar("Accounts", onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editor = AccountEntity(name = "", type = AccountType.BANK) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
            ) { Icon(Icons.Rounded.Add, "Add account") }
        },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Stat("Total across accounts", formatMoney(total, cur), MaterialTheme.colorScheme.primary)
                }
            }
            if (accounts.isEmpty()) {
                item { EmptyState(Icons.Rounded.AccountBalanceWallet, "No accounts", "Add cash, bank, mobile banking or card accounts.") }
            }
            items(accounts, key = { it.account.id }) { item ->
                AppCard(Modifier.fillMaxWidth(), onClick = { editor = item.account }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(item.account.type.icon(), item.account.type.color())
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.account.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(item.account.type.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            formatMoney(item.balance, cur),
                            style = MaterialTheme.typography.titleMedium.merge(MoneyStyle),
                            color = if (item.balance < 0) ExpenseRed else MaterialTheme.colorScheme.onSurface,
                        )
                        IconButton(onClick = { toDelete = item.account }) {
                            Icon(Icons.Rounded.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    editor?.let { acc ->
        var name by rememberSaveable { mutableStateOf(acc.name) }
        var type by rememberSaveable { mutableStateOf(acc.type) }
        var opening by rememberSaveable { mutableStateOf(app.olet.ui.util.amountInput(acc.openingBalance)) }
        FormSheet(
            title = if (acc.id == 0L) "New account" else "Edit account",
            saveEnabled = name.isNotBlank(),
            onDismiss = { editor = null },
            onSave = {
                vm.saveAccount(acc.copy(name = name.trim(), type = type, openingBalance = app.olet.ui.util.parseAmount(opening) ?: 0.0))
                editor = null
            },
        ) {
            AppTextField(name, { name = it.take(40) }, "Account name (e.g. DBBL, bKash)")
            ChipSelector(
                items = AccountType.entries.toList(),
                selected = type,
                label = { it.label },
                onSelect = { type = it },
                icon = { it.icon() },
                tint = { it.color() },
            )
            AppTextField(
                opening,
                { opening = app.olet.ui.components.sanitizeAmount(it) },
                "Opening balance",
                keyboardType = KeyboardType.Decimal,
                prefix = cur,
            )
        }
    }
    toDelete?.let { acc ->
        ConfirmDialog(
            "Delete account?",
            "\"${acc.name}\" will be removed. Its transactions are kept but won't count toward any account balance.",
            onConfirm = { vm.deleteAccount(acc) },
            onDismiss = { toDelete = null },
        )
    }
}
