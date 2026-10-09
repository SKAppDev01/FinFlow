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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AccountEntity
import com.example.data.model.AutomationRuleEntity
import com.example.data.model.RecurringRuleEntity
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryEmerald
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AutomationsScreen(
    recurringRules: List<RecurringRuleEntity>,
    automationRules: List<AutomationRuleEntity>,
    accounts: List<AccountEntity>,
    isProcessingSync: Boolean,
    lastProcessMessage: String?,
    onTriggerAutoProcess: () -> Unit,
    onOpenAlertSimulator: () -> Unit,
    onToggleRecurringRule: (RecurringRuleEntity) -> Unit,
    onDeleteRecurringRule: (Long) -> Unit,
    onAddRecurringRule: (title: String, amount: Double, type: String, category: String, accountId: String, frequency: String, dayOfMonth: Int) -> Unit,
    onAddAutomationRule: (keyword: String, category: String, accountId: String) -> Unit,
    onDeleteAutomationRule: (Long) -> Unit,
    onClearStatusMessage: () -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    var showAddRecurringDialog by remember { mutableStateOf(false) }
    var showAddRuleDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("automations_screen"),
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
                    text = "Automated Expense Tracking",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Rules, recurring schedules, and smart alert ingestion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Auto-Tracker Status & Quick Action Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryEmerald.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PrimaryEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Auto-Tracking Engine",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${recurringRules.count { it.isActive }} active schedules • ${automationRules.size} smart rules",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IncomeGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(IncomeGreen, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (lastProcessMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = lastProcessMessage,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = onClearStatusMessage, modifier = Modifier.size(20.dp)) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onTriggerAutoProcess,
                            enabled = !isProcessingSync,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("run_auto_check_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                        ) {
                            if (isProcessingSync) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Run Check Now", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }

                        Button(
                            onClick = onOpenAlertSimulator,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("simulate_alert_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate SMS", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Recurring Automated Schedules
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recurring Bills & Schedules (${recurringRules.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryEmerald.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showAddRecurringDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = PrimaryEmerald, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Bill", style = MaterialTheme.typography.labelSmall, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(recurringRules) { rule ->
            val isIncome = rule.type == "INCOME"
            val categoryColor = getCategoryColor(rule.category)
            val dueDateStr = dateFormat.format(Date(rule.nextDueDate))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 5.dp)
                    .testTag("recurring_item_${rule.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(rule.category),
                            contentDescription = rule.category,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rule.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = rule.frequency,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            Text(
                                text = "Next: $dueDateStr",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isIncome) "+$currencySymbol${String.format(Locale.US, "%.2f", rule.amount)}"
                            else "-$currencySymbol${String.format(Locale.US, "%.2f", rule.amount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isIncome) IncomeGreen else ExpenseCoral
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = rule.isActive,
                                onCheckedChange = { onToggleRecurringRule(rule) },
                                modifier = Modifier.size(36.dp),
                                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryEmerald, checkedTrackColor = PrimaryEmerald.copy(alpha = 0.4f))
                            )
                            IconButton(onClick = { onDeleteRecurringRule(rule.id) }, modifier = Modifier.size(24.dp).padding(start = 4.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Section: Smart Categorization Rules
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smart Categorization Rules (${automationRules.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryEmerald.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showAddRuleDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = PrimaryEmerald, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Add Rule", style = MaterialTheme.typography.labelSmall, color = PrimaryEmerald, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(automationRules) { rule ->
            val catColor = getCategoryColor(rule.targetCategory)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .testTag("rule_item_${rule.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Rule,
                        contentDescription = null,
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "contains \"${rule.keyword}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = catColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = rule.targetCategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = catColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDeleteAutomationRule(rule.id) },
                        modifier = Modifier.size(28.dp).padding(start = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Rule",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }

    // Add Recurring Schedule Dialog
    if (showAddRecurringDialog) {
        AddRecurringDialog(
            accounts = accounts,
            onDismiss = { showAddRecurringDialog = false },
            onSave = { title, amt, type, cat, accId, freq, day ->
                onAddRecurringRule(title, amt, type, cat, accId, freq, day)
                showAddRecurringDialog = false
            }
        )
    }

    // Add Rule Dialog
    if (showAddRuleDialog) {
        AddRuleDialog(
            accounts = accounts,
            onDismiss = { showAddRuleDialog = false },
            onSave = { kw, cat, accId ->
                onAddAutomationRule(kw, cat, accId)
                showAddRuleDialog = false
            }
        )
    }
}

@Composable
private fun AddRecurringDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, type: String, category: String, accountId: String, frequency: String, dayOfMonth: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var category by remember { mutableStateOf("Housing") }
    var frequency by remember { mutableStateOf("MONTHLY") }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "checking") }

    val frequencies = listOf("DAILY", "WEEKLY", "BIWEEKLY", "MONTHLY", "YEARLY")
    val categories = listOf("Housing", "Entertainment", "Utilities", "Health & Wellness", "Food & Dining", "Transportation", "Salary")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Add Recurring Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Name (e.g. Netflix, Rent)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Frequency", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(frequencies) { freq ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (frequency == freq) PrimaryEmerald else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { frequency = freq }
                        ) {
                            Text(
                                text = freq,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (frequency == freq) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Category", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (category == cat) PrimaryEmerald else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (category == cat) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amt > 0) {
                            onSave(title, amt, type, category, accountId, frequency, 1)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                ) {
                    Text("Save Recurring Schedule", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AddRuleDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (keyword: String, category: String, accountId: String) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food & Dining") }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "credit_card") }

    val categories = listOf("Food & Dining", "Transportation", "Shopping", "Entertainment", "Housing", "Utilities", "Salary")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "New Auto-Categorization Rule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "If transaction description contains keyword, auto-assign category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("Keyword (e.g. uber, trader joe, chevron)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Assign to Category:", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (category == cat) PrimaryEmerald else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (category == cat) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (keyword.isNotBlank()) {
                            onSave(keyword.trim().lowercase(Locale.ROOT), category, accountId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                ) {
                    Text("Create Rule", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
