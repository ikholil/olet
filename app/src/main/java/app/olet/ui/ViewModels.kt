package app.olet.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.olet.OletApp
import app.olet.auth.GoogleAuth
import app.olet.auth.GoogleResult
import app.olet.data.AccountEntity
import app.olet.data.AccountWithBalance
import app.olet.data.CategoryEntity
import app.olet.data.InvestmentEntity
import app.olet.data.LoanEntity
import app.olet.data.LoanType
import app.olet.data.ThemeMode
import app.olet.data.TransactionDetail
import app.olet.data.TransactionEntity
import app.olet.data.TxType
import app.olet.data.UserProfile
import app.olet.ui.util.toLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

/**
 * Single app-wide ViewModel: the data set of a personal expense app is small, and sharing
 * one instance keeps every screen in sync without extra plumbing.
 */
class MainViewModel(private val app: OletApp) : ViewModel() {
    private val repo = app.repository
    private val prefs = app.prefs

    private fun <T> kotlinx.coroutines.flow.Flow<T>.state(initial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

    /** null until preferences have loaded (used to avoid flashing the login screen). */
    val profile: StateFlow<UserProfile?> = prefs.profile.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val accounts: StateFlow<List<AccountWithBalance>> = repo.accounts.state(emptyList())
    val categories: StateFlow<List<CategoryEntity>> = repo.categories.state(emptyList())
    val transactions: StateFlow<List<TransactionDetail>> = repo.transactions.state(emptyList())
    val loans: StateFlow<List<LoanEntity>> = repo.loans.state(emptyList())
    val investments: StateFlow<List<InvestmentEntity>> = repo.investments.state(emptyList())

    val totalBalance: StateFlow<Double> = accounts.map { list -> list.sumOf { it.balance } }.state(0.0)

    data class MonthSummary(val income: Double = 0.0, val expense: Double = 0.0) {
        val net get() = income - expense
    }

    val thisMonth: StateFlow<MonthSummary> = transactions.map { summaryFor(it, YearMonth.now()) }.state(MonthSummary())

    fun summaryFor(list: List<TransactionDetail>, month: YearMonth): MonthSummary {
        var inc = 0.0
        var exp = 0.0
        list.forEach {
            if (YearMonth.from(it.tx.date.toLocalDate()) == month) {
                if (it.tx.type == TxType.INCOME) inc += it.tx.amount else exp += it.tx.amount
            }
        }
        return MonthSummary(inc, exp)
    }

    data class LoanSummary(val toReceive: Double = 0.0, val toPay: Double = 0.0)

    val loanSummary: StateFlow<LoanSummary> = loans.map { list ->
        LoanSummary(
            toReceive = list.filter { it.type == LoanType.LENT }.sumOf { it.remaining },
            toPay = list.filter { it.type == LoanType.BORROWED }.sumOf { it.remaining },
        )
    }.state(LoanSummary())

    // ---- Transactions screen filtering ----
    val selectedMonth = MutableStateFlow(YearMonth.now())
    val typeFilter = MutableStateFlow<TxType?>(null)

    val filteredTransactions: StateFlow<List<TransactionDetail>> =
        combine(transactions, selectedMonth, typeFilter) { list, month, type ->
            list.filter {
                YearMonth.from(it.tx.date.toLocalDate()) == month && (type == null || it.tx.type == type)
            }
        }.state(emptyList())

    val selectedMonthSummary: StateFlow<MonthSummary> =
        combine(transactions, selectedMonth) { list, m -> summaryFor(list, m) }.state(MonthSummary())

    // ---- Auth / settings ----
    fun continueAsGuest(name: String) = viewModelScope.launch {
        prefs.signIn(name.trim().ifEmpty { "Friend" }, "", google = false)
    }

    suspend fun signInWithGoogle(activityContext: Context): GoogleResult {
        val result = GoogleAuth.signIn(activityContext)
        if (result is GoogleResult.Success) prefs.signIn(result.name, result.email, google = true)
        return result
    }

    fun signOut(context: Context) = viewModelScope.launch {
        GoogleAuth.signOut(context)
        prefs.signOut()
    }

    fun updateName(name: String) = viewModelScope.launch { prefs.updateName(name.trim()) }
    fun setCurrency(symbol: String) = viewModelScope.launch { prefs.setCurrency(symbol) }
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { prefs.setTheme(mode) }

    // ---- CRUD ----
    suspend fun getTransaction(id: Long) = repo.getTransaction(id)
    fun saveTransaction(t: TransactionEntity) = viewModelScope.launch { repo.saveTransaction(t) }
    fun deleteTransaction(t: TransactionEntity) = viewModelScope.launch { repo.deleteTransaction(t) }

    fun saveAccount(a: AccountEntity) = viewModelScope.launch { repo.saveAccount(a) }
    fun deleteAccount(a: AccountEntity) = viewModelScope.launch { repo.deleteAccount(a) }

    fun saveCategory(c: CategoryEntity) = viewModelScope.launch { repo.saveCategory(c) }
    fun deleteCategory(c: CategoryEntity) = viewModelScope.launch { repo.deleteCategory(c) }

    fun saveLoan(l: LoanEntity) = viewModelScope.launch { repo.saveLoan(l) }
    fun deleteLoan(l: LoanEntity) = viewModelScope.launch { repo.deleteLoan(l) }
    fun addLoanPayment(l: LoanEntity, amount: Double) = viewModelScope.launch {
        repo.saveLoan(l.copy(paid = (l.paid + amount).coerceAtMost(l.amount)))
    }

    fun saveInvestment(i: InvestmentEntity) = viewModelScope.launch { repo.saveInvestment(i) }
    fun deleteInvestment(i: InvestmentEntity) = viewModelScope.launch { repo.deleteInvestment(i) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as OletApp
                MainViewModel(app)
            }
        }
    }
}
