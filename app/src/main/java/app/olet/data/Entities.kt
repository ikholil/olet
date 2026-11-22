package app.olet.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TxType { EXPENSE, INCOME }

enum class AccountType(val label: String) {
    CASH("Cash"), BANK("Bank"), MOBILE("Mobile Banking"), CARD("Card"), OTHER("Other")
}

/** LENT = I gave money, they owe me. BORROWED = I took money, I owe them. */
enum class LoanType { LENT, BORROWED }

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val openingBalance: Double = 0.0,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TxType,
    val icon: String,
    val color: Long,
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TxType,
    val title: String,
    val amount: Double,
    val categoryId: Long?,
    val accountId: Long?,
    val date: Long,
    val note: String = "",
)

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val person: String,
    val phone: String = "",
    val type: LoanType,
    val amount: Double,
    val paid: Double = 0.0,
    val date: Long,
    val dueDate: Long? = null,
    val note: String = "",
) {
    val remaining: Double get() = (amount - paid).coerceAtLeast(0.0)
    val settled: Boolean get() = paid >= amount
}

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contact: String = "",
    val amount: Double,
    val date: Long,
    val note: String = "",
    val active: Boolean = true,
)

data class TransactionDetail(
    @Embedded val tx: TransactionEntity,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: Long?,
    val accountName: String?,
)

data class AccountWithBalance(
    @Embedded val account: AccountEntity,
    val balance: Double,
)
