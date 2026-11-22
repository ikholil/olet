package app.olet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.olet.data.CategoryEntity
import app.olet.data.TxType
import app.olet.ui.MainViewModel
import app.olet.ui.components.AppCard
import app.olet.ui.components.AppTextField
import app.olet.ui.components.BackTopBar
import app.olet.ui.components.ConfirmDialog
import app.olet.ui.components.IconBadge
import app.olet.ui.util.categoryIcons
import app.olet.ui.util.colorOf
import app.olet.ui.util.iconFor
import app.olet.ui.util.paletteColors

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(vm: MainViewModel, onBack: () -> Unit) {
    val all by vm.categories.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(0) }
    val type = if (tab == 0) TxType.EXPENSE else TxType.INCOME
    val list = all.filter { it.type == type }
    var editor by remember { mutableStateOf<CategoryEntity?>(null) }
    var toDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackTopBar("Categories", onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editor = CategoryEntity(name = "", type = type, icon = "other", color = paletteColors.first()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
            ) { Icon(Icons.Rounded.Add, "Add category") }
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            PrimaryTabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.background) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, selectedContentColor = MaterialTheme.colorScheme.primary, unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant, text = { Text("Expense") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, selectedContentColor = MaterialTheme.colorScheme.primary, unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant, text = { Text("Income") })
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(list, key = { it.id }) { c ->
                    AppCard(Modifier.fillMaxWidth(), onClick = { editor = c }, contentPadding = PaddingValues(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconBadge(iconFor(c.icon), colorOf(c.color))
                            Spacer(Modifier.width(12.dp))
                            Text(c.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            IconButton(onClick = { toDelete = c }, enabled = list.size > 1) {
                                Icon(Icons.Rounded.DeleteOutline, "Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    editor?.let { cat ->
        var name by rememberSaveable { mutableStateOf(cat.name) }
        var icon by rememberSaveable { mutableStateOf(cat.icon) }
        var color by rememberSaveable { mutableStateOf(cat.color) }
        FormSheet(
            title = if (cat.id == 0L) "New category" else "Edit category",
            saveEnabled = name.isNotBlank(),
            onDismiss = { editor = null },
            onSave = { vm.saveCategory(cat.copy(name = name.trim(), icon = icon, color = color)); editor = null },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(iconFor(icon), colorOf(color), size = 56.dp)
                Spacer(Modifier.width(12.dp))
                AppTextField(name, { name = it.take(30) }, "Name")
            }
            Text("Icon", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categoryIcons.forEach { (key, vec) ->
                    val sel = key == icon
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (sel) colorOf(color).copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(if (sel) 1.5.dp else 0.dp, if (sel) colorOf(color) else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable { icon = key },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(vec, key, tint = if (sel) colorOf(color) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text("Color", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                paletteColors.forEach { c ->
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(colorOf(c)).clickable { color = c },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (c == color) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
    toDelete?.let { c ->
        ConfirmDialog(
            "Delete category?",
            "\"${c.name}\" will be removed. Existing transactions will show as Uncategorized.",
            onConfirm = { vm.deleteCategory(c) },
            onDismiss = { toDelete = null },
        )
    }
}
