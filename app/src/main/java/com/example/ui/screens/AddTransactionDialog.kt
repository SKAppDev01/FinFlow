package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.AccountEntity
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryEmerald

@Composable
fun AddTransactionDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, type: String, category: String, accountId: String, note: String) -> Unit,
    currencySymbol: String = "$"
) {
    var type by remember { mutableStateOf("EXPENSE") }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food & Dining") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "checking") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val expenseCategories = listOf(
        "Food & Dining", "Transportation", "Shopping",
        "Entertainment", "Housing", "Utilities", "Health & Wellness", "Other"
    )
    val incomeCategories = listOf("Salary", "Investment", "Other")

    val categories = if (type == "EXPENSE") expenseCategories else incomeCategories

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("add_transaction_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Type Toggle (Expense / Income)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (type == "EXPENSE") ExpenseCoral else Color.Transparent)
                            .clickable {
                                type = "EXPENSE"
                                selectedCategory = "Food & Dining"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            fontWeight = FontWeight.Bold,
                            color = if (type == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (type == "INCOME") IncomeGreen else Color.Transparent)
                            .clickable {
                                type = "INCOME"
                                selectedCategory = "Salary"
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            fontWeight = FontWeight.Bold,
                            color = if (type == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($currencySymbol)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("transaction_amount_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Title / Payee
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Merchant / Description") },
                    placeholder = { Text(if (type == "EXPENSE") "e.g. Whole Foods, Uber" else "e.g. Payroll, Dividends") },
                    modifier = Modifier.fillMaxWidth().testTag("transaction_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        val color = getCategoryColor(cat)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedCategory = cat }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(cat),
                                    contentDescription = cat,
                                    tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Account Selection
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(accounts) { acc ->
                        val isSelected = selectedAccountId == acc.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedAccountId = acc.id }
                        ) {
                            Text(
                                text = "${acc.name} (*${acc.lastFour})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) PrimaryEmerald else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = ExpenseCoral,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save button
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            errorMessage = "Please enter a valid amount greater than 0"
                            return@Button
                        }
                        if (title.isBlank()) {
                            errorMessage = "Please enter a description or merchant name"
                            return@Button
                        }
                        onSave(title.trim(), amt, type, selectedCategory, selectedAccountId, note)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_transaction_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "EXPENSE") ExpenseCoral else IncomeGreen
                    )
                ) {
                    Text(
                        text = "Add Transaction",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
