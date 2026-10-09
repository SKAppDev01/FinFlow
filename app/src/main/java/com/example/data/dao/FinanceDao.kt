package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountEntity
import com.example.data.model.AutomationRuleEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    // Accounts
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Query("UPDATE accounts SET balance = :balance WHERE id = :id")
    suspend fun updateAccountBalance(id: String, balance: Double)

    // Recurring Rules
    @Query("SELECT * FROM recurring_rules ORDER BY nextDueDate ASC")
    fun getAllRecurringRules(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1")
    suspend fun getActiveRecurringRulesSync(): List<RecurringRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringRule(rule: RecurringRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringRules(rules: List<RecurringRuleEntity>)

    @Update
    suspend fun updateRecurringRule(rule: RecurringRuleEntity)

    @Query("DELETE FROM recurring_rules WHERE id = :id")
    suspend fun deleteRecurringRuleById(id: Long)

    // Category Budgets
    @Query("SELECT * FROM category_budgets")
    fun getAllBudgets(): Flow<List<CategoryBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: CategoryBudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<CategoryBudgetEntity>)

    @Update
    suspend fun updateBudget(budget: CategoryBudgetEntity)

    @Query("DELETE FROM category_budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Long)

    // Automation Rules
    @Query("SELECT * FROM automation_rules")
    fun getAllAutomationRules(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules WHERE isEnabled = 1")
    suspend fun getActiveAutomationRulesSync(): List<AutomationRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomationRule(rule: AutomationRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomationRules(rules: List<AutomationRuleEntity>)

    @Update
    suspend fun updateAutomationRule(rule: AutomationRuleEntity)

    @Query("DELETE FROM automation_rules WHERE id = :id")
    suspend fun deleteAutomationRuleById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    @Query("DELETE FROM recurring_rules")
    suspend fun clearAllRecurringRules()

    @Query("DELETE FROM category_budgets")
    suspend fun clearAllBudgets()

    @Query("DELETE FROM automation_rules")
    suspend fun clearAllAutomationRules()

    @Query("DELETE FROM accounts")
    suspend fun clearAllAccounts()
}
