package com.piggylabs.piggyflow.core.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Small, genuinely shared helpers. Feature-specific formatting stays in its feature. */

private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

/** Parses a date stored in the database, returning null rather than throwing. */
fun String.toLocalDateOrNull(): LocalDate? = try {
    LocalDate.parse(trim(), ISO_DATE)
} catch (_: Exception) {
    null
}

fun LocalDate.toDbDate(): String = format(ISO_DATE)

/** Indian-format currency amount without the symbol, e.g. `1,24,500.50`. */
fun Double.formatAmount(): String = String.format(Locale.US, "%,.2f", this)

fun Double.formatAmountWithSymbol(symbol: String = "₹"): String = "$symbol${formatAmount()}"

/** Falls back to [default] when the receiver is null or blank. */
fun String?.orDefault(default: String): String =
    if (this.isNullOrBlank()) default else this

/** Percentage of [total] this value represents, 0 when [total] is zero. */
fun Double.percentOf(total: Double): Double =
    if (total == 0.0) 0.0 else (this / total) * 100
