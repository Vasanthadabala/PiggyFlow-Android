package com.piggylabs.piggyflow.core.utils

import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.Income
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

/**
 * Turns the raw expense/income rows into the figures the Home and Reports screens show.
 * Everything here is derived from stored data, so an empty database yields zeroes rather
 * than placeholder numbers.
 */

private val DAY_MONTH = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
private val MONTH_YEAR = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)
private val FULL_DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

/** Parses a stored ISO date, returning null instead of throwing on legacy/corrupt values. */
fun parseDbDateOrNull(dbDate: String): LocalDate? =
    runCatching { LocalDate.parse(dbDate) }.getOrNull()

// ---------------------------------------------------------------- date ranges

data class DateWindow(
    val start: LocalDate,
    val end: LocalDate,
    val label: String
) {
    fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

    val dayCount: Int get() = (ChronoUnit.DAYS.between(start, end) + 1).toInt().coerceAtLeast(1)

    /**
     * Days of this window that have actually happened. Averaging over this instead of
     * [dayCount] stops a mid-month view from dividing by days that haven't occurred yet.
     */
    fun elapsedDays(today: LocalDate = LocalDate.now()): Int {
        val effectiveEnd = if (end.isAfter(today)) today else end
        if (effectiveEnd.isBefore(start)) return 1
        return (ChronoUnit.DAYS.between(start, effectiveEnd) + 1).toInt().coerceAtLeast(1)
    }
}

object DateRanges {
    const val TODAY = "Today"
    const val YESTERDAY = "Yesterday"
    const val THIS_WEEK = "This Week"
    const val THIS_MONTH = "This Month"
    const val LAST_MONTH = "Last Month"
    const val THIS_YEAR = "This Year"

    val all = listOf(TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, LAST_MONTH, THIS_YEAR)
}

fun windowFor(range: String, today: LocalDate = LocalDate.now()): DateWindow = when (range) {
    DateRanges.TODAY -> DateWindow(today, today, today.format(FULL_DATE))

    DateRanges.YESTERDAY -> today.minusDays(1).let {
        DateWindow(it, it, it.format(FULL_DATE))
    }

    DateRanges.THIS_WEEK -> {
        val start = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val end = start.plusDays(6)
        DateWindow(start, end, "${start.format(DAY_MONTH)} – ${end.format(DAY_MONTH)}")
    }

    DateRanges.LAST_MONTH -> {
        val month = YearMonth.from(today).minusMonths(1)
        DateWindow(month.atDay(1), month.atEndOfMonth(), month.atDay(1).format(MONTH_YEAR))
    }

    DateRanges.THIS_YEAR -> DateWindow(
        LocalDate.of(today.year, 1, 1),
        LocalDate.of(today.year, 12, 31),
        today.year.toString()
    )

    else -> {
        val month = YearMonth.from(today)
        DateWindow(month.atDay(1), month.atEndOfMonth(), month.atDay(1).format(MONTH_YEAR))
    }
}

/** The equally-long window immediately before [window], used for "vs last period" deltas. */
fun previousWindow(window: DateWindow): DateWindow {
    val length = ChronoUnit.DAYS.between(window.start, window.end) + 1
    val start = window.start.minusDays(length)
    val end = window.start.minusDays(1)
    return DateWindow(start, end, "${start.format(DAY_MONTH)} – ${end.format(DAY_MONTH)}")
}

/**
 * The window to compare [range] against. Calendar ranges step back by a whole calendar
 * unit so "This Month" is measured against last month, not against the previous 31 days.
 */
fun comparisonWindow(
    range: String,
    window: DateWindow,
    today: LocalDate = LocalDate.now()
): DateWindow = when (range) {
    DateRanges.THIS_MONTH -> windowFor(DateRanges.LAST_MONTH, today)

    DateRanges.LAST_MONTH -> {
        val month = YearMonth.from(today).minusMonths(2)
        DateWindow(month.atDay(1), month.atEndOfMonth(), month.atDay(1).format(MONTH_YEAR))
    }

    DateRanges.THIS_YEAR -> DateWindow(
        LocalDate.of(today.year - 1, 1, 1),
        LocalDate.of(today.year - 1, 12, 31),
        (today.year - 1).toString()
    )

    else -> previousWindow(window)
}

fun List<Expense>.inWindow(window: DateWindow): List<Expense> =
    filter { parseDbDateOrNull(it.date)?.let(window::contains) == true }

@JvmName("incomeInWindow")
fun List<Income>.inWindow(window: DateWindow): List<Income> =
    filter { parseDbDateOrNull(it.date)?.let(window::contains) == true }

// ---------------------------------------------------------------- summaries

data class PeriodSummary(
    val income: Double,
    val expense: Double,
    val transactionCount: Int
) {
    val net: Double get() = income - expense

    /** Share of income kept. Zero when nothing was earned, so it never divides by zero. */
    val savingsRate: Float
        get() = if (income > 0) ((income - expense) / income * 100).toFloat() else 0f
}

fun summarise(
    expenses: List<Expense>,
    income: List<Income>,
    window: DateWindow
): PeriodSummary {
    val e = expenses.inWindow(window)
    val i = income.inWindow(window)
    return PeriodSummary(
        income = i.sumOf { it.amount },
        expense = e.sumOf { it.amount },
        transactionCount = e.size + i.size
    )
}

/**
 * Percentage change from [previous] to [current]. Null when there is no previous
 * figure to compare against, so the UI can hide the badge instead of showing "▲ 0%".
 */
fun percentChange(current: Double, previous: Double): Float? {
    if (previous == 0.0) return null
    return (((current - previous) / abs(previous)) * 100).toFloat()
}

fun formatSignedPercent(change: Float): String {
    val arrow = if (change >= 0) "▲" else "▼"
    return "$arrow %.1f%%".format(abs(change))
}

// ---------------------------------------------------------------- categories

data class CategoryTotal(
    val name: String,
    val emoji: String,
    val amount: Double,
    val share: Float,
    val count: Int
)

/**
 * Groups expenses by category. When there are more than [limit] categories the tail is
 * merged into a single "Others" slice so the donut stays readable.
 */
fun categoryTotals(expenses: List<Expense>, limit: Int = 5): List<CategoryTotal> {
    if (expenses.isEmpty()) return emptyList()
    val total = expenses.sumOf { it.amount }
    if (total <= 0) return emptyList()

    val grouped = expenses
        .groupBy { it.categoryName.ifBlank { "Uncategorised" } }
        .map { (name, rows) ->
            CategoryTotal(
                name = name,
                emoji = rows.firstOrNull { it.categoryEmoji.isNotBlank() }?.categoryEmoji.orEmpty(),
                amount = rows.sumOf { it.amount },
                share = (rows.sumOf { it.amount } / total * 100).toFloat(),
                count = rows.size
            )
        }
        .sortedByDescending { it.amount }

    if (grouped.size <= limit) return grouped

    val head = grouped.take(limit - 1)
    val tail = grouped.drop(limit - 1)
    return head + CategoryTotal(
        name = "Others",
        emoji = "",
        amount = tail.sumOf { it.amount },
        share = tail.sumOf { it.share.toDouble() }.toFloat(),
        count = tail.sumOf { it.count }
    )
}

/**
 * Top payees, taken from the free-text note on each expense. Falls back to an empty
 * list when the user hasn't been writing notes, so the UI can hide the section.
 */
fun topPayees(expenses: List<Expense>, limit: Int = 5): List<CategoryTotal> {
    val named = expenses.filter { it.note.isNotBlank() }
    if (named.isEmpty()) return emptyList()
    val total = named.sumOf { it.amount }

    return named
        .groupBy { it.note.trim().lowercase(Locale.getDefault()) }
        .map { (_, rows) ->
            CategoryTotal(
                name = rows.first().note.trim(),
                emoji = rows.first().categoryEmoji,
                amount = rows.sumOf { it.amount },
                share = if (total > 0) (rows.sumOf { it.amount } / total * 100).toFloat() else 0f,
                count = rows.size
            )
        }
        .sortedByDescending { it.amount }
        .take(limit)
}

// ---------------------------------------------------------------- time series

data class SeriesBucket(
    val label: String,
    val income: Double,
    val expense: Double
)

/**
 * Splits [window] into [buckets] equal slices and totals each one. Used by the bar
 * charts, which previously drew a fixed set of made-up bar heights.
 */
fun buildSeries(
    expenses: List<Expense>,
    income: List<Income>,
    window: DateWindow,
    buckets: Int = 5
): List<SeriesBucket> {
    val days = window.dayCount
    val slices = buckets.coerceAtLeast(1).coerceAtMost(days)
    val perSlice = days.toDouble() / slices

    val expenseByDate = expenses.inWindow(window).mapNotNull { row ->
        parseDbDateOrNull(row.date)?.let { it to row.amount }
    }
    val incomeByDate = income.inWindow(window).mapNotNull { row ->
        parseDbDateOrNull(row.date)?.let { it to row.amount }
    }

    return (0 until slices).map { index ->
        val startOffset = (index * perSlice).toLong()
        val endOffset = (((index + 1) * perSlice).toLong() - 1).coerceAtLeast(startOffset)
        val start = window.start.plusDays(startOffset)
        val end = window.start.plusDays(endOffset).coerceAtMostDate(window.end)

        SeriesBucket(
            label = start.format(DAY_MONTH),
            income = incomeByDate.filter { it.first in start..end }.sumOf { it.second },
            expense = expenseByDate.filter { it.first in start..end }.sumOf { it.second }
        )
    }
}

private fun LocalDate.coerceAtMostDate(other: LocalDate) = if (isAfter(other)) other else this

enum class GroupMode(val label: String, val subtitle: String) {
    DAY("Date-wise", "Day-by-day breakdown"),
    WEEK("Week-wise", "Week-by-week analysis"),
    MONTH("Month-wise", "Detailed monthly analysis"),
    YEAR("Year-wise", "Yearly spending summary")
}

/**
 * Totals every transaction in [window] bucketed by [mode]. Unlike [buildSeries] this keeps
 * only periods that actually contain activity, which is what a report table wants.
 */
fun groupTotals(
    expenses: List<Expense>,
    income: List<Income>,
    window: DateWindow,
    mode: GroupMode
): List<SeriesBucket> {
    data class Entry(val date: LocalDate, val amount: Double, val isExpense: Boolean)

    val entries = expenses.inWindow(window).mapNotNull { row ->
        parseDbDateOrNull(row.date)?.let { Entry(it, row.amount, true) }
    } + income.inWindow(window).mapNotNull { row ->
        parseDbDateOrNull(row.date)?.let { Entry(it, row.amount, false) }
    }

    if (entries.isEmpty()) return emptyList()

    return entries
        .groupBy { entry -> groupKey(entry.date, mode) }
        .map { (key, rows) ->
            key to SeriesBucket(
                label = groupLabel(rows.first().date, mode),
                income = rows.filter { !it.isExpense }.sumOf { it.amount },
                expense = rows.filter { it.isExpense }.sumOf { it.amount }
            )
        }
        .sortedByDescending { it.first }
        .map { it.second }
}

private fun groupKey(date: LocalDate, mode: GroupMode): String = when (mode) {
    GroupMode.DAY -> date.toString()
    GroupMode.WEEK -> {
        val week = date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())
        val year = date.get(java.time.temporal.WeekFields.ISO.weekBasedYear())
        "%04d-W%02d".format(year, week)
    }
    GroupMode.MONTH -> "%04d-%02d".format(date.year, date.monthValue)
    GroupMode.YEAR -> date.year.toString()
}

private fun groupLabel(date: LocalDate, mode: GroupMode): String = when (mode) {
    GroupMode.DAY -> date.format(FULL_DATE)
    GroupMode.WEEK -> {
        val start = date.minusDays((date.dayOfWeek.value - 1).toLong())
        "${start.format(DAY_MONTH)} – ${start.plusDays(6).format(DAY_MONTH)}"
    }
    GroupMode.MONTH -> date.format(MONTH_YEAR)
    GroupMode.YEAR -> date.year.toString()
}

/** The single day with the highest total spend inside [window], or null when there is none. */
fun highestSpendDay(expenses: List<Expense>, window: DateWindow): Pair<LocalDate, Double>? =
    expenses.inWindow(window)
        .mapNotNull { row -> parseDbDateOrNull(row.date)?.let { it to row.amount } }
        .groupBy({ it.first }, { it.second })
        .map { (date, amounts) -> date to amounts.sum() }
        .maxByOrNull { it.second }

fun LocalDate.formatFull(): String = format(FULL_DATE)

fun LocalDate.formatDayMonth(): String = format(DAY_MONTH)

// ---------------------------------------------------------------- health score

data class HealthScore(
    val total: Int,
    val savingsPoints: Int,
    val spendRatioPoints: Int,
    val consistencyPoints: Int,
    val label: String
)

/**
 * A transparent 0–100 score so the banner reflects the user's own numbers:
 * savings rate (50), income-to-spend ratio (30) and tracking consistency (20).
 */
fun healthScore(
    summary: PeriodSummary,
    expenses: List<Expense>,
    income: List<Income>,
    window: DateWindow
): HealthScore {
    val savingsPoints = if (summary.income <= 0) 0
    else ((summary.savingsRate / 40f).coerceIn(0f, 1f) * 50f).toInt()

    val spendRatioPoints = when {
        summary.income <= 0 -> 0
        else -> {
            val ratio = summary.expense / summary.income
            ((1 - ratio).coerceIn(0.0, 1.0) * 30).toInt()
        }
    }

    val loggedDays = (expenses.inWindow(window).mapNotNull { parseDbDateOrNull(it.date) } +
        income.inWindow(window).mapNotNull { parseDbDateOrNull(it.date) })
        .distinct().size
    val elapsedDays = window.dayCount.coerceAtMost(
        (ChronoUnit.DAYS.between(window.start, minOf(LocalDate.now(), window.end)) + 1)
            .toInt().coerceAtLeast(1)
    )
    val consistencyPoints = ((loggedDays.toFloat() / elapsedDays).coerceIn(0f, 1f) * 20f).toInt()

    val total = savingsPoints + spendRatioPoints + consistencyPoints
    return HealthScore(
        total = total,
        savingsPoints = savingsPoints,
        spendRatioPoints = spendRatioPoints,
        consistencyPoints = consistencyPoints,
        label = when {
            total >= 80 -> "Excellent"
            total >= 60 -> "Good"
            total >= 40 -> "Fair"
            total > 0 -> "Needs work"
            else -> "No data"
        }
    )
}

// ---------------------------------------------------------------- nudges

enum class NudgeTone { POSITIVE, WARNING, NEGATIVE, INFO }

data class Nudge(
    val title: String,
    val description: String,
    val tone: NudgeTone
)

/**
 * Observations built by comparing the selected window against the one before it.
 * Returns an empty list when there isn't enough data to say anything truthful.
 */
fun buildNudges(
    expenses: List<Expense>,
    income: List<Income>,
    window: DateWindow,
    prevWindow: DateWindow = previousWindow(window)
): List<Nudge> {
    val current = summarise(expenses, income, window)
    if (current.transactionCount == 0) return emptyList()

    val previous = summarise(expenses, income, prevWindow)
    val nudges = mutableListOf<Nudge>()

    // Savings rate against a 40% target.
    if (current.income > 0) {
        if (current.savingsRate >= 40f) {
            nudges += Nudge(
                title = "On track with savings",
                description = "You kept %.1f%% of your income in %s, above the 40%% target."
                    .format(current.savingsRate, window.label),
                tone = NudgeTone.POSITIVE
            )
        } else if (current.savingsRate >= 0f) {
            nudges += Nudge(
                title = "Savings below target",
                description = "You kept %.1f%% of your income. Trimming ₹%,.0f of spending would reach the 40%% target."
                    .format(current.savingsRate, (current.income * 0.6) - current.expense.coerceAtLeast(0.0)),
                tone = NudgeTone.WARNING
            )
        } else {
            nudges += Nudge(
                title = "Spending exceeded income",
                description = "You spent ₹%,.0f more than you earned in %s."
                    .format(current.expense - current.income, window.label),
                tone = NudgeTone.NEGATIVE
            )
        }
    }

    // Total spend versus the previous period.
    percentChange(current.expense, previous.expense)?.let { change ->
        if (abs(change) >= 5f) {
            nudges += if (change < 0) {
                Nudge(
                    title = "Spending is down",
                    description = "Total spend fell %.1f%% versus %s — ₹%,.0f less."
                        .format(abs(change), prevWindow.label, previous.expense - current.expense),
                    tone = NudgeTone.POSITIVE
                )
            } else {
                Nudge(
                    title = "Spending is up",
                    description = "Total spend rose %.1f%% versus %s — ₹%,.0f more."
                        .format(change, prevWindow.label, current.expense - previous.expense),
                    tone = NudgeTone.WARNING
                )
            }
        }
    }

    // The category that moved the most between the two periods.
    val currentByCategory = expenses.inWindow(window)
        .groupBy { it.categoryName.ifBlank { "Uncategorised" } }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
    val previousByCategory = expenses.inWindow(prevWindow)
        .groupBy { it.categoryName.ifBlank { "Uncategorised" } }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    currentByCategory
        .mapNotNull { (name, amount) ->
            val before = previousByCategory[name] ?: return@mapNotNull null
            val delta = amount - before
            if (abs(delta) < 1.0) null else Triple(name, delta, before)
        }
        .maxByOrNull { abs(it.second) }
        ?.let { (name, delta, before) ->
            val pct = percentChange(before + delta, before)
            nudges += if (delta > 0) {
                Nudge(
                    title = "$name increased",
                    description = "You spent ₹%,.0f more on %s than in %s%s."
                        .format(delta, name, prevWindow.label, pct?.let { " (%.0f%% up)".format(it) } ?: ""),
                    tone = NudgeTone.WARNING
                )
            } else {
                Nudge(
                    title = "$name savings",
                    description = "You spent ₹%,.0f less on %s than in %s. Nice work."
                        .format(abs(delta), name, prevWindow.label),
                    tone = NudgeTone.POSITIVE
                )
            }
        }

    // Largest single category share.
    categoryTotals(expenses.inWindow(window), limit = 8).firstOrNull()?.let { top ->
        if (top.share >= 35f && top.name != "Others") {
            nudges += Nudge(
                title = "${top.name} dominates your spending",
                description = "%.0f%% of this period's spend (₹%,.0f) went to %s across %s."
                    .format(
                        top.share,
                        top.amount,
                        top.name,
                        if (top.count == 1) "1 transaction" else "${top.count} transactions"
                    ),
                tone = NudgeTone.INFO
            )
        }
    }

    return nudges
}
