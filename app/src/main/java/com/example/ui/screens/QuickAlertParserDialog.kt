package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.ParsedAlertResult
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryEmerald
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun QuickAlertParserDialog(
    onDismiss: () -> Unit,
    onParseText: suspend (String) -> ParsedAlertResult,
    onConfirmSave: (ParsedAlertResult) -> Unit,
    currencySymbol: String = "$"
) {
    val coroutineScope = rememberCoroutineScope()
    var rawInputText by remember { mutableStateOf("") }
    var parsedResult by remember { mutableStateOf<ParsedAlertResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    val sampleAlerts = listOf(
        "Chase Alert: Debit card ending 4021 charged $42.50 at WHOLE FOODS MARKET on Oct 09",
        "Your Apple Card was charged $24.80 for UBER TRIP downtown",
        "Direct deposit of $3,400.00 from TECH CORP into Checking *4021",
        "Alert: $89.90 purchase approved at AMAZON.COM on Visa ending 9312",
        "Starbucks Card refill of $15.00 via Apple Pay"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("quick_alert_parser_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                item {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PrimaryEmerald.copy(alpha = 0.15f), CircleShape),
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
                                    text = "Automated Expense Ingestion",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Simulate Bank SMS / Push notification alert",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Text Input
                    OutlinedTextField(
                        value = rawInputText,
                        onValueChange = {
                            rawInputText = it
                            parsedResult = null
                        },
                        label = { Text("Bank SMS or Notification Text") },
                        placeholder = { Text("Paste raw alert message or choose below...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("alert_text_input"),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sample Presets
                    Text(
                        text = "Or test with real sample bank alerts:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(sampleAlerts) { sample ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                rawInputText = sample
                                coroutineScope.launch {
                                    isAnalyzing = true
                                    parsedResult = onParseText(sample)
                                    isAnalyzing = false
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = PrimaryEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = sample,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Parse Action Button
                    Button(
                        onClick = {
                            if (rawInputText.isNotBlank()) {
                                coroutineScope.launch {
                                    isAnalyzing = true
                                    parsedResult = onParseText(rawInputText)
                                    isAnalyzing = false
                                }
                            }
                        },
                        enabled = rawInputText.isNotBlank() && !isAnalyzing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("parse_alert_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAnalyzing) "Analyzing NLP Rules..." else "Run Smart Expense Parser",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Parsed Result Card
                    if (parsedResult != null) {
                        val result = parsedResult!!
                        val catColor = getCategoryColor(result.category)

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("parsed_result_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Extracted & Auto-Categorized",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen
                                        )
                                    }

                                    Text(
                                        text = if (result.type == "EXPENSE") "-$currencySymbol${String.format(Locale.US, "%.2f", result.amount)}"
                                        else "+$currencySymbol${String.format(Locale.US, "%.2f", result.amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (result.type == "EXPENSE") ExpenseCoral else IncomeGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Payee/Merchant: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = result.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Category: ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Surface(
                                            color = catColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = getCategoryIcon(result.category),
                                                    contentDescription = null,
                                                    tint = catColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = result.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = catColor,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Account: ",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = result.accountId.replace("_", " ").replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                if (result.matchedRuleKeyword != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Applied rule: '${result.matchedRuleKeyword}' → ${result.category}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryEmerald,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        onConfirmSave(result)
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("confirm_auto_save_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                                ) {
                                    Text(
                                        text = "Approve & Auto-Log Expense",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
