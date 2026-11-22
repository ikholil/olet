package app.olet.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import app.olet.ui.components.FloatingNavBar
import app.olet.ui.components.NavItem
import app.olet.ui.screens.QuickAdd
import app.olet.ui.screens.QuickAddSheet
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.olet.data.TxType
import app.olet.data.UserProfile
import app.olet.ui.MainViewModel
import app.olet.ui.screens.AccountsScreen
import app.olet.ui.screens.AddTransactionScreen
import app.olet.ui.screens.CategoriesScreen
import app.olet.ui.screens.HomeScreen
import app.olet.ui.screens.InvestmentsScreen
import app.olet.ui.screens.LoansScreen
import app.olet.ui.screens.LoginScreen
import app.olet.ui.screens.SettingsScreen
import app.olet.ui.screens.TransactionsScreen

object Routes {
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val LOANS = "loans"
    const val INVESTMENTS = "investments"
    const val SETTINGS = "settings"
    const val ACCOUNTS = "accounts"
    const val CATEGORIES = "categories"
    const val TX = "tx?type={type}&id={id}"
    fun tx(type: TxType = TxType.EXPENSE, id: Long = 0) = "tx?type=${type.name}&id=$id"
}

private val tabs = listOf(
    NavItem(Routes.HOME, "Home", Icons.Outlined.Home, Icons.Rounded.Home),
    NavItem(Routes.TRANSACTIONS, "Activity", Icons.AutoMirrored.Outlined.ReceiptLong, Icons.AutoMirrored.Rounded.ReceiptLong),
    NavItem(Routes.LOANS, "Loans", Icons.Outlined.Handshake, Icons.Rounded.Handshake),
    NavItem(Routes.INVESTMENTS, "Invest", Icons.Outlined.Savings, Icons.Rounded.Savings),
)

@Composable
fun OletNavHost(vm: MainViewModel, profile: UserProfile) {
    if (!profile.loggedIn) {
        LoginScreen(vm)
        return
    }
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = tabs.any { it.route == route }
    var quickAdd by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(showBar, enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut()) {
                FloatingNavBar(
                    items = tabs,
                    currentRoute = route,
                    onSelect = { navigateTab(nav, it.route) },
                    onCenterClick = { quickAdd = true },
                    modifier = Modifier.navigationBarsPadding(),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { inner ->
        val bottom = PaddingValues(bottom = inner.calculateBottomPadding())
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(bottom).consumeWindowInsets(bottom),
            enterTransition = { fadeIn(tween(220)) },
            exitTransition = { fadeOut(tween(180)) },
        ) {
            composable(Routes.HOME) { HomeScreen(vm, profile, nav) }
            composable(Routes.TRANSACTIONS) { TransactionsScreen(vm, profile, nav) }
            composable(Routes.LOANS) { LoansScreen(vm, profile) }
            composable(Routes.INVESTMENTS) { InvestmentsScreen(vm, profile) }
            composable(
                Routes.SETTINGS,
                enterTransition = { slideInHorizontally(tween(260)) { it / 3 } + fadeIn(tween(260)) },
                popExitTransition = { slideOutHorizontally(tween(220)) { it / 3 } + fadeOut(tween(220)) },
            ) { SettingsScreen(vm, profile, nav) }
            composable(
                Routes.ACCOUNTS,
                enterTransition = { slideInHorizontally(tween(260)) { it / 3 } + fadeIn(tween(260)) },
                popExitTransition = { slideOutHorizontally(tween(220)) { it / 3 } + fadeOut(tween(220)) },
            ) { AccountsScreen(vm, profile, onBack = { nav.popBackStack() }) }
            composable(
                Routes.CATEGORIES,
                enterTransition = { slideInHorizontally(tween(260)) { it / 3 } + fadeIn(tween(260)) },
                popExitTransition = { slideOutHorizontally(tween(220)) { it / 3 } + fadeOut(tween(220)) },
            ) { CategoriesScreen(vm, onBack = { nav.popBackStack() }) }
            composable(
                Routes.TX,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType; defaultValue = TxType.EXPENSE.name },
                    navArgument("id") { type = NavType.LongType; defaultValue = 0L },
                ),
                enterTransition = { slideInHorizontally(tween(260)) { it / 3 } + fadeIn(tween(260)) },
                popExitTransition = { slideOutHorizontally(tween(220)) { it / 3 } + fadeOut(tween(220)) },
            ) { e ->
                val type = runCatching { TxType.valueOf(e.arguments?.getString("type") ?: "") }.getOrDefault(TxType.EXPENSE)
                val id = e.arguments?.getLong("id") ?: 0L
                AddTransactionScreen(vm, profile, initialType = type, editId = id, onDone = { nav.popBackStack() }, onManageCategories = { nav.navigate(Routes.CATEGORIES) })
            }
        }
    }

    if (quickAdd) {
        QuickAddSheet(
            onDismiss = { quickAdd = false },
            onPick = { kind ->
                quickAdd = false
                when (kind) {
                    QuickAdd.EXPENSE -> nav.navigate(Routes.tx(TxType.EXPENSE))
                    QuickAdd.INCOME -> nav.navigate(Routes.tx(TxType.INCOME))
                    QuickAdd.LOAN -> { vm.pendingAdd.value = "loan"; navigateTab(nav, Routes.LOANS) }
                    QuickAdd.INVESTMENT -> { vm.pendingAdd.value = "investment"; navigateTab(nav, Routes.INVESTMENTS) }
                }
            },
        )
    }
}

fun navigateTab(nav: NavHostController, route: String) {
    nav.navigate(route) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
