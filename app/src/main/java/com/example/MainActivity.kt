package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.security.SecurityLockType
import com.example.ui.screens.AddTransactionDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AutomationsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.QuickAlertParserDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.FinFlowTheme
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Home", Icons.Default.Home, "tab_dashboard"),
    ANALYTICS("Analytics", Icons.Default.BarChart, "tab_analytics"),
    BUDGETS("Budgets", Icons.Default.AccountBalanceWallet, "tab_budgets"),
    AUTOMATIONS("Automate", Icons.Default.AutoAwesome, "tab_automations"),
    SETTINGS("Settings", Icons.Default.Settings, "tab_settings")
}

class MainActivity : FragmentActivity() {
    var autoHideBarsEnabled: Boolean = true
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                } else {
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
        }
        hideSystemBars()
        setContent {
            FinFlowRoot()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && autoHideBarsEnabled) {
            hideSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        if (autoHideBarsEnabled) {
            hideSystemBars()
        }
    }

    fun setAutoHideBars(enable: Boolean) {
        autoHideBarsEnabled = enable
        if (enable) {
            hideSystemBars()
        } else {
            showSystemBars()
        }
    }

    fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    fun showSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }

    fun launchBiometricPrompt(
        title: String = "FinFlow Protected",
        subtitle: String = "Verify your biometric identity to unlock",
        negativeButtonText: String = "Use PIN / Password",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        com.example.data.security.BiometricAuthManager.promptBiometricAuthentication(
            activity = this,
            title = title,
            subtitle = subtitle,
            negativeButtonText = negativeButtonText,
            onSuccess = onSuccess,
            onError = onError
        )
    }
}

@Composable
fun FinFlowRoot(viewModel: FinanceViewModel = viewModel()) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val autoHideBars by viewModel.autoHideSystemBars.collectAsStateWithLifecycle()
    val respectPunchHole by viewModel.respectPunchHole.collectAsStateWithLifecycle()
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    DisposableEffect(autoHideBars) {
        val activity = context as? MainActivity
        activity?.setAutoHideBars(autoHideBars)
        onDispose { }
    }

    FinFlowTheme(darkTheme = isDark) {
        FinFlowMainApp(
            viewModel = viewModel,
            themeMode = themeMode,
            autoHideBars = autoHideBars,
            respectPunchHole = respectPunchHole
        )
    }
}

@Composable
fun FinFlowMainApp(
    viewModel: FinanceViewModel = viewModel(),
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    autoHideBars: Boolean = true,
    respectPunchHole: Boolean = true
) {
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAlertParserDialog by remember { mutableStateOf(false) }

    // Collect Reactive State
    val netWorth by viewModel.netWorth.collectAsStateWithLifecycle()
    val totalIncome by viewModel.currentIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.currentExpense.collectAsStateWithLifecycle()
    val savingsRate by viewModel.savingsRate.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.transactions.collectAsStateWithLifecycle()
    val weeklySpending by viewModel.weeklyDailySpending.collectAsStateWithLifecycle()
    val categorySlices by viewModel.categorySpendSlices.collectAsStateWithLifecycle()
    val monthlyComparison by viewModel.monthlyComparisonData.collectAsStateWithLifecycle()
    val cashFlowTrend by viewModel.cashFlowTrendData.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val budgetProgressList by viewModel.budgetProgressList.collectAsStateWithLifecycle()
    val recurringRules by viewModel.recurringRules.collectAsStateWithLifecycle()
    val automationRules by viewModel.automationRules.collectAsStateWithLifecycle()
    val isProcessingSync by viewModel.isProcessingSync.collectAsStateWithLifecycle()
    val lastProcessMessage by viewModel.lastAutoProcessedMessage.collectAsStateWithLifecycle()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val autoCheckOnStartup by viewModel.autoCheckOnStartup.collectAsStateWithLifecycle()
    val biometricLockEnabled by viewModel.biometricLockEnabled.collectAsStateWithLifecycle()
    val lockType by viewModel.lockType.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val autoLockOnResume by viewModel.autoLockOnResume.collectAsStateWithLifecycle()
    val decimalPrecision by viewModel.decimalPrecision.collectAsStateWithLifecycle()

    // Observe app lifecycle for auto-lock on pause/stop
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, autoLockOnResume, lockType) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && autoLockOnResume && lockType != SecurityLockType.NONE) {
                viewModel.lockApp()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Proper back navigation: if on secondary tab, back button returns to Dashboard
    BackHandler(enabled = currentTab != AppTab.DASHBOARD && !isAppLocked) {
        currentTab = AppTab.DASHBOARD
    }

    val context = LocalContext.current
    val activity = context as? MainActivity

    if (isAppLocked && lockType != SecurityLockType.NONE) {
        LockScreen(
            lockType = lockType,
            biometricEnabled = biometricLockEnabled,
            onAttemptUnlock = { credential ->
                viewModel.verifyAndUnlock(credential)
            },
            onBiometricUnlock = { onError ->
                activity?.launchBiometricPrompt(
                    title = "FinFlow Protected",
                    subtitle = when (lockType) {
                        SecurityLockType.PIN -> "Touch sensor or use 4-digit PIN"
                        SecurityLockType.PASSWORD -> "Touch sensor or use Master Password"
                        else -> "Verify biometric identity"
                    },
                    negativeButtonText = if (lockType == SecurityLockType.PIN) "Use PIN" else "Use Password",
                    onSuccess = {
                        viewModel.unlockWithBiometrics()
                    },
                    onError = { err ->
                        onError(err)
                    }
                ) ?: onError("Biometric prompt unavailable")
            }
        )
    } else {
        Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = if (respectPunchHole) WindowInsets.safeDrawing else WindowInsets.systemBars,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppTab.values().forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryEmerald,
                            selectedTextColor = PrimaryEmerald,
                            indicatorColor = PrimaryEmerald.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentTab != AppTab.SETTINGS) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = PrimaryEmerald,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    modifier = Modifier.testTag("main_add_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()
        val safeDrawingInsets = WindowInsets.safeDrawing.asPaddingValues()

        val topPadding = if (respectPunchHole) {
            val measuredTop = maxOf(
                innerPadding.calculateTopPadding(),
                cutoutInsets.calculateTopPadding(),
                safeDrawingInsets.calculateTopPadding()
            )
            if (autoHideBars) {
                maxOf(measuredTop, 32.dp)
            } else {
                measuredTop
            }
        } else {
            innerPadding.calculateTopPadding()
        }

        val leftPadding = if (respectPunchHole) {
            maxOf(innerPadding.calculateLeftPadding(layoutDirection), cutoutInsets.calculateLeftPadding(layoutDirection))
        } else {
            innerPadding.calculateLeftPadding(layoutDirection)
        }

        val rightPadding = if (respectPunchHole) {
            maxOf(innerPadding.calculateRightPadding(layoutDirection), cutoutInsets.calculateRightPadding(layoutDirection))
        } else {
            innerPadding.calculateRightPadding(layoutDirection)
        }

        val bottomPadding = innerPadding.calculateBottomPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = leftPadding,
                    top = topPadding,
                    end = rightPadding,
                    bottom = bottomPadding
                )
        ) {
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    AppTab.DASHBOARD -> DashboardScreen(
                        netWorth = netWorth,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        savingsRate = savingsRate,
                        accounts = accounts,
                        recentTransactions = recentTransactions,
                        weeklySpending = weeklySpending,
                        onAddTransactionClick = { showAddDialog = true },
                        onOpenAlertParserClick = { showAlertParserDialog = true },
                        onTriggerAutoSync = { viewModel.processDueBills() },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) },
                        onViewAllTransactions = { currentTab = AppTab.ANALYTICS },
                        currencySymbol = currencySymbol
                    )

                    AppTab.ANALYTICS -> AnalyticsScreen(
                        selectedTimeframe = selectedTimeframe,
                        onTimeframeSelected = { viewModel.setTimeframe(it) },
                        categorySlices = categorySlices,
                        monthlyComparison = monthlyComparison,
                        cashFlowTrend = cashFlowTrend,
                        transactions = filteredTransactions,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        onDeleteTransaction = { viewModel.deleteTransaction(it) },
                        currencySymbol = currencySymbol
                    )

                    AppTab.BUDGETS -> BudgetsScreen(
                        budgetProgressList = budgetProgressList,
                        onUpdateBudget = { category, newLimit ->
                            viewModel.updateBudgetLimit(category, newLimit)
                        },
                        currencySymbol = currencySymbol
                    )

                    AppTab.AUTOMATIONS -> AutomationsScreen(
                        recurringRules = recurringRules,
                        automationRules = automationRules,
                        accounts = accounts,
                        isProcessingSync = isProcessingSync,
                        lastProcessMessage = lastProcessMessage,
                        onTriggerAutoProcess = { viewModel.processDueBills() },
                        onOpenAlertSimulator = { showAlertParserDialog = true },
                        onToggleRecurringRule = { viewModel.toggleRecurringRule(it) },
                        onDeleteRecurringRule = { viewModel.deleteRecurringRule(it) },
                        onAddRecurringRule = { title, amt, type, cat, accId, freq, day ->
                            viewModel.addRecurringRule(title, amt, type, cat, accId, freq, day)
                        },
                        onAddAutomationRule = { kw, cat, accId ->
                            viewModel.addAutomationRule(kw, cat, accId)
                        },
                        onDeleteAutomationRule = { viewModel.deleteAutomationRule(it) },
                        onClearStatusMessage = { viewModel.clearAutoProcessedMessage() },
                        currencySymbol = currencySymbol
                    )

                    AppTab.SETTINGS -> SettingsScreen(
                        currencySymbol = currencySymbol,
                        onCurrencySelected = { viewModel.setCurrency(it) },
                        themeMode = themeMode,
                        onThemeModeSelected = { viewModel.setThemeMode(it) },
                        autoCheckOnStartup = autoCheckOnStartup,
                        onToggleAutoCheck = { viewModel.toggleAutoCheckOnStartup(it) },
                        biometricLockEnabled = biometricLockEnabled,
                        onToggleBiometricLock = { viewModel.toggleBiometricLock(it) },
                        lockType = lockType,
                        autoLockOnResume = autoLockOnResume,
                        onToggleAutoLockOnResume = { viewModel.toggleAutoLockOnResume(it) },
                        onSetPin = { currentCred, newPin -> viewModel.setPin(currentCred, newPin) },
                        onSetPassword = { currentCred, newPassword -> viewModel.setPassword(currentCred, newPassword) },
                        onRemoveLock = { currentCred -> viewModel.removeLock(currentCred) },
                        onLockAppNow = { viewModel.lockApp() },
                        decimalPrecision = decimalPrecision,
                        onToggleDecimalPrecision = { viewModel.toggleDecimalPrecision(it) },
                        autoHideSystemBars = autoHideBars,
                        onToggleAutoHideSystemBars = { viewModel.toggleAutoHideSystemBars(it) },
                        respectPunchHole = respectPunchHole,
                        onToggleRespectPunchHole = { viewModel.toggleRespectPunchHole(it) },
                        onClearAllData = { viewModel.clearAllData() },
                        onClearAllTransactions = { viewModel.clearAllTransactions() }
                    )
                }
            }
        }
    }
    }

    // Add Transaction Dialog
    if (showAddDialog) {
        AddTransactionDialog(
            accounts = accounts,
            onDismiss = {
                showAddDialog = false
                if (autoHideBars) {
                    (context as? MainActivity)?.hideSystemBars()
                }
            },
            onSave = { title, amount, type, category, accountId, note ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    accountId = accountId,
                    note = note
                )
                if (autoHideBars) {
                    (context as? MainActivity)?.hideSystemBars()
                }
            },
            currencySymbol = currencySymbol
        )
    }

    // Quick Alert Parser Simulator Dialog
    if (showAlertParserDialog) {
        QuickAlertParserDialog(
            onDismiss = {
                showAlertParserDialog = false
                if (autoHideBars) {
                    (context as? MainActivity)?.hideSystemBars()
                }
            },
            onParseText = { rawText ->
                viewModel.parseAlert(rawText)
            },
            onConfirmSave = { result ->
                viewModel.saveParsedAlert(result)
                if (autoHideBars) {
                    (context as? MainActivity)?.hideSystemBars()
                }
            },
            currencySymbol = currencySymbol
        )
    }
}
