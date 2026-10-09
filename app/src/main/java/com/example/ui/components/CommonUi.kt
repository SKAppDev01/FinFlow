package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryEmerald
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase(Locale.ROOT)) {
        "food & dining", "food", "dining" -> Icons.Default.Fastfood
        "transportation", "transport", "auto" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        "entertainment" -> Icons.Default.Movie
        "housing", "rent" -> Icons.Default.Home
        "utilities" -> Icons.Default.Lightbulb
        "salary", "income" -> Icons.Default.Payments
        "investment" -> Icons.Default.MonetizationOn
        "health & wellness", "health" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Payments
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase(Locale.ROOT)) {
        "food & dining", "food" -> Color(0xFFF59E0B)
        "transportation" -> Color(0xFF3B82F6)
        "shopping" -> Color(0xFFEC4899)
        "entertainment" -> Color(0xFF06B6D4)
        "housing" -> Color(0xFF8B5CF6)
        "utilities" -> Color(0xFF6366F1)
        "salary", "income" -> IncomeGreen
        "investment" -> Color(0xFF10B981)
        "health & wellness", "health" -> Color(0xFF14B8A6)
        else -> Color(0xFF94A3B8)
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$",
    onDelete: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val categoryColor = getCategoryColor(transaction.category)
    val icon = getCategoryIcon(transaction.category)
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(transaction.timestamp))
    val isExpense = transaction.type == "EXPENSE"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}")
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with Auto badge indicator if automated
            Box(
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = transaction.category,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (transaction.isAutomated) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .background(PrimaryEmerald, CircleShape)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Automated",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (transaction.isAutomated) {
                        Surface(
                            color = PrimaryEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Auto-tracked",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryEmerald,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isExpense) {
                        "-$currencySymbol${String.format(Locale.US, "%,.2f", transaction.amount)}"
                    } else {
                        "+$currencySymbol${String.format(Locale.US, "%,.2f", transaction.amount)}"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) ExpenseCoral else IncomeGreen
                )

                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp).padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("metric_card_$title"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AccountSummaryCard(
    account: AccountEntity,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$",
    onClick: (() -> Unit)? = null
) {
    val isNegative = account.balance < 0

    Card(
        modifier = modifier
            .width(170.dp)
            .testTag("account_card_${account.id}")
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (account.type) {
                    "CREDIT_CARD" -> Icons.Default.CreditCard
                    "SAVINGS" -> Icons.Default.MonetizationOn
                    "CASH" -> Icons.Default.Payments
                    else -> Icons.Default.AccountBalance
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(PrimaryEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = account.type,
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "*${account.lastFour}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = account.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$currencySymbol${String.format(Locale.US, "%,.2f", account.balance)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isNegative) ExpenseCoral else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
