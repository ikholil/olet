package app.olet

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.olet.data.AccountEntity
import app.olet.data.AccountType
import app.olet.data.AppDatabase
import app.olet.data.LoanEntity
import app.olet.data.LoanType
import app.olet.data.TransactionEntity
import app.olet.data.TxType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseTest {
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        ctx.deleteDatabase("olet.db")
        db = AppDatabase.build(ctx)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun seedsDefaultCategoriesAndCashAccount() = runBlocking {
        val cats = db.categories().observeAll().first()
        val expenseNames = cats.filter { it.type == TxType.EXPENSE }.map { it.name }
        assertTrue(expenseNames.containsAll(listOf("Food", "Transport", "Utilities", "Entertainment", "Other")))
        assertTrue(cats.any { it.type == TxType.INCOME && it.name == "Salary" })
        val accounts = db.accounts().observeWithBalance().first()
        assertEquals(listOf("Cash"), accounts.map { it.account.name })
    }

    @Test
    fun accountBalanceIncludesOpeningIncomeAndExpense() = runBlocking {
        val bank = db.accounts().upsert(AccountEntity(name = "Bank", type = AccountType.BANK, openingBalance = 1000.0))
        val food = db.categories().observeAll().first().first { it.name == "Food" }.id
        db.transactions().upsert(TransactionEntity(type = TxType.INCOME, title = "Salary", amount = 500.0, categoryId = null, accountId = bank, date = 1))
        val lunchId = db.transactions().upsert(TransactionEntity(type = TxType.EXPENSE, title = "Lunch", amount = 120.5, categoryId = food, accountId = bank, date = 2))
        val balance = db.accounts().observeWithBalance().first().first { it.account.id == bank }.balance
        assertEquals(1379.5, balance, 0.001)

        val details = db.transactions().observeDetails().first()
        assertEquals("Lunch", details.first().tx.title) // newest first
        assertEquals("Food", details.first().categoryName)
        assertEquals("Bank", details.first().accountName)

        db.transactions().delete(db.transactions().get(lunchId)!!)
        assertEquals(1500.0, db.accounts().observeWithBalance().first().first { it.account.id == bank }.balance, 0.001)
    }

    @Test
    fun deletingCategoryKeepsTransactionAsUncategorized() = runBlocking {
        val cat = db.categories().observeAll().first().first { it.name == "Transport" }
        db.transactions().upsert(TransactionEntity(type = TxType.EXPENSE, title = "Bus", amount = 20.0, categoryId = cat.id, accountId = null, date = 1))
        db.categories().delete(cat)
        val d = db.transactions().observeDetails().first().single()
        assertEquals(null, d.categoryName)
        assertEquals(20.0, d.tx.amount, 0.0)
    }

    @Test
    fun loansOrderOpenFirstAndComputeRemaining() = runBlocking {
        db.loans().upsert(LoanEntity(person = "Rahim", type = LoanType.LENT, amount = 1000.0, paid = 1000.0, date = 5))
        db.loans().upsert(LoanEntity(person = "Karim", type = LoanType.LENT, amount = 500.0, paid = 200.0, date = 1))
        val loans = db.loans().observeAll().first()
        assertEquals(listOf("Karim", "Rahim"), loans.map { it.person })
        assertEquals(300.0, loans.first().remaining, 0.0)
        assertTrue(loans.last().settled)
    }
}
