package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE", "INCOME", "TRANSFER"
    val category: String, // "Food & Dining", "Transportation", "Shopping", "Entertainment", "Housing", "Utilities", "Salary", "Investment", "Health", "Other"
    val accountId: String, // "checking", "savings", "credit_card", "cash"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val isAutomated: Boolean = false,
    val sourceAlertSnippet: String? = null
)

@Entity(tableName = "recurring_rules")
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val category: String,
    val accountId: String,
    val frequency: String, // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val dayOfMonth: Int = 1,
    val nextDueDate: Long,
    val lastExecutedDate: Long = 0,
    val isActive: Boolean = true,
    val autoProcess: Boolean = true
)

@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val colorHex: String = "#10B981"
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String, // e.g. "checking", "savings", "credit_card", "cash"
    val name: String,
    val type: String, // "CHECKING", "SAVINGS", "CREDIT_CARD", "CASH"
    val balance: Double,
    val institution: String,
    val lastFour: String = "1234",
    val colorHex: String = "#0D9488"
)

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keyword: String,
    val targetCategory: String,
    val targetAccountId: String = "checking",
    val isEnabled: Boolean = true
)
