package app.olet.data

class Repository(private val db: AppDatabase) {
    val accounts = db.accounts().observeWithBalance()
    val categories = db.categories().observeAll()
    val transactions = db.transactions().observeDetails()
    val loans = db.loans().observeAll()
    val investments = db.investments().observeAll()

    suspend fun saveAccount(a: AccountEntity) = db.accounts().upsert(a)
    suspend fun deleteAccount(a: AccountEntity) = db.accounts().delete(a)

    suspend fun saveCategory(c: CategoryEntity) = db.categories().upsert(c)
    suspend fun deleteCategory(c: CategoryEntity) = db.categories().delete(c)

    suspend fun getTransaction(id: Long) = db.transactions().get(id)
    suspend fun saveTransaction(t: TransactionEntity) = db.transactions().upsert(t)
    suspend fun deleteTransaction(t: TransactionEntity) = db.transactions().delete(t)

    suspend fun saveLoan(l: LoanEntity) = db.loans().upsert(l)
    suspend fun deleteLoan(l: LoanEntity) = db.loans().delete(l)

    suspend fun saveInvestment(i: InvestmentEntity) = db.investments().upsert(i)
    suspend fun deleteInvestment(i: InvestmentEntity) = db.investments().delete(i)
}
