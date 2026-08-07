package com.piggylabs.piggyflow.core.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.piggylabs.piggyflow.core.domain.model.BusinessEntry
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.Income
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** A single exportable row, flattened from either an expense or an income record. */
data class ExportRow(
    val type: String,
    val date: String,
    val amount: Double,
    val categoryName: String,
    val note: String
)

fun buildExportRows(
    expenses: List<Expense>,
    income: List<Income>
): List<ExportRow> {
    val rows = expenses.map {
        ExportRow("Expense", it.date, it.amount, it.categoryName, it.note)
    } + income.map {
        ExportRow("Income", it.date, it.amount, it.categoryName, it.note)
    }
    return rows.sortedByDescending { it.date }
}

fun generateTransactionPdf(
    context: Context,
    expenses: List<Expense>,
    income: List<Income>,
    reportTitle: String = "All Transactions"
): String = generateTransactionPdf(context, reportTitle, buildExportRows(expenses, income))

fun generateTransactionPdf(
    context: Context,
    categoryName: String,
    transactions: List<ExportRow>
): String {

    val pdf = PdfDocument()

    val titlePaint = Paint().apply {
        textSize = 22f
        isFakeBoldText = true
        color = Color.BLACK
    }

    val headerPaint = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        color = Color.BLACK
    }

    val rowPaint = Paint().apply {
        textSize = 15f
        color = Color.DKGRAY
    }

    val linePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
    }

    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var page = pdf.startPage(pageInfo)
    var canvas: Canvas = page.canvas

    var y = 60

    /* ----------- TITLE ------------ */
    canvas.drawText("Transactions - $categoryName", 40f, y.toFloat(), titlePaint)
    y += 26

    /* ----------- SUMMARY ------------ */
    val totalIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }
    canvas.drawText(
        "Income: ₹%,.2f    Expenses: ₹%,.2f    Net: ₹%,.2f".format(
            totalIncome,
            totalExpense,
            totalIncome - totalExpense
        ),
        40f,
        y.toFloat(),
        rowPaint
    )
    y += 18
    canvas.drawText("${transactions.size} transactions", 40f, y.toFloat(), rowPaint)
    y += 28

    /* ----------- TABLE HEADERS ------------ */
    val colTypeX = 40f
    val colDateX = 130f
    val colAmountX = 260f
    val colDescX = 380f

    canvas.drawText("Type", colTypeX, y.toFloat(), headerPaint)
    canvas.drawText("Date", colDateX, y.toFloat(), headerPaint)
    canvas.drawText("Amount", colAmountX, y.toFloat(), headerPaint)
    canvas.drawText("Description", colDescX, y.toFloat(), headerPaint)

    y += 20
    canvas.drawLine(40f, y.toFloat(), 550f, y.toFloat(), linePaint)
    y += 20

    /* ----------- ROWS ------------ */
    transactions.forEach { txn ->

        // Break to new page if needed
        if (y > 780) {
            pdf.finishPage(page)
            page = pdf.startPage(pageInfo)
            canvas = page.canvas
            y = 60
        }

        canvas.drawText(txn.type, colTypeX, y.toFloat(), rowPaint)
        canvas.drawText(txn.date, colDateX, y.toFloat(), rowPaint)
        canvas.drawText("₹${txn.amount}", colAmountX, y.toFloat(), rowPaint)

        // Handle long descriptions
        val desc = txn.note.ifBlank { txn.categoryName }
        val maxDescWidth = 180f
        val descLines = splitLongText(desc, rowPaint, maxDescWidth)

        descLines.forEachIndexed { index, line ->
            canvas.drawText(line, colDescX, y + (index * 18).toFloat(), rowPaint)
        }

        y += maxOf(24, descLines.size * 18)
        canvas.drawLine(40f, y.toFloat(), 550f, y.toFloat(), linePaint)
        y += 16
    }

    pdf.finishPage(page)

    val fileName = "Transactions_${categoryName}_${System.currentTimeMillis()}.pdf"

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        resolver.openOutputStream(uri!!).use { output ->
            pdf.writeTo(output)
        }

        pdf.close()
        "Saved to Downloads/$fileName"

    } else {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        FileOutputStream(file).use { output ->
            pdf.writeTo(output)
        }
        pdf.close()
        "Saved to: ${file.absolutePath}"
    }
}

/**
 * Writes the transactions to a real .csv in Downloads. Spreadsheet apps open this
 * directly, which is what the "Excel" and "CSV" export actions both need.
 */
fun generateTransactionCsv(
    context: Context,
    expenses: List<Expense>,
    income: List<Income>,
    reportTitle: String = "All Transactions"
): String {
    val rows = buildExportRows(expenses, income)

    val csv = buildString {
        appendLine("Type,Date,Amount,Category,Note")
        rows.forEach { row ->
            append(csvCell(row.type)).append(',')
            append(csvCell(row.date)).append(',')
            append(row.amount).append(',')
            append(csvCell(row.categoryName)).append(',')
            appendLine(csvCell(row.note))
        }
    }

    val safeTitle = reportTitle.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')
    val fileName = "Transactions_${safeTitle}_${System.currentTimeMillis()}.csv"
    val bytes = csv.toByteArray(Charsets.UTF_8)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return "Could not create $fileName"
        resolver.openOutputStream(uri).use { output ->
            output?.write(bytes)
        }
        "Saved to Downloads/$fileName"
    } else {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        FileOutputStream(file).use { output ->
            output.write(bytes)
        }
        "Saved to: ${file.absolutePath}"
    }
}

/** Quotes a CSV cell so commas, quotes and newlines in notes don't break the columns. */
private fun csvCell(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
}

fun splitLongText(text: String, paint: Paint, maxWidth: Float): List<String> {
    val words = text.split(" ")
    val lines = mutableListOf<String>()
    var currentLine = ""

    for (word in words) {
        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
        if (paint.measureText(testLine) <= maxWidth) {
            currentLine = testLine
        } else {
            lines.add(currentLine)
            currentLine = word
        }
    }

    if (currentLine.isNotEmpty()) {
        lines.add(currentLine)
    }

    return lines
}

fun generateBusinessLedgerPdf(
    context: Context,
    partyName: String,
    phone: String,
    balance: Double,
    entries: List<BusinessEntry>
): String {
    val pdf = PdfDocument()

    val titlePaint = Paint().apply {
        textSize = 22f
        isFakeBoldText = true
        color = Color.BLACK
    }
    val headerPaint = Paint().apply {
        textSize = 16f
        isFakeBoldText = true
        color = Color.BLACK
    }
    val rowPaint = Paint().apply {
        textSize = 14f
        color = Color.DKGRAY
    }
    val linePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
    }

    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var page = pdf.startPage(pageInfo)
    var canvas = page.canvas
    var y = 60

    val formatter = SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault())

    canvas.drawText("Flow Ledger - $partyName", 40f, y.toFloat(), titlePaint)
    y += 28
    canvas.drawText("Phone: $phone", 40f, y.toFloat(), rowPaint)
    y += 22
    canvas.drawText("Net Balance: ₹$balance", 40f, y.toFloat(), headerPaint)
    y += 34

    val colDateX = 40f
    val colTypeX = 190f
    val colAmountX = 300f
    val colNoteX = 400f

    canvas.drawText("Date", colDateX, y.toFloat(), headerPaint)
    canvas.drawText("Type", colTypeX, y.toFloat(), headerPaint)
    canvas.drawText("Amount", colAmountX, y.toFloat(), headerPaint)
    canvas.drawText("Note", colNoteX, y.toFloat(), headerPaint)
    y += 20
    canvas.drawLine(40f, y.toFloat(), 550f, y.toFloat(), linePaint)
    y += 18

    entries.forEach { entry ->
        if (y > 780) {
            pdf.finishPage(page)
            page = pdf.startPage(pageInfo)
            canvas = page.canvas
            y = 60
        }

        val noteLines = splitLongText(entry.note.ifBlank { "-" }, rowPaint, 130f)
        canvas.drawText(formatter.format(Date(entry.createdAt)), colDateX, y.toFloat(), rowPaint)
        canvas.drawText(if (entry.type == "gave") "You gave" else "You got", colTypeX, y.toFloat(), rowPaint)
        canvas.drawText("₹${entry.amount}", colAmountX, y.toFloat(), rowPaint)
        noteLines.forEachIndexed { index, line ->
            canvas.drawText(line, colNoteX, y + (index * 16).toFloat(), rowPaint)
        }

        y += maxOf(22, noteLines.size * 16)
        canvas.drawLine(40f, y.toFloat(), 550f, y.toFloat(), linePaint)
        y += 14
    }

    pdf.finishPage(page)

    val fileName = "Flow_${partyName}_${System.currentTimeMillis()}.pdf"
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        resolver.openOutputStream(uri!!).use { output ->
            pdf.writeTo(output)
        }
        pdf.close()
        "Saved to Downloads/$fileName"
    } else {
        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            fileName
        )
        FileOutputStream(file).use { output ->
            pdf.writeTo(output)
        }
        pdf.close()
        "Saved to: ${file.absolutePath}"
    }
}
