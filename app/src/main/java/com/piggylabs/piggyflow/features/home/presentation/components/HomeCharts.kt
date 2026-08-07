package com.piggylabs.piggyflow.features.home.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piggylabs.piggyflow.core.designsystem.theme.appColors

/** One slice of a donut. Value is an absolute amount; shares are computed by the chart. */
data class DonutSlice(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun SavingsGaugeChart(
    savingsRate: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color.White.copy(alpha = 0.22f),
    progressColor: Color = Color(0xFF86EFAC),
    labelColor: Color = Color.White,
    caption: String = "Savings Rate",
    /** Overrides the centre text. Use when the arc shows a score rather than a percentage. */
    valueLabel: String? = null
) {
    val animatedRate by animateFloatAsState(
        targetValue = savingsRate.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 600),
        label = "savingsRate"
    )

    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = -220f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val progressSweep = (animatedRate / 100f) * 260f
            if (progressSweep > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -220f,
                    sweepAngle = progressSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = valueLabel ?: "%.1f%%".format(savingsRate),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = labelColor
            )
            Text(
                text = caption,
                fontSize = 9.sp,
                color = labelColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Bars scaled against the largest value in [values]. An all-zero series draws a flat
 * baseline instead of collapsing to nothing, so an empty month still reads as a chart.
 */
@Composable
fun SpendBarChart(
    values: List<Double>,
    modifier: Modifier = Modifier,
    barColor: Color = appColors().accent,
    emptyColor: Color = appColors().surfaceMuted,
    showAverageLine: Boolean = true
) {
    val gridColor = appColors().textMuted.copy(alpha = 0.35f)
    val series = values.ifEmpty { List(5) { 0.0 } }
    val max = series.maxOrNull() ?: 0.0
    val average = if (series.isNotEmpty()) series.average() else 0.0

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val minBarHeight = 3.dp.toPx()
        val slot = width / series.size
        val barWidth = (slot * 0.45f).coerceAtMost(14.dp.toPx())

        if (showAverageLine && max > 0) {
            val y = height - (height * (average / max)).toFloat()
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        series.forEachIndexed { index, value ->
            val fraction = if (max > 0) (value / max).toFloat() else 0f
            val barHeight = (height * fraction).coerceAtLeast(minBarHeight)
            val x = (index * slot) + (slot - barWidth) / 2f

            drawRoundRect(
                color = if (value > 0) barColor else emptyColor,
                topLeft = Offset(x, height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

/** Grouped income/expense bars, both scaled against a shared maximum. */
@Composable
fun DualBarChart(
    incomeValues: List<Double>,
    expenseValues: List<Double>,
    modifier: Modifier = Modifier,
    incomeColor: Color = appColors().positive,
    expenseColor: Color = appColors().negative,
    emptyColor: Color = appColors().surfaceMuted
) {
    val gridColor = appColors().textMuted.copy(alpha = 0.3f)
    val count = maxOf(incomeValues.size, expenseValues.size, 1)
    val max = (incomeValues + expenseValues).maxOrNull() ?: 0.0

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val minBarHeight = 3.dp.toPx()
        val slot = width / count
        val barWidth = (slot * 0.28f).coerceAtMost(10.dp.toPx())
        val gap = barWidth * 0.35f

        drawLine(
            color = gridColor,
            start = Offset(0f, height * 0.5f),
            end = Offset(width, height * 0.5f),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )

        for (index in 0 until count) {
            val income = incomeValues.getOrElse(index) { 0.0 }
            val expense = expenseValues.getOrElse(index) { 0.0 }

            val groupWidth = (barWidth * 2) + gap
            val groupStart = (index * slot) + (slot - groupWidth) / 2f

            val incomeHeight = (
                if (max > 0) height * (income / max).toFloat() else 0f
                ).coerceAtLeast(minBarHeight)
            drawRoundRect(
                color = if (income > 0) incomeColor else emptyColor,
                topLeft = Offset(groupStart, height - incomeHeight),
                size = Size(barWidth, incomeHeight),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            val expenseHeight = (
                if (max > 0) height * (expense / max).toFloat() else 0f
                ).coerceAtLeast(minBarHeight)
            drawRoundRect(
                color = if (expense > 0) expenseColor else emptyColor,
                topLeft = Offset(groupStart + barWidth + gap, height - expenseHeight),
                size = Size(barWidth, expenseHeight),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )
        }
    }
}

/**
 * Donut sized by each slice's share of the total. Renders a single muted ring when
 * there is nothing to show, rather than an empty box.
 */
@Composable
fun CategoryDonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 18.dp,
    centerContent: @Composable (() -> Unit)? = null
) {
    val emptyColor = appColors().surfaceMuted
    val total = slices.sumOf { it.value }

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            val arcSize = Size(diameter, diameter)

            if (total <= 0.0) {
                drawArc(
                    color = emptyColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke)
                )
                return@Canvas
            }

            // A gap between slices only makes sense when there is more than one.
            val gap = if (slices.size > 1) 3f else 0f
            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = (slice.value / total).toFloat() * 360f
                if (sweep > 0f) {
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = (sweep - gap).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke)
                    )
                }
                startAngle += sweep
            }
        }

        centerContent?.invoke()
    }
}
