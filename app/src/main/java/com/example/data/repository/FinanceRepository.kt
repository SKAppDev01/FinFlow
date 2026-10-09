package com.example.data.repository

import com.example.data.dao.FinanceDao
import com.example.data.model.AccountEntity
import com.example.data.model.AutomationRuleEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

data class ParsedAlertResult(
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val accountId: String,
    val originalText: String,
    val matchedRuleKeyword: String? = null
)

class FinanceRepository(private val dao: FinanceDao) {

    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allAccounts: Flow<List<AccountEntity>> = dao.getAllAccounts()
    val allRecurringRules: Flow<List<RecurringRuleEntity>> = dao.getAllRecurringRules()
    val allBudgets: Flow<List<CategoryBudgetEntity>> = dao.getAllBudgets()
    val allAutomationRules: Flow<List<AutomationRuleEntity>> = dao.getAllAutomationRules()

    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>> =
        dao.getTransactionsBetween(start, end)

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val id = dao.insertTransaction(transaction)
        adjustAccountBalance(transaction.accountId, transaction.amount, transaction.type, isReversal = false)
        return id
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
        adjustAccountBalance(transaction.accountId, transaction.amount, transaction.type, isReversal = true)
    }

    private suspend fun adjustAccountBalance(accountId: String, amount: Double, type: String, isReversal: Boolean) {
        val account = dao.getAccountById(accountId) ?: return
        val multiplier = if (isReversal) -1.0 else 1.0
        val balanceDelta = when (type) {
            "INCOME" -> amount * multiplier
            "EXPENSE" -> -amount * multiplier
            else -> 0.0
        }
        dao.updateAccountBalance(accountId, account.balance + balanceDelta)
    }

    // Automated Recurring Schedule Processor
    suspend fun processDueRecurringRules(): Int {
        val rules = dao.getActiveRecurringRulesSync()
        val now = System.currentTimeMillis()
        var processedCount = 0

        for (rule in rules) {
            if (rule.autoProcess && rule.nextDueDate <= now) {
                // Generate automated transaction
                val transaction = TransactionEntity(
                    title = rule.title,
                    amount = rule.amount,
                    type = rule.type,
                    category = rule.category,
                    accountId = rule.accountId,
                    timestamp = rule.nextDueDate,
                    note = "Auto-generated recurring ${rule.frequency.lowercase(Locale.ROOT)} bill",
                    isAutomated = true,
                    sourceAlertSnippet = "Automated Recurring Schedule: ${rule.title} ($${String.format(Locale.US, "%.2f", rule.amount)})"
                )
                insertTransaction(transaction)

                // Advance next due date
                val nextDate = calculateNextDueDate(rule.nextDueDate, rule.frequency)
                val updatedRule = rule.copy(
                    lastExecutedDate = now,
                    nextDueDate = nextDate
                )
                dao.updateRecurringRule(updatedRule)
                processedCount++
            }
        }
        return processedCount
    }

    private fun calculateNextDueDate(currentDue: Long, frequency: String): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentDue
        when (frequency.uppercase(Locale.ROOT)) {
            "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "BIWEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 2)
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
            "YEARLY" -> calendar.add(Calendar.YEAR, 1)
            else -> calendar.add(Calendar.MONTH, 1)
        }
        return calendar.timeInMillis
    }

    // Natural Language Bank SMS / Push Alert Parser
    suspend fun parseBankAlert(rawText: String): ParsedAlertResult {
        val cleanText = rawText.trim()
        val lowerText = cleanText.lowercase(Locale.ROOT)

        // 1. Extract Amount
        val amountPattern = Pattern.compile("(?i)(?:\\$|usd\\s*|amount\\s*:?\\s*\\$?|charged\\s*\\$?|spent\\s*\\$?|of\\s*\\$?)([0-9]+(?:\\.[0-9]{2})?)")
        val amountMatcher = amountPattern.matcher(cleanText)
        var extractedAmount = 0.0
        if (amountMatcher.find()) {
            extractedAmount = amountMatcher.group(1)?.toDoubleOrNull() ?: 0.0
        } else {
            // Fallback match any standalone currency decimal
            val fallbackMatcher = Pattern.compile("([0-9]+\\.[0-9]{2})").matcher(cleanText)
            if (fallbackMatcher.find()) {
                extractedAmount = fallbackMatcher.group(1)?.toDoubleOrNull() ?: 0.0
            }
        }

        // 2. Determine Type (Expense vs Income)
        val isIncome = lowerText.contains("deposit") ||
                lowerText.contains("credit") ||
                lowerText.contains("received") ||
                lowerText.contains("salary") ||
                lowerText.contains("payroll") ||
                lowerText.contains("refund")

        val txType = if (isIncome) "INCOME" else "EXPENSE"

        // 3. Extract Merchant / Payee
        var merchant = "Card Transaction"
        val merchantPattern = Pattern.compile("(?i)(?:at|paid to|to|from|for|merchant:)\\s+([A-Za-z0-9&'\\.\\-\\s]{2,28}?)(?:\\s+(?:on|with|using|ending|in|via|\\*|card|date|ref)|$)")
        val merchantMatcher = merchantPattern.matcher(cleanText)
        if (merchantMatcher.find()) {
            merchant = merchantMatcher.group(1)?.trim() ?: merchant
        } else {
            // Look for known merchants
            val knownVendors = listOf(
                "Whole Foods", "Trader Joe's", "Starbucks", "Uber", "Lyft", "Amazon",
                "Target", "Walmart", "Netflix", "Spotify", "Shell", "Chevron", "Apple"
            )
            for (vendor in knownVendors) {
                if (lowerText.contains(vendor.lowercase(Locale.ROOT))) {
                    merchant = vendor
                    break
                }
            }
        }

        // Clean merchant string
        merchant = merchant.replace(Regex("(?i)\\b(on|with|using|card|ending)\\b.*"), "").trim()
        if (merchant.isBlank()) merchant = "Automated Expense"

        // 4. Auto-Categorize & Assign Account via Active Rules
        val autoRules = dao.getActiveAutomationRulesSync()
        var category = if (isIncome) "Income" else "Other"
        var accountId = if (lowerText.contains("credit") || lowerText.contains("card") || lowerText.contains("9312")) "credit_card" else "checking"
        var matchedKeyword: String? = null

        for (rule in autoRules) {
            val kw = rule.keyword.lowercase(Locale.ROOT)
            if (lowerText.contains(kw) || merchant.lowercase(Locale.ROOT).contains(kw)) {
                category = rule.targetCategory
                if (rule.targetAccountId.isNotBlank()) {
                    accountId = rule.targetAccountId
                }
                matchedKeyword = rule.keyword
                break
            }
        }

        // Account heuristics if specific account numbers mentioned
        if (lowerText.contains("4021") || lowerText.contains("checking")) {
            accountId = "checking"
        } else if (lowerText.contains("8820") || lowerText.contains("savings")) {
            accountId = "savings"
        } else if (lowerText.contains("9312") || lowerText.contains("apple card") || lowerText.contains("visa")) {
            accountId = "credit_card"
        }

        return ParsedAlertResult(
            title = merchant,
            amount = if (extractedAmount > 0) extractedAmount else 25.00,
            type = txType,
            category = category,
            accountId = accountId,
            originalText = cleanText,
            matchedRuleKeyword = matchedKeyword
        )
    }

    // Rules & Budgets
    suspend fun insertRecurringRule(rule: RecurringRuleEntity): Long = dao.insertRecurringRule(rule)
    suspend fun updateRecurringRule(rule: RecurringRuleEntity) = dao.updateRecurringRule(rule)
    suspend fun deleteRecurringRule(id: Long) = dao.deleteRecurringRuleById(id)

    suspend fun insertBudget(budget: CategoryBudgetEntity): Long = dao.insertBudget(budget)
    suspend fun updateBudget(budget: CategoryBudgetEntity) = dao.updateBudget(budget)
    suspend fun deleteBudget(id: Long) = dao.deleteBudgetById(id)

    suspend fun insertAutomationRule(rule: AutomationRuleEntity): Long = dao.insertAutomationRule(rule)
    suspend fun updateAutomationRule(rule: AutomationRuleEntity) = dao.updateAutomationRule(rule)
    suspend fun deleteAutomationRule(id: Long) = dao.deleteAutomationRuleById(id)

    suspend fun clearAllTransactions() {
        dao.clearAllTransactions()
    }

    suspend fun clearAllData() {
        dao.clearAllTransactions()
        dao.clearAllRecurringRules()
        dao.clearAllBudgets()
        dao.clearAllAutomationRules()
        dao.clearAllAccounts()
        // Provide clean base accounts with 0.00 balance so user has an active ledger ready
        val cleanAccounts = listOf(
            AccountEntity(
                id = "checking",
                name = "Primary Checking",
                type = "CHECKING",
                balance = 0.00,
                institution = "Bank",
                lastFour = "0001",
                colorHex = "#0D9488"
            ),
            AccountEntity(
                id = "savings",
                name = "Savings Account",
                type = "SAVINGS",
                balance = 0.00,
                institution = "Bank",
                lastFour = "0002",
                colorHex = "#10B981"
            ),
            AccountEntity(
                id = "cash",
                name = "Cash Wallet",
                type = "CASH",
                balance = 0.00,
                institution = "Physical",
                lastFour = "0000",
                colorHex = "#F59E0B"
            )
        )
        dao.insertAccounts(cleanAccounts)

        // Provide clean standard category budgets with $0 spent
        val cleanBudgets = listOf(
            CategoryBudgetEntity(category = "Food & Dining", monthlyLimit = 500.0, colorHex = "#F59E0B"),
            CategoryBudgetEntity(category = "Housing", monthlyLimit = 1500.0, colorHex = "#8B5CF6"),
            CategoryBudgetEntity(category = "Transportation", monthlyLimit = 300.0, colorHex = "#3B82F6"),
            CategoryBudgetEntity(category = "Shopping", monthlyLimit = 300.0, colorHex = "#EC4899"),
            CategoryBudgetEntity(category = "Utilities", monthlyLimit = 200.0, colorHex = "#6366F1"),
            CategoryBudgetEntity(category = "Entertainment", monthlyLimit = 150.0, colorHex = "#06B6D4"),
            CategoryBudgetEntity(category = "Health & Wellness", monthlyLimit = 100.0, colorHex = "#10B981"),
            CategoryBudgetEntity(category = "Other", monthlyLimit = 100.0, colorHex = "#64748B")
        )
        dao.insertBudgets(cleanBudgets)
    }
}
