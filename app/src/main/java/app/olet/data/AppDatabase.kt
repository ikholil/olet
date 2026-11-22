package app.olet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AccountEntity::class, CategoryEntity::class, TransactionEntity::class,
        LoanEntity::class, InvestmentEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accounts(): AccountDao
    abstract fun categories(): CategoryDao
    abstract fun transactions(): TransactionDao
    abstract fun loans(): LoanDao
    abstract fun investments(): InvestmentDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "olet.db")
                .addCallback(SeedCallback)
                .build()
    }
}

private object SeedCallback : RoomDatabase.Callback() {
    private val expense = listOf(
        Triple("Food", "food", 0xFFFF7A59),
        Triple("Transport", "transport", 0xFF3D8BFF),
        Triple("Utilities", "utilities", 0xFFFFB020),
        Triple("Entertainment", "entertainment", 0xFFE5508C),
        Triple("Shopping", "shopping", 0xFF9B59F6),
        Triple("Health", "health", 0xFF1FB57A),
        Triple("Other", "other", 0xFF7C8598),
    )
    private val income = listOf(
        Triple("Salary", "salary", 0xFF1FB57A),
        Triple("Business", "business", 0xFF3D8BFF),
        Triple("Gift", "gift", 0xFFE5508C),
        Triple("Other", "other", 0xFF7C8598),
    )

    override fun onCreate(db: SupportSQLiteDatabase) {
        expense.forEach { (n, i, c) -> insertCategory(db, n, TxType.EXPENSE, i, c) }
        income.forEach { (n, i, c) -> insertCategory(db, n, TxType.INCOME, i, c) }
        db.execSQL(
            "INSERT INTO accounts (name, type, openingBalance) VALUES (?, ?, ?)",
            arrayOf<Any>("Cash", AccountType.CASH.name, 0.0)
        )
    }

    private fun insertCategory(db: SupportSQLiteDatabase, name: String, type: TxType, icon: String, color: Long) {
        db.execSQL(
            "INSERT INTO categories (name, type, icon, color) VALUES (?, ?, ?, ?)",
            arrayOf<Any>(name, type.name, icon, color)
        )
    }
}
