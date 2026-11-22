package app.olet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query(
        """
        SELECT a.*, a.openingBalance + COALESCE((
            SELECT SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END)
            FROM transactions t WHERE t.accountId = a.id
        ), 0) AS balance
        FROM accounts a ORDER BY a.id
        """
    )
    fun observeWithBalance(): Flow<List<AccountWithBalance>>

    @Upsert suspend fun upsert(account: AccountEntity): Long
    @Delete suspend fun delete(account: AccountEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY type, id")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Upsert suspend fun upsert(category: CategoryEntity): Long
    @Delete suspend fun delete(category: CategoryEntity)
}

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT t.*, c.name AS categoryName, c.icon AS categoryIcon, c.color AS categoryColor,
               a.name AS accountName
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        LEFT JOIN accounts a ON t.accountId = a.id
        ORDER BY t.date DESC, t.id DESC
        """
    )
    fun observeDetails(): Flow<List<TransactionDetail>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun get(id: Long): TransactionEntity?

    @Upsert suspend fun upsert(tx: TransactionEntity): Long
    @Delete suspend fun delete(tx: TransactionEntity)
}

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY (paid >= amount), date DESC, id DESC")
    fun observeAll(): Flow<List<LoanEntity>>

    @Upsert suspend fun upsert(loan: LoanEntity): Long
    @Delete suspend fun delete(loan: LoanEntity)
}

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments ORDER BY active DESC, date DESC, id DESC")
    fun observeAll(): Flow<List<InvestmentEntity>>

    @Upsert suspend fun upsert(item: InvestmentEntity): Long
    @Delete suspend fun delete(item: InvestmentEntity)
}
