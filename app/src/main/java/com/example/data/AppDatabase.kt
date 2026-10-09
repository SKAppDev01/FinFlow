package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.FinanceDao
import com.example.data.model.AccountEntity
import com.example.data.model.AutomationRuleEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        TransactionEntity::class,
        RecurringRuleEntity::class,
        CategoryBudgetEntity::class,
        AccountEntity::class,
        AutomationRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finflow_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(dao: FinanceDao) {
            // Accounts
            val accounts = listOf(
                AccountEntity(
                    id = "checking",
                    name = "Primary Checking",
                    type = "CHECKING",
                    balance = 4850.40,
                    institution = "Chase",
                    lastFour = "4021",
                    colorHex = "#0D9488"
                ),
                AccountEntity(
                    id = "savings",
                    name = "High-Yield Savings",
                    type = "SAVINGS",
                    balance = 16420.00,
                    institution = "Ally Bank",
                    lastFour = "8820",
                    colorHex = "#10B981"
                ),
                AccountEntity(
                    id = "credit_card",
                    name = "Sapphire Rewards",
                    type = "CREDIT_CARD",
                    balance = -640.25,
                    institution = "Chase",
                    lastFour = "9312",
                    colorHex = "#F43F5E"
                ),
                AccountEntity(
                    id = "cash",
                    name = "Cash Wallet",
                    type = "CASH",
                    balance = 145.00,
                    institution = "Physical",
                    lastFour = "0000",
                    colorHex = "#F59E0B"
                )
            )
            dao.insertAccounts(accounts)

            // Budgets
            val budgets = listOf(
                CategoryBudgetEntity(category = "Food & Dining", monthlyLimit = 650.0, colorHex = "#F59E0B"),
                CategoryBudgetEntity(category = "Housing", monthlyLimit = 1600.0, colorHex = "#8B5CF6"),
                CategoryBudgetEntity(category = "Transportation", monthlyLimit = 280.0, colorHex = "#3B82F6"),
                CategoryBudgetEntity(category = "Shopping", monthlyLimit = 350.0, colorHex = "#EC4899"),
                CategoryBudgetEntity(category = "Entertainment", monthlyLimit = 180.0, colorHex = "#06B6D4"),
                CategoryBudgetEntity(category = "Utilities", monthlyLimit = 220.0, colorHex = "#6366F1"),
                CategoryBudgetEntity(category = "Health & Wellness", monthlyLimit = 150.0, colorHex = "#10B981"),
                CategoryBudgetEntity(category = "Other", monthlyLimit = 100.0, colorHex = "#64748B")
            )
            dao.insertBudgets(budgets)

            // Automation Rules for Intelligent categorization
            val autoRules = listOf(
                AutomationRuleEntity(keyword = "uber", targetCategory = "Transportation", targetAccountId = "credit_card"),
                AutomationRuleEntity(keyword = "lyft", targetCategory = "Transportation", targetAccountId = "credit_card"),
                AutomationRuleEntity(keyword = "whole foods", targetCategory = "Food & Dining", targetAccountId = "checking"),
                AutomationRuleEntity(keyword = "trader joe", targetCategory = "Food & Dining", targetAccountId = "checking"),
                AutomationRuleEntity(keyword = "starbucks", targetCategory = "Food & Dining", targetAccountId = "credit_card"),
                AutomationRuleEntity(keyword = "amazon", targetCategory = "Shopping", targetAccountId = "credit_card"),
                AutomationRuleEntity(keyword = "target", targetCategory = "Shopping", targetAccountId = "credit_card"),
                AutomationRuleEntity(keyword = "shell", targetCategory = "Transportation", targetAccountId = "credit_card"),
                AutomationRuleEntity(keyword = "chevron", targetCategory = "Transportation", targetAccountId = "credit_card"),
                AutomationRuleEntity(keyword = "netflix", targetCategory = "Entertainment", targetAccountId = "credit_card"),
                AutomationRuleEntity(keyword = "spotify", targetCategory = "Entertainment", targetAccountId = "credit_card"),
                AutomationRuleEntity(keyword = "payroll", targetCategory = "Salary", targetAccountId = "checking")
            )
            dao.insertAutomationRules(autoRules)

            val now = System.currentTimeMillis()
            val oneDayMs = 86_400_000L

            // Recurring Rules
            val recurringRules = listOf(
                RecurringRuleEntity(
                    title = "Apartment Rent",
                    amount = 1500.0,
                    type = "EXPENSE",
                    category = "Housing",
                    accountId = "checking",
                    frequency = "MONTHLY",
                    dayOfMonth = 1,
                    nextDueDate = now + (oneDayMs * 18),
                    lastExecutedDate = now - (oneDayMs * 8),
                    isActive = true,
                    autoProcess = true
                ),
                RecurringRuleEntity(
                    title = "Netflix 4K Premium",
                    amount = 22.99,
                    type = "EXPENSE",
                    category = "Entertainment",
                    accountId = "credit_card",
                    frequency = "MONTHLY",
                    dayOfMonth = 14,
                    nextDueDate = now + (oneDayMs * 5),
                    lastExecutedDate = now - (oneDayMs * 25),
                    isActive = true,
                    autoProcess = true
                ),
                RecurringRuleEntity(
                    title = "Gym Membership",
                    amount = 75.00,
                    type = "EXPENSE",
                    category = "Health & Wellness",
                    accountId = "credit_card",
                    frequency = "MONTHLY",
                    dayOfMonth = 10,
                    nextDueDate = now + (oneDayMs * 1),
                    lastExecutedDate = now - (oneDayMs * 29),
                    isActive = true,
                    autoProcess = true
                ),
                RecurringRuleEntity(
                    title = "Bi-Weekly Salary",
                    amount = 3200.0,
                    type = "INCOME",
                    category = "Salary",
                    accountId = "checking",
                    frequency = "WEEKLY",
                    dayOfMonth = 15,
                    nextDueDate = now + (oneDayMs * 6),
                    lastExecutedDate = now - (oneDayMs * 8),
                    isActive = true,
                    autoProcess = true
                ),
                RecurringRuleEntity(
                    title = "High-Speed Fiber Internet",
                    amount = 69.99,
                    type = "EXPENSE",
                    category = "Utilities",
                    accountId = "checking",
                    frequency = "MONTHLY",
                    dayOfMonth = 20,
                    nextDueDate = now + (oneDayMs * 11),
                    lastExecutedDate = now - (oneDayMs * 19),
                    isActive = true,
                    autoProcess = true
                )
            )
            dao.insertRecurringRules(recurringRules)

            // Initial transactions showing automated and manual expenses
            val initialTransactions = listOf(
                TransactionEntity(
                    title = "Tech Corp Bi-Weekly Payroll",
                    amount = 3200.0,
                    type = "INCOME",
                    category = "Salary",
                    accountId = "checking",
                    timestamp = now - (oneDayMs * 8),
                    note = "Direct deposit automated payroll",
                    isAutomated = true,
                    sourceAlertSnippet = "Alert: Direct deposit from TECH CORP of $3,200.00 to Checking *4021"
                ),
                TransactionEntity(
                    title = "Apartment Rent",
                    amount = 1500.0,
                    type = "EXPENSE",
                    category = "Housing",
                    accountId = "checking",
                    timestamp = now - (oneDayMs * 8),
                    note = "Automated monthly rent scheduled transfer",
                    isAutomated = true,
                    sourceAlertSnippet = "Scheduled payment: $1,500.00 processed for APARTMENT MANAGEMENT"
                ),
                TransactionEntity(
                    title = "Whole Foods Market",
                    amount = 138.45,
                    type = "EXPENSE",
                    category = "Food & Dining",
                    accountId = "checking",
                    timestamp = now - (oneDayMs * 1),
                    note = "Weekly organic groceries",
                    isAutomated = true,
                    sourceAlertSnippet = "Your Chase Visa ending 4021 was charged $138.45 at WHOLE FOODS MARKET"
                ),
                TransactionEntity(
                    title = "Trader Joe's",
                    amount = 54.20,
                    type = "EXPENSE",
                    category = "Food & Dining",
                    accountId = "checking",
                    timestamp = now - (oneDayMs * 4),
                    note = "Snacks & pantry items",
                    isAutomated = true,
                    sourceAlertSnippet = "Chase Alert: $54.20 paid to TRADER JOE'S on 10/05"
                ),
                TransactionEntity(
                    title = "Uber Ride downtown",
                    amount = 26.50,
                    type = "EXPENSE",
                    category = "Transportation",
                    accountId = "credit_card",
                    timestamp = now - (oneDayMs * 2),
                    note = "Trip to client meeting",
                    isAutomated = true,
                    sourceAlertSnippet = "Apple Card: $26.50 spent at UBER TRIP"
                ),
                TransactionEntity(
                    title = "Shell Gas Station",
                    amount = 45.10,
                    type = "EXPENSE",
                    category = "Transportation",
                    accountId = "credit_card",
                    timestamp = now - (oneDayMs * 6),
                    note = "Fuel refill",
                    isAutomated = true,
                    sourceAlertSnippet = "Alert: $45.10 approved at SHELL OIL 1042"
                ),
                TransactionEntity(
                    title = "Amazon.com",
                    amount = 89.90,
                    type = "EXPENSE",
                    category = "Shopping",
                    accountId = "credit_card",
                    timestamp = now - (oneDayMs * 3),
                    note = "Desk accessories & cables",
                    isAutomated = true,
                    sourceAlertSnippet = "Purchase of $89.90 on Card ending 9312 at AMAZON.COM"
                ),
                TransactionEntity(
                    title = "Starbucks Reserve",
                    amount = 8.75,
                    type = "EXPENSE",
                    category = "Food & Dining",
                    accountId = "credit_card",
                    timestamp = now - (oneDayMs * 0) - (3600_000L * 3),
                    note = "Cold brew & pastry",
                    isAutomated = false,
                    sourceAlertSnippet = null
                ),
                TransactionEntity(
                    title = "Electric & Gas Utility",
                    amount = 94.60,
                    type = "EXPENSE",
                    category = "Utilities",
                    accountId = "checking",
                    timestamp = now - (oneDayMs * 5),
                    note = "Monthly utility bill",
                    isAutomated = true,
                    sourceAlertSnippet = "Scheduled utility draft of $94.60 to CITY POWER"
                ),
                TransactionEntity(
                    title = "Cinema & Snacks",
                    amount = 34.00,
                    type = "EXPENSE",
                    category = "Entertainment",
                    accountId = "credit_card",
                    timestamp = now - (oneDayMs * 7),
                    note = "Movie tickets",
                    isAutomated = false,
                    sourceAlertSnippet = null
                ),
                TransactionEntity(
                    title = "Target",
                    amount = 62.30,
                    type = "EXPENSE",
                    category = "Shopping",
                    accountId = "credit_card",
                    timestamp = now - (oneDayMs * 9),
                    note = "Home essentials",
                    isAutomated = true,
                    sourceAlertSnippet = "Chase Alert: $62.30 charged at TARGET T-241"
                ),
                TransactionEntity(
                    title = "High-Yield Savings Interest",
                    amount = 48.15,
                    type = "INCOME",
                    category = "Investment",
                    accountId = "savings",
                    timestamp = now - (oneDayMs * 8),
                    note = "Monthly compounding interest APY 4.5%",
                    isAutomated = true,
                    sourceAlertSnippet = "Ally Bank: Interest credit of $48.15 added to account *8820"
                )
            )
            dao.insertTransactions(initialTransactions)
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getInstance(context).financeDao()
                populateInitialData(dao)
            }
        }
    }
}
