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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionEntity
import com.example.ui.components.BarComparisonGroup
import com.example.ui.components.CashFlowLineChart
import com.example.ui.components.ChartSlice
import com.example.ui.components.DonutSpendingChart
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.components.TransactionRowItem
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.viewmodel.TimeframeFilter
import java.util.Locale

@Composable
fun AnalyticsScreen(
    selectedTimeframe: TimeframeFilter,
    onTimeframeSelected: (TimeframeFilter) -> Unit,
    categorySlices: List<ChartSlice>,
    monthlyComparison: List<BarComparisonGroup>,
    cashFlowTrend: List<Pair<String, Double>>,
    transactions: List<TransactionEntity>,
    totalIncome: Double,
    totalExpense: Double,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val filteredList = remember(transactions, searchQuery, selectedCategoryFilter) {
        transactions.filter { tx ->
            val matchesSearch = searchQuery.isBlank() ||
                    tx.title.contains(searchQuery, ignoreCase = true) ||
                    tx.category.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == null || tx.category == selectedCategoryFilter
            matchesSearch && matchesCategory
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Title & Timeframe Selector
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Financial Analytics & Charts",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Intuitive visualizations for spending patterns & cash flow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Timeframe Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(TimeframeFilter.values()) { filter ->
                        val isSelected = filter == selectedTimeframe
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) PrimaryEmerald else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onTimeframeSelected(filter) }
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Period Income",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+$currencySymbol${String.format(Locale.US, "%,.2f", totalIncome)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Period Expenses",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "-$currencySymbol${String.format(Locale.US, "%,.2f", totalExpense)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseCoral
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Net Savings",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val net = totalIncome - totalExpense
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", net)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (net >= 0) PrimaryEmerald else ExpenseCoral
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Chart 1: Interactive Donut Spending Breakdown
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
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category Spending Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap category to filter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (categorySlices.isEmpty()) {
                        Box(
                            modifier = Modifier.padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No expense data for ${selectedTimeframe.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        DonutSpendingChart(
                            slices = categorySlices,
                            modifier = Modifier.fillMaxWidth(),
                            currencySymbol = currencySymbol,
                            onSliceSelected = { slice ->
                                selectedCategoryFilter = slice?.label
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Chart 2: Monthly Comparison Grouped Bar Chart
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Income vs. Expense Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Historical grouped monthly performance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    IncomeExpenseBarChart(
                        data = monthlyComparison,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Chart 3: Cash Flow & Net Balance Bezier Line Chart
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Net Balance Trajectory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cumulative cash flow progression",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CashFlowLineChart(
                        points = cashFlowTrend,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Transaction Explorer Header & Search
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction Explorer (${filteredList.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (selectedCategoryFilter != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryEmerald.copy(alpha = 0.2f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedCategoryFilter = null }
                        ) {
                            Text(
                                text = "Clear '${selectedCategoryFilter}'",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by merchant or category...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analytics_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Filtered transactions
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching transactions found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredList) { tx ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    TransactionRowItem(
                        transaction = tx,
                        currencySymbol = currencySymbol,
                        onDelete = { onDeleteTransaction(tx) }
                    )
                }
            }
        }
    }
}
