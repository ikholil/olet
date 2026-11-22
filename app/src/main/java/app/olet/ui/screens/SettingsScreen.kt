package app.olet.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.olet.BuildConfig
import app.olet.data.ThemeMode
import app.olet.data.UserProfile
import app.olet.ui.MainViewModel
import app.olet.ui.components.AppCard
import app.olet.ui.components.AppTextField
import app.olet.ui.components.Avatar
import app.olet.ui.components.ConfirmDialog
import app.olet.ui.components.IconBadge
import app.olet.ui.navigation.Routes
import app.olet.ui.theme.Amber
import app.olet.ui.theme.ExpenseRed
import app.olet.ui.theme.IncomeGreen
import app.olet.ui.util.initialsOf

private val currencies = listOf(
    "৳" to "Bangladeshi Taka",
    "$" to "US Dollar",
    "€" to "Euro",
    "£" to "British Pound",
    "₹" to "Indian Rupee",
    "¥" to "Yen",
    "SAR " to "Saudi Riyal",
    "AED " to "UAE Dirham",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: MainViewModel, profile: UserProfile, nav: NavHostController) {
    val context = LocalContext.current
    var dialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text("More") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(initialsOf(profile.name.ifBlank { "Friend" }), size = 60.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(profile.name.ifBlank { "Friend" }, style = MaterialTheme.typography.titleLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (profile.isGoogle) {
                                Icon(Icons.Rounded.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(14.dp))
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(
                                if (profile.isGoogle) profile.email else "Local profile • data on this device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(onClick = { dialog = "name" }) { Icon(Icons.Rounded.Edit, "Edit name") }
                }
            }

            Group {
                Item(Icons.Rounded.AccountBalanceWallet, IncomeGreen, "Accounts", "Cash, bank, mobile banking") { nav.navigate(Routes.ACCOUNTS) }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Item(Icons.Rounded.Category, Amber, "Categories", "Manage income & expense categories") { nav.navigate(Routes.CATEGORIES) }
            }
            Group {
                Item(Icons.Rounded.Payments, MaterialTheme.colorScheme.primary, "Currency", currencies.firstOrNull { it.first == profile.currency }?.second ?: profile.currency) { dialog = "currency" }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Item(Icons.Rounded.DarkMode, Color(0xFF3D8BFF), "Theme", profile.theme.label()) { dialog = "theme" }
            }
            Group {
                Item(Icons.Rounded.Info, Color(0xFF7C8598), "About Olet", "Version ${BuildConfig.VERSION_NAME}", showArrow = false) {}
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Item(Icons.AutoMirrored.Rounded.Logout, ExpenseRed, "Sign out", "Your data stays on this device", showArrow = false) { dialog = "signout" }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    when (dialog) {
        "name" -> {
            var name by remember { mutableStateOf(profile.name) }
            AlertDialog(
                onDismissRequest = { dialog = null },
                title = { Text("Your name") },
                text = { AppTextField(name, { name = it.take(40) }, "Name") },
                confirmButton = { TextButton(onClick = { vm.updateName(name); dialog = null }) { Text("Save") } },
                dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel") } },
            )
        }
        "currency" -> ChoiceDialog(
            "Currency",
            currencies.map { "${it.first.trim()}  ${it.second}" },
            currencies.indexOfFirst { it.first == profile.currency },
            onPick = { vm.setCurrency(currencies[it].first); dialog = null },
            onDismiss = { dialog = null },
        )
        "theme" -> ChoiceDialog(
            "Theme",
            ThemeMode.entries.map { it.label() },
            profile.theme.ordinal,
            onPick = { vm.setTheme(ThemeMode.entries[it]); dialog = null },
            onDismiss = { dialog = null },
        )
        "signout" -> ConfirmDialog(
            "Sign out?",
            "You'll return to the welcome screen. Your transactions and records stay on this device.",
            confirmText = "Sign out",
            onConfirm = { vm.signOut(context) },
            onDismiss = { dialog = null },
        )
    }
}

private fun ThemeMode.label() = when (this) {
    ThemeMode.SYSTEM -> "System default"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

@Composable
private fun Group(content: @Composable () -> Unit) {
    AppCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 4.dp)) { content() }
}

@Composable
private fun Item(icon: ImageVector, tint: Color, title: String, subtitle: String, showArrow: Boolean = true, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, tint, size = 40.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (showArrow) Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChoiceDialog(title: String, options: List<String>, selected: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEachIndexed { i, label ->
                    Surface(onClick = { onPick(i) }, color = Color.Transparent) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = i == selected, onClick = { onPick(i) })
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}
