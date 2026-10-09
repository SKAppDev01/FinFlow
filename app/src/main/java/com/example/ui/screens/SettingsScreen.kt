package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.SecurityLockType
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    currencySymbol: String,
    onCurrencySelected: (String) -> Unit,
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    autoCheckOnStartup: Boolean,
    onToggleAutoCheck: (Boolean) -> Unit,
    biometricLockEnabled: Boolean,
    onToggleBiometricLock: (Boolean) -> Unit,
    lockType: SecurityLockType,
    autoLockOnResume: Boolean,
    onToggleAutoLockOnResume: (Boolean) -> Unit,
    onSetPin: (currentCred: String?, newPin: String) -> Boolean,
    onSetPassword: (currentCred: String?, newPassword: String) -> Boolean,
    onRemoveLock: (currentCred: String) -> Boolean,
    onLockAppNow: () -> Unit,
    decimalPrecision: Boolean,
    onToggleDecimalPrecision: (Boolean) -> Unit,
    autoHideSystemBars: Boolean = true,
    onToggleAutoHideSystemBars: (Boolean) -> Unit = {},
    onResetSampleData: () -> Unit,
    onClearAllTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showSecurityLockDialog by remember { mutableStateOf(false) }

    val currencyList = listOf(
        "₱" to "PHP (₱)",
        "$" to "USD ($)",
        "€" to "EUR (€)",
        "£" to "GBP (£)",
        "¥" to "JPY (¥)",
        "C$" to "CAD (C$)",
        "A$" to "AUD (A$)"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Settings & Preferences",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Customize currency, automation, security, and data storage",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section: Appearance & Theme Mode (Light / Dark / System)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("theme_mode_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        icon = Icons.Default.Palette,
                        title = "Appearance & Theme"
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Choose your preferred display mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThemeOptionTile(
                            title = "System",
                            subtitle = "Auto theme",
                            icon = Icons.Default.PhoneAndroid,
                            selected = themeMode == ThemeMode.SYSTEM,
                            onClick = { onThemeModeSelected(ThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f),
                            testTag = "theme_mode_system"
                        )

                        ThemeOptionTile(
                            title = "Light",
                            subtitle = "Bright look",
                            icon = Icons.Default.LightMode,
                            selected = themeMode == ThemeMode.LIGHT,
                            onClick = { onThemeModeSelected(ThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f),
                            testTag = "theme_mode_light"
                        )

                        ThemeOptionTile(
                            title = "Dark",
                            subtitle = "OLED dark",
                            icon = Icons.Default.DarkMode,
                            selected = themeMode == ThemeMode.DARK,
                            onClick = { onThemeModeSelected(ThemeMode.DARK) },
                            modifier = Modifier.weight(1f),
                            testTag = "theme_mode_dark"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Section: Currency & Display Preferences
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        icon = Icons.Default.MonetizationOn,
                        title = "Currency & Display"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Primary Currency Symbol",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(currencyList) { (sym, label) ->
                            val isSelected = currencySymbol == sym
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryEmerald else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onCurrencySelected(sym) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsToggleRow(
                        title = "Show Cents & Decimal Precision",
                        subtitle = "Display .00 in amounts across all balance cards",
                        checked = decimalPrecision,
                        onCheckedChange = onToggleDecimalPrecision
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsToggleRow(
                        title = "Auto-Hide Status & Gesture Bar",
                        subtitle = "Automatically hide status bar & navigation gesture button (swipe to reveal)",
                        checked = autoHideSystemBars,
                        onCheckedChange = onToggleAutoHideSystemBars
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Section: Automation Preferences
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        icon = Icons.Default.AutoAwesome,
                        title = "Automated Expense Tracking"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsToggleRow(
                        title = "Auto-Check Recurring Bills on App Launch",
                        subtitle = "Automatically log due bills when opening FinFlow",
                        checked = autoCheckOnStartup,
                        onCheckedChange = onToggleAutoCheck
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsToggleRow(
                        title = "Smart Bank Alert Categorization",
                        subtitle = "Apply active keyword rules when parsing notifications",
                        checked = true,
                        onCheckedChange = { /* Always enabled */ },
                        enabled = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Section: Privacy & Security
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        icon = Icons.Default.Security,
                        title = "Security & Privacy"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lock Status Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = when (lockType) {
                            SecurityLockType.NONE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            SecurityLockType.PIN -> PrimaryEmerald.copy(alpha = 0.10f)
                            SecurityLockType.PASSWORD -> PrimaryEmerald.copy(alpha = 0.10f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (lockType == SecurityLockType.NONE) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            else PrimaryEmerald.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (lockType == SecurityLockType.NONE) MaterialTheme.colorScheme.surfaceVariant else PrimaryEmerald,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (lockType) {
                                                SecurityLockType.PIN -> Icons.Default.Pin
                                                SecurityLockType.PASSWORD -> Icons.Default.Key
                                                SecurityLockType.NONE -> Icons.Default.LockOpen
                                            },
                                            contentDescription = null,
                                            tint = if (lockType == SecurityLockType.NONE) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when (lockType) {
                                            SecurityLockType.PIN -> "4-Digit PIN Lock (Active)"
                                            SecurityLockType.PASSWORD -> "Master Password Lock (Active)"
                                            SecurityLockType.NONE -> "App Lock: Disabled"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = when (lockType) {
                                            SecurityLockType.PIN -> "Protected with numeric PIN"
                                            SecurityLockType.PASSWORD -> "Protected with master password"
                                            SecurityLockType.NONE -> "Add a PIN or Password to protect your finances"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showSecurityLockDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryEmerald
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("configure_app_lock_button")
                                ) {
                                    Text(
                                        text = if (lockType == SecurityLockType.NONE) "Set PIN / Password" else "Change Lock",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                if (lockType != SecurityLockType.NONE) {
                                    OutlinedButton(
                                        onClick = onLockAppNow,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("lock_app_now_button")
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Lock Now", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsToggleRow(
                        title = "Require Biometric / Fingerprint",
                        subtitle = "Fast fingerprint unlock alongside PIN or Password",
                        checked = biometricLockEnabled,
                        onCheckedChange = onToggleBiometricLock
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SettingsToggleRow(
                        title = "Auto-Lock on App Resume",
                        subtitle = "Prompt for PIN or Password when returning to FinFlow",
                        checked = autoLockOnResume,
                        onCheckedChange = onToggleAutoLockOnResume,
                        enabled = lockType != SecurityLockType.NONE
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Local privacy badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IncomeGreen.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = IncomeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "100% On-Device Data Privacy",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                                Text(
                                    text = "All accounts, transactions, and rules reside securely in your phone's Room database. Zero external trackers.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Section: Data & Backup Management
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader(
                        icon = Icons.Default.Tune,
                        title = "Data Management"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Reset / Restore Sample Data
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reload Sample Data",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Repopulate full demo accounts, budgets, and transactions",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showResetConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("reload_sample_data_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clear All Transactions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clear All Transactions",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ExpenseCoral
                            )
                            Text(
                                text = "Wipe transaction logs to start fresh with zero records",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = { showClearConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseCoral),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("clear_transactions_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Section: About App
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "FinFlow v1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Personal finance manager with automated tracking & visual analytics",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Confirmation Dialogs
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reload Sample Data?") },
            text = { Text("This will reset all accounts, categories, budgets, and transactions back to the default demo state.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetSampleData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                ) {
                    Text("Confirm Reload")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Transactions?") },
            text = { Text("This action cannot be undone. All recorded transactions will be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllTransactions()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseCoral)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSecurityLockDialog) {
        ConfigureSecurityLockDialog(
            currentLockType = lockType,
            onDismiss = { showSecurityLockDialog = false },
            onSavePin = onSetPin,
            onSavePassword = onSetPassword,
            onRemoveLock = onRemoveLock
        )
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(PrimaryEmerald.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryEmerald,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryEmerald,
                checkedTrackColor = PrimaryEmerald.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
private fun ThemeOptionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) PrimaryEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = if (selected) BorderStroke(2.dp, PrimaryEmerald) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 14.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (selected) PrimaryEmerald else MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) PrimaryEmerald else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

