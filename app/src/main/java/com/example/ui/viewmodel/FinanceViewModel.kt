package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AutomationRuleEntity
import com.example.data.model.CategoryBudgetEntity
import com.example.data.model.RecurringRuleEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.FinanceRepository
import com.example.data.repository.ParsedAlertResult
import com.example.data.security.SecurityLockType
import com.example.data.security.SecurityManager
import com.example.ui.components.BarComparisonGroup
import com.example.ui.components.ChartSlice
import com.example.ui.components.DailySpendItem
import com.example.ui.components.getCategoryColor
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimeframeFilter(val label: String) {
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week"),
    LAST_MONTH("Last Month"),
    ALL_TIME("All Time")
}

data class BudgetProgressState(
    val category: String,
    val spent: Double,
    val limit: Double,
    val colorHex: String
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    val transactions: StateFlow<List<TransactionEntity>>
    val accounts: StateFlow<List<AccountEntity>>
    val recurringRules: StateFlow<List<RecurringRuleEntity>>
    val budgets: StateFlow<List<CategoryBudgetEntity>>
    val automationRules: StateFlow<List<AutomationRuleEntity>>

    private val _selectedTimeframe = MutableStateFlow(TimeframeFilter.THIS_MONTH)
    val selectedTimeframe: StateFlow<TimeframeFilter> = _selectedTimeframe.asStateFlow()

    private val _lastAutoProcessedMessage = MutableStateFlow<String?>(null)
    val lastAutoProcessedMessage: StateFlow<String?> = _lastAutoProcessedMessage.asStateFlow()

    private val _isProcessingSync = MutableStateFlow(false)
    val isProcessingSync: StateFlow<Boolean> = _isProcessingSync.asStateFlow()

    // Settings State
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _currencySymbol = MutableStateFlow("₱")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _autoCheckOnStartup = MutableStateFlow(true)
    val autoCheckOnStartup: StateFlow<Boolean> = _autoCheckOnStartup.asStateFlow()

    // Security & App Lock State
    private val securityManager = SecurityManager(application)

    private val _lockType = MutableStateFlow(securityManager.getLockType())
    val lockType: StateFlow<SecurityLockType> = _lockType.asStateFlow()

    private val _isAppLocked = MutableStateFlow(securityManager.hasCredentialSet())
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _biometricLockEnabled = MutableStateFlow(securityManager.isBiometricEnabled())
    val biometricLockEnabled: StateFlow<Boolean> = _biometricLockEnabled.asStateFlow()

    private val _autoLockOnResume = MutableStateFlow(securityManager.isAutoLockOnResume())
    val autoLockOnResume: StateFlow<Boolean> = _autoLockOnResume.asStateFlow()

    private val _decimalPrecision = MutableStateFlow(true)
    val decimalPrecision: StateFlow<Boolean> = _decimalPrecision.asStateFlow()

    private val _autoHideSystemBars = MutableStateFlow(true)
    val autoHideSystemBars: StateFlow<Boolean> = _autoHideSystemBars.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = FinanceRepository(db.financeDao())

        transactions = repository.allTransactions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        accounts = repository.allAccounts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        recurringRules = repository.allRecurringRules.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        budgets = repository.allBudgets.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        automationRules = repository.allAutomationRules.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Automatically verify and process any due recurring bills at startup
        viewModelScope.launch {
            processDueBills()
        }
    }

    // Filtered transactions by current timeframe
    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(transactions, _selectedTimeframe) { txList, filter ->
            val calendar = Calendar.getInstance()
            val now = System.currentTimeMillis()

            when (filter) {
                TimeframeFilter.THIS_MONTH -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    val startOfMonth = calendar.timeInMillis
                    txList.filter { it.timestamp >= startOfMonth }
                }
                TimeframeFilter.THIS_WEEK -> {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    val startOfWeek = calendar.timeInMillis
                    txList.filter { it.timestamp >= startOfWeek }
                }
                TimeframeFilter.LAST_MONTH -> {
                    calendar.add(Calendar.MONTH, -1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val startLastMonth = calendar.timeInMillis
                    calendar.add(Calendar.MONTH, 1)
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val endLastMonth = calendar.timeInMillis
                    txList.filter { it.timestamp in startLastMonth until endLastMonth }
                }
                TimeframeFilter.ALL_TIME -> txList
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics
    val netWorth: StateFlow<Double> = accounts.combine(transactions) { accList, _ ->
        accList.sumOf { it.balance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentIncome: StateFlow<Double> = filteredTransactions.combine(_selectedTimeframe) { txList, _ ->
        txList.filter { it.type == "INCOME" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val currentExpense: StateFlow<Double> = filteredTransactions.combine(_selectedTimeframe) { txList, _ ->
        txList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val savingsRate: StateFlow<Int> = combine(currentIncome, currentExpense) { income, expense ->
        if (income > 0) {
            val saved = income - expense
            val rate = (saved / income * 100).toInt()
            rate.coerceIn(0, 100)
        } else 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Visual Charts Breakdown
    val categorySpendSlices: StateFlow<List<ChartSlice>> = filteredTransactions.combine(_selectedTimeframe) { txList, _ ->
        val expenseTx = txList.filter { it.type == "EXPENSE" }
        val grouped = expenseTx.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        grouped.map { (cat, amt) ->
            ChartSlice(
                label = cat,
                value = amt,
                color = getCategoryColor(cat)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Monthly Comparison Data (Past 5 Months)
    val monthlyComparisonData: StateFlow<List<BarComparisonGroup>> = transactions.combine(_selectedTimeframe) { txList, _ ->
        val result = mutableListOf<BarComparisonGroup>()
        val cal = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        // Last 4 months + current month
        for (i in 4 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -i)
            c.set(Calendar.DAY_OF_MONTH, 1)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            val startMs = c.timeInMillis

            c.add(Calendar.MONTH, 1)
            val endMs = c.timeInMillis

            val monthLabel = monthFormat.format(Date(startMs))
            val monthTx = txList.filter { it.timestamp in startMs until endMs }
            val inc = monthTx.filter { it.type == "INCOME" }.sumOf { it.amount }
            val exp = monthTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }

            result.add(BarComparisonGroup(label = monthLabel, income = inc, expense = exp))
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cash Flow Timeline (Past 6 Data Points)
    val cashFlowTrendData: StateFlow<List<Pair<String, Double>>> = transactions.combine(accounts) { txList, accList ->
        val totalCurrentBal = accList.sumOf { it.balance }
        val points = mutableListOf<Pair<String, Double>>()
        val dayFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val oneDay = 86_400_000L
        val now = System.currentTimeMillis()

        // Generate 6 sample timeline points representing net balance progression
        val daysBack = listOf(25, 20, 15, 10, 5, 0)
        daysBack.forEach { days ->
            val timestamp = now - (days * oneDay)
            val label = dayFormat.format(Date(timestamp))
            // Approximate historical balance
            val futureTransactions = txList.filter { it.timestamp > timestamp }
            val diff = futureTransactions.sumOf {
                if (it.type == "INCOME") -it.amount else it.amount
            }
            points.add(label to (totalCurrentBal + diff).coerceAtLeast(1000.0))
        }
        points
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 7-Day Daily Spending for Home Dashboard Activity Pulse
    val weeklyDailySpending: StateFlow<List<DailySpendItem>> = transactions.combine(_currencySymbol) { txList, _ ->
        val result = mutableListOf<DailySpendItem>()
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
        val todayCalendar = Calendar.getInstance()

        // Generate past 7 days ending with today
        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            c.set(Calendar.HOUR_OF_DAY, 0)
            c.set(Calendar.MINUTE, 0)
            c.set(Calendar.SECOND, 0)
            c.set(Calendar.MILLISECOND, 0)
            val startOfDay = c.timeInMillis
            val endOfDay = startOfDay + 86_400_000L

            val isToday = (c.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) &&
                    c.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR))

            val daySpend = txList.filter {
                it.type == "EXPENSE" && it.timestamp in startOfDay until endOfDay
            }.sumOf { it.amount }

            result.add(
                DailySpendItem(
                    dayLabel = if (isToday) "Today" else dayNameFormat.format(Date(startOfDay)),
                    dateLabel = dateFormat.format(Date(startOfDay)),
                    amount = daySpend,
                    isToday = isToday
                )
            )
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Budget progress list
    val budgetProgressList: StateFlow<List<BudgetProgressState>> =
        combine(budgets, filteredTransactions) { budgetList, txList ->
            val expenseMap = txList.filter { it.type == "EXPENSE" }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { tx -> tx.amount } }

            budgetList.map { b ->
                val spent = expenseMap[b.category] ?: 0.0
                BudgetProgressState(
                    category = b.category,
                    spent = spent,
                    limit = b.monthlyLimit,
                    colorHex = b.colorHex
                )
            }.sortedByDescending { it.spent / it.limit }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTimeframe(filter: TimeframeFilter) {
        _selectedTimeframe.value = filter
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountId: String,
        note: String = "",
        isAutomated: Boolean = false,
        sourceSnippet: String? = null
    ) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                accountId = accountId,
                timestamp = System.currentTimeMillis(),
                note = note,
                isAutomated = isAutomated,
                sourceAlertSnippet = sourceSnippet
            )
            repository.insertTransaction(tx)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun processDueBills() {
        viewModelScope.launch {
            _isProcessingSync.value = true
            val count = repository.processDueRecurringRules()
            _isProcessingSync.value = false
            if (count > 0) {
                _lastAutoProcessedMessage.value = "Automated Expense Tracker logged $count scheduled transaction${if (count > 1) "s" else ""}."
            } else {
                _lastAutoProcessedMessage.value = "All recurring schedules are up to date."
            }
        }
    }

    suspend fun parseAlert(rawText: String): ParsedAlertResult {
        return repository.parseBankAlert(rawText)
    }

    fun saveParsedAlert(result: ParsedAlertResult) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                title = result.title,
                amount = result.amount,
                type = result.type,
                category = result.category,
                accountId = result.accountId,
                timestamp = System.currentTimeMillis(),
                note = "Auto-parsed from bank alert",
                isAutomated = true,
                sourceAlertSnippet = result.originalText
            )
            repository.insertTransaction(tx)
            _lastAutoProcessedMessage.value = "Auto-tracked '$${String.format(Locale.US, "%.2f", result.amount)} at ${result.title}' via smart alert ingestion."
        }
    }

    fun addRecurringRule(
        title: String,
        amount: Double,
        type: String,
        category: String,
        accountId: String,
        frequency: String,
        dayOfMonth: Int
    ) {
        viewModelScope.launch {
            val rule = RecurringRuleEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                accountId = accountId,
                frequency = frequency,
                dayOfMonth = dayOfMonth,
                nextDueDate = System.currentTimeMillis() + 86_400_000L * 7,
                isActive = true,
                autoProcess = true
            )
            repository.insertRecurringRule(rule)
        }
    }

    fun toggleRecurringRule(rule: RecurringRuleEntity) {
        viewModelScope.launch {
            repository.updateRecurringRule(rule.copy(isActive = !rule.isActive))
        }
    }

    fun deleteRecurringRule(id: Long) {
        viewModelScope.launch {
            repository.deleteRecurringRule(id)
        }
    }

    fun addAutomationRule(keyword: String, category: String, accountId: String) {
        viewModelScope.launch {
            val rule = AutomationRuleEntity(
                keyword = keyword.trim(),
                targetCategory = category,
                targetAccountId = accountId,
                isEnabled = true
            )
            repository.insertAutomationRule(rule)
        }
    }

    fun deleteAutomationRule(id: Long) {
        viewModelScope.launch {
            repository.deleteAutomationRule(id)
        }
    }

    fun updateBudgetLimit(category: String, newLimit: Double) {
        viewModelScope.launch {
            val current = budgets.value.find { it.category == category }
            if (current != null) {
                repository.updateBudget(current.copy(monthlyLimit = newLimit))
            } else {
                repository.insertBudget(CategoryBudgetEntity(category = category, monthlyLimit = newLimit))
            }
        }
    }

    fun clearAutoProcessedMessage() {
        _lastAutoProcessedMessage.value = null
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setCurrency(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun toggleAutoCheckOnStartup(enabled: Boolean) {
        _autoCheckOnStartup.value = enabled
    }

    fun toggleBiometricLock(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
        _biometricLockEnabled.value = enabled
    }

    fun toggleAutoLockOnResume(enabled: Boolean) {
        securityManager.setAutoLockOnResume(enabled)
        _autoLockOnResume.value = enabled
    }

    fun setPin(currentCred: String?, newPin: String): Boolean {
        if (securityManager.hasCredentialSet()) {
            if (currentCred == null || !securityManager.verifyCredential(currentCred)) {
                return false
            }
        }
        val success = securityManager.setPin(newPin)
        if (success) {
            _lockType.value = SecurityLockType.PIN
            _isAppLocked.value = false
        }
        return success
    }

    fun setPassword(currentCred: String?, newPassword: String): Boolean {
        if (securityManager.hasCredentialSet()) {
            if (currentCred == null || !securityManager.verifyCredential(currentCred)) {
                return false
            }
        }
        val success = securityManager.setPassword(newPassword)
        if (success) {
            _lockType.value = SecurityLockType.PASSWORD
            _isAppLocked.value = false
        }
        return success
    }

    fun removeLock(currentCred: String): Boolean {
        if (!securityManager.verifyCredential(currentCred)) {
            return false
        }
        securityManager.removeLock()
        _lockType.value = SecurityLockType.NONE
        _isAppLocked.value = false
        return true
    }

    fun verifyAndUnlock(input: String): Boolean {
        val matches = securityManager.verifyCredential(input)
        if (matches) {
            _isAppLocked.value = false
        }
        return matches
    }

    fun unlockWithBiometrics() {
        if (securityManager.isBiometricEnabled()) {
            _isAppLocked.value = false
        }
    }

    fun lockApp() {
        if (securityManager.hasCredentialSet()) {
            _isAppLocked.value = true
        }
    }

    fun toggleDecimalPrecision(enabled: Boolean) {
        _decimalPrecision.value = enabled
    }

    fun toggleAutoHideSystemBars(enabled: Boolean) {
        _autoHideSystemBars.value = enabled
    }

    fun resetAllDataToDefault() {
        viewModelScope.launch {
            repository.resetToSampleData()
            _lastAutoProcessedMessage.value = "Sample data restored successfully."
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.clearAllData()
            _lastAutoProcessedMessage.value = "All transactions cleared."
        }
    }
}
