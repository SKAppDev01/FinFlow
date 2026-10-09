package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryEmerald
import java.util.Locale
import kotlin.math.atan2

data class ChartSlice(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun DonutSpendingChart(
    slices: List<ChartSlice>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$",
    centerTitle: String = "Total Spent",
    strokeWidth: Dp = 28.dp,
    onSliceSelected: (ChartSlice?) -> Unit = {}
) {
    var selectedSlice by remember { mutableStateOf<ChartSlice?>(null) }
    val total = remember(slices) { slices.sumOf { it.value } }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier.testTag("donut_chart_column"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(slices, total) {
                        detectTapGestures { tapOffset ->
                            if (total <= 0) return@detectTapGestures
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = tapOffset.x - center.x
                            val dy = tapOffset.y - center.y
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            // Adjust for top-start (-90 degrees)
                            val normalizedAngle = (angle + 90f) % 360f

                            var currentStart = 0f
                            var clickedSlice: ChartSlice? = null
                            for (slice in slices) {
                                val sweep = (slice.value / total * 360f).toFloat()
                                if (normalizedAngle in currentStart..(currentStart + sweep)) {
                                    clickedSlice = slice
                                    break
                                }
                                currentStart += sweep
                            }
                            selectedSlice = if (selectedSlice == clickedSlice) null else clickedSlice
                            onSliceSelected(selectedSlice)
                        }
                    }
            ) {
                if (total <= 0 || slices.isEmpty()) {
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.2f),
                        style = Stroke(width = strokeWidth.toPx())
                    )
                    return@Canvas
                }

                val strokePx = strokeWidth.toPx()
                val diameter = size.minDimension - strokePx
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)

                var startAngle = -90f
                slices.forEach { slice ->
                    val fullSweep = (slice.value / total * 360f).toFloat()
                    val animatedSweep = fullSweep * animationProgress.value
                    val isSelected = selectedSlice == slice

                    val effectiveStroke = if (isSelected) strokePx * 1.25f else strokePx
                    val drawColor = if (selectedSlice == null || isSelected) {
                        slice.color
                    } else {
                        slice.color.copy(alpha = 0.35f)
                    }

                    drawArc(
                        color = drawColor,
                        startAngle = startAngle + 1.5f,
                        sweepAngle = (animatedSweep - 3f).coerceAtLeast(0.1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = effectiveStroke, cap = StrokeCap.Round)
                    )
                    startAngle += fullSweep
                }
            }

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = selectedSlice?.label ?: centerTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%,.2f", selectedSlice?.value ?: total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (selectedSlice != null && total > 0) {
                    val pct = (selectedSlice!!.value / total * 100).toInt()
                    Surface(
                        color = selectedSlice!!.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "$pct% of total",
                            style = MaterialTheme.typography.labelSmall,
                            color = selectedSlice!!.color,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Legend chips
        Spacer(modifier = Modifier.height(12.dp))
        LegendFlowRow(
            slices = slices,
            selectedSlice = selectedSlice,
            total = total,
            onSliceClick = { slice ->
                selectedSlice = if (selectedSlice == slice) null else slice
                onSliceSelected(selectedSlice)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LegendFlowRow(
    slices: List<ChartSlice>,
    selectedSlice: ChartSlice?,
    total: Double,
    onSliceClick: (ChartSlice) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        slices.take(6).forEach { slice ->
            val isSelected = selectedSlice == slice
            val percentage = if (total > 0) (slice.value / total * 100).toInt() else 0

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) slice.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSliceClick(slice) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(slice.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class BarComparisonGroup(
    val label: String,
    val income: Double,
    val expense: Double
)

@Composable
fun IncomeExpenseBarChart(
    data: List<BarComparisonGroup>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxVal = remember(data) {
        data.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(100.0) ?: 100.0
    }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = modifier.testTag("bar_chart_comparison")) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).background(IncomeGreen, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.size(10.dp).background(ExpenseCoral, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasHeight = size.height - 24.dp.toPx()
                val groupWidth = size.width / data.size
                val barWidth = 14.dp.toPx()
                val spacing = 4.dp.toPx()

                // Draw 3 horizontal guideline dashes
                val steps = 3
                for (i in 1..steps) {
                    val y = canvasHeight * (1f - (i.toFloat() / steps))
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.15f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw grouped bars
                data.forEachIndexed { index, group ->
                    val centerX = groupWidth * index + groupWidth / 2f
                    val incomeHeight = ((group.income / maxVal) * canvasHeight * animProgress.value).toFloat()
                    val expenseHeight = ((group.expense / maxVal) * canvasHeight * animProgress.value).toFloat()

                    // Income Bar (Left)
                    val incomeX = centerX - barWidth - (spacing / 2f)
                    drawRoundRect(
                        color = IncomeGreen,
                        topLeft = Offset(incomeX, canvasHeight - incomeHeight),
                        size = Size(barWidth, incomeHeight.coerceAtLeast(2f)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Expense Bar (Right)
                    val expenseX = centerX + (spacing / 2f)
                    drawRoundRect(
                        color = ExpenseCoral,
                        topLeft = Offset(expenseX, canvasHeight - expenseHeight),
                        size = Size(barWidth, expenseHeight.coerceAtLeast(2f)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            // Labels row at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                data.forEach { group ->
                    Text(
                        text = group.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CashFlowLineChart(
    points: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return

    val minVal = remember(points) { points.minOf { it.second } }
    val maxVal = remember(points) { points.maxOf { it.second }.coerceAtLeast(minVal + 1.0) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    Column(modifier = modifier.testTag("cashflow_line_chart")) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartH = size.height - 20.dp.toPx()
                val stepX = size.width / (points.size - 1)

                val normalizedPoints = points.mapIndexed { idx, pair ->
                    val normY = ((pair.second - minVal) / (maxVal - minVal)).toFloat()
                    val y = chartH * (1f - (normY * animProgress.value))
                    Offset(idx * stepX, y)
                }

                val strokePath = Path()
                val fillPath = Path()

                fillPath.moveTo(0f, chartH)

                normalizedPoints.forEachIndexed { i, pt ->
                    if (i == 0) {
                        strokePath.moveTo(pt.x, pt.y)
                        fillPath.lineTo(pt.x, pt.y)
                    } else {
                        val prev = normalizedPoints[i - 1]
                        val cx = (prev.x + pt.x) / 2f
                        strokePath.cubicTo(cx, prev.y, cx, pt.y, pt.x, pt.y)
                        fillPath.cubicTo(cx, prev.y, cx, pt.y, pt.x, pt.y)
                    }
                }

                fillPath.lineTo(size.width, chartH)
                fillPath.close()

                // Area gradient
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(PrimaryEmerald.copy(alpha = 0.35f), Color.Transparent),
                        startY = 0f,
                        endY = chartH
                    )
                )

                // Smooth line
                drawPath(
                    path = strokePath,
                    color = PrimaryEmerald,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Key dots
                normalizedPoints.forEach { pt ->
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = PrimaryEmerald,
                        radius = 2.5.dp.toPx(),
                        center = pt
                    )
                }
            }

            // Bottom X-axis labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { (label, _) ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBudgetMeter(
    category: String,
    spent: Double,
    limit: Double,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    val progress = if (limit > 0) (spent / limit).toFloat() else 0f
    val percentage = (progress * 100).toInt()
    val isOver = spent > limit
    val meterColor = when {
        progress >= 1.0f -> ExpenseCoral
        progress >= 0.8f -> Color(0xFFF59E0B) // Warning amber
        else -> IncomeGreen
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isOver) {
                        "Over by $currencySymbol${String.format(Locale.US, "%.2f", spent - limit)}"
                    } else {
                        "$currencySymbol${String.format(Locale.US, "%.2f", limit - spent)} remaining"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOver) ExpenseCoral else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%.0f", spent)} / $currencySymbol${String.format(Locale.US, "%.0f", limit)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelSmall,
                    color = meterColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceAtMost(1f))
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(meterColor)
            )
        }
    }
}

data class DailySpendItem(
    val dayLabel: String,
    val dateLabel: String,
    val amount: Double,
    val isToday: Boolean
)

@Composable
fun WeeklyActivityChart(
    days: List<DailySpendItem>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    if (days.isEmpty()) return

    val maxAmount = remember(days) { days.maxOfOrNull { it.amount }?.coerceAtLeast(50.0) ?: 50.0 }
    val totalWeekly = remember(days) { days.sumOf { it.amount } }
    val dailyAvg = remember(days) { totalWeekly / days.size }
    var selectedDay by remember { mutableStateOf<DailySpendItem?>(null) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(days) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
    }

    Column(modifier = modifier.testTag("weekly_activity_chart")) {
        // Stats subheader
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (selectedDay != null) "${selectedDay!!.dayLabel} Spending" else "7-Day Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (selectedDay != null) {
                        "$currencySymbol${String.format(Locale.US, "%,.2f", selectedDay!!.amount)}"
                    } else {
                        "$currencySymbol${String.format(Locale.US, "%,.2f", totalWeekly)}"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Avg $currencySymbol${String.format(Locale.US, "%.0f", dailyAvg)}/day",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryEmerald,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 7-day pill bar canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { item ->
                    val isSelected = selectedDay == item
                    val isPeak = item.amount == maxAmount && item.amount > 0
                    val fraction = ((item.amount / maxAmount) * animProgress.value).toFloat().coerceIn(0.04f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedDay = if (selectedDay == item) null else item
                            }
                            .padding(horizontal = 2.dp)
                    ) {
                        // Bar Container
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(95.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Track background
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            )

                            // Filled bar
                            val barColor = when {
                                isSelected -> PrimaryEmerald
                                item.isToday -> PrimaryEmerald
                                isPeak -> ExpenseCoral.copy(alpha = 0.85f)
                                item.amount > 0 -> PrimaryEmerald.copy(alpha = 0.65f)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((95 * fraction).dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(barColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day Label
                        Text(
                            text = item.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (item.isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (item.isToday) PrimaryEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

