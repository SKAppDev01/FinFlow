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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.AccountSummaryCard
import com.example.ui.components.DailySpendItem
import com.example.ui.components.StatMetricCard
import com.example.ui.components.TransactionRowItem
import com.example.ui.components.WeeklyActivityChart
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryEmerald
import java.util.Locale

@Composable
fun DashboardScreen(
    netWorth: Double,
    totalIncome: Double,
    totalExpense: Double,
    savingsRate: Int,
    accounts: List<AccountEntity>,
    recentTransactions: List<TransactionEntity>,
    weeklySpending: List<DailySpendItem>,
    onAddTransactionClick: () -> Unit,
    onOpenAlertParserClick: () -> Unit,
    onTriggerAutoSync: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onViewAllTransactions: () -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Hero Net Worth & Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Net Worth",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", netWorth)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Auto-sync fast button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onTriggerAutoSync() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Auto-Sync",
                                tint = PrimaryEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Auto-Check",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Accounts Carousel
        item {
            Text(
                text = "Accounts & Wallets",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(accounts) { account ->
                    AccountSummaryCard(account = account, currencySymbol = currencySymbol)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Automated Expense Capture Feature Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("auto_ingestion_banner")
                    .clickable { onOpenAlertParserClick() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PrimaryEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Automated Expense Ingestion",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Paste or simulate real-time bank SMS alerts to auto-categorize expenses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryEmerald
                    ) {
                        Text(
                            text = "Test",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Metrics Grid (Income, Expenses, Savings Rate)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = "Income",
                    value = "$currencySymbol${String.format(Locale.US, "%,.0f", totalIncome)}",
                    subtitle = "This month",
                    icon = Icons.Default.ArrowDownward,
                    accentColor = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Spent",
                    value = "$currencySymbol${String.format(Locale.US, "%,.0f", totalExpense)}",
                    subtitle = "This month",
                    icon = Icons.Default.ArrowUpward,
                    accentColor = ExpenseCoral,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Savings",
                    value = "$savingsRate%",
                    subtitle = "Saved rate",
                    icon = Icons.Default.Savings,
                    accentColor = PrimaryEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 7-Day Spending Activity Chart (Distinct to Home Dashboard)
        if (weeklySpending.isNotEmpty()) {
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
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Spending Activity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Daily Rhythm",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        WeeklyActivityChart(
                            days = weeklySpending,
                            modifier = Modifier.fillMaxWidth(),
                            currencySymbol = currencySymbol
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Recent Transactions Header & List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "View Analytics",
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryEmerald,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onViewAllTransactions() }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions recorded yet. Tap + to add or test automated alerts!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(recentTransactions.take(8)) { transaction ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    TransactionRowItem(
                        transaction = transaction,
                        currencySymbol = currencySymbol,
                        onDelete = { onDeleteTransaction(transaction) }
                    )
                }
            }
        }
    }
}
