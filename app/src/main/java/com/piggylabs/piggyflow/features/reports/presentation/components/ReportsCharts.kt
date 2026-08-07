package com.piggylabs.piggyflow.features.reports.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min

/** Data class for a category slice in the donut chart. */
data class ReportCategoryItem(
    val name: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

/** Donut chart showing category-wise spending breakdown. */
@Composable
fun ReportsCategoryDonutChart(
    categories: List<ReportCategoryItem>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.13f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        // Draw background track
        drawArc(
            color = Color.LightGray.copy(alpha = 0.25f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        val total = categories.sumOf { it.amount }.takeIf { it > 0 } ?: 1.0
        var startAngle = -90f
        categories.forEach { item ->
            val sweep = ((item.amount / total) * 360f).toFloat()
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

/** Dual bar chart showing income (green) vs expense (red) side by side per week. */
@Composable
fun DualCashFlowBarChart(modifier: Modifier = Modifier) {
    // Static demo data matching reference image; swap for live data when needed.
    val incomeData = listOf(20000f, 25000f, 18000f, 28000f, 15000f)
    val expenseData = listOf(12000f, 15000f, 10000f, 20000f, 8000f)

    Canvas(modifier = modifier) {
        val maxVal = (incomeData + expenseData).max().coerceAtLeast(1f)
        val barPairs = incomeData.size
        val totalWidth = size.width
        val groupWidth = totalWidth / barPairs
        val barWidth = groupWidth * 0.28f
        val gap = barWidth * 0.3f

        incomeData.forEachIndexed { index, income ->
            val expense = expenseData[index]
            val groupLeft = index * groupWidth + groupWidth * 0.1f

            // Income bar (green)
            val incomeHeight = (income / maxVal) * size.height
            drawRoundRect(
                color = Color(0xFF15803D),
                topLeft = Offset(groupLeft, size.height - incomeHeight),
                size = Size(barWidth, incomeHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // Expense bar (red)
            val expenseHeight = (expense / maxVal) * size.height
            drawRoundRect(
                color = Color(0xFFEF4444),
                topLeft = Offset(groupLeft + barWidth + gap, size.height - expenseHeight),
                size = Size(barWidth, expenseHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }
}
