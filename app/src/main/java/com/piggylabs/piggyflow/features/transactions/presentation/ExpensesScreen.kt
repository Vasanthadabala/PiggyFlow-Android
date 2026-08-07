package com.piggylabs.piggyflow.features.transactions.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.navigation.Stats
import com.piggylabs.piggyflow.core.navigation.Tracker
import com.piggylabs.piggyflow.core.navigation.Transactions
import com.piggylabs.piggyflow.core.navigation.BottomBar
import com.piggylabs.piggyflow.features.home.presentation.components.CategoryDonutChart
import com.piggylabs.piggyflow.features.home.presentation.components.DonutSlice
import com.piggylabs.piggyflow.features.home.presentation.components.ListComponent
import com.piggylabs.piggyflow.features.home.presentation.components.TransactionUi
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.piggylabs.piggyflow.core.utils.CategoryTotal
import com.piggylabs.piggyflow.core.utils.DateRanges
import com.piggylabs.piggyflow.core.utils.comparisonWindow
import com.piggylabs.piggyflow.core.utils.categoryTotals
import com.piggylabs.piggyflow.core.utils.formatSignedPercent
import com.piggylabs.piggyflow.core.utils.generateTransactionPdf
import com.piggylabs.piggyflow.core.utils.inWindow
import com.piggylabs.piggyflow.core.utils.parseDbDateOrNull
import com.piggylabs.piggyflow.core.utils.percentChange
import com.piggylabs.piggyflow.core.utils.topPayees
import com.piggylabs.piggyflow.core.utils.windowFor
import java.time.LocalDate
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@ExperimentalMaterial3Api
@Composable
fun ExpensesScreen(navController: NavHostController, viewModel: HomeViewModel) {
    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            ExpensesScreenComponent(navController = navController, viewModel = viewModel)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ExpensesScreenComponent(navController: NavHostController, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = appColors()

    val expenses = uiState.expenses
    val today = remember { LocalDate.now() }
    val monthWindow = remember(today) { windowFor(DateRanges.THIS_MONTH, today) }
    val lastMonthWindow = remember(monthWindow, today) {
        comparisonWindow(DateRanges.THIS_MONTH, monthWindow, today)
    }

    val monthExpenses = remember(expenses, monthWindow) { expenses.inWindow(monthWindow) }
    val lastMonthExpenses = remember(expenses, lastMonthWindow) { expenses.inWindow(lastMonthWindow) }
    val totalThisMonth = remember(monthExpenses) { monthExpenses.sumOf { it.amount } }
    val totalLastMonth = remember(lastMonthExpenses) { lastMonthExpenses.sumOf { it.amount } }
    val monthChange = remember(totalThisMonth, totalLastMonth) {
        percentChange(totalThisMonth, totalLastMonth)
    }

    val topCategories = remember(monthExpenses) { categoryTotals(monthExpenses, limit = 6) }
    val topCategory = topCategories.maxByOrNull { it.amount }
    val topMerchants = remember(monthExpenses) { topPayees(monthExpenses, limit = 5) }

    val recentExpenses = remember(expenses) {
        expenses.sortedByDescending { (parseDbDateOrNull(it.date)?.toEpochDay() ?: 0L) * 100_000L + it.id }
            .take(4)
    }

    var isExporting by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. HEADER
        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text2(text = "Expenses", size = 24, weight = FontWeight.Bold, color = colors.text)
                    Text2(
                        text = "Track, manage and analyze your expenses",
                        size = 13,
                        weight = FontWeight.Normal,
                        color = colors.textMuted
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeaderIconButton(
                        icon = Icons.Default.Search,
                        onClick = { navController.navigate(Transactions.route) { launchSingleTop = true } }
                    )
                    HeaderIconButton(
                        icon = Icons.Default.FilterList,
                        onClick = { navController.navigate(Transactions.route) { launchSingleTop = true } }
                    )
                }
            }
        }

        // 2. TOTAL EXPENSES HERO
        item(key = "total_hero") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text2(text = "Total Expenses", size = 13, weight = FontWeight.Normal, color = colors.textMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text2(
                            text = "₹%,.0f".format(totalThisMonth),
                            size = 26,
                            weight = FontWeight.Bold,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text2(text = monthWindow.label, size = 12, weight = FontWeight.Normal, color = colors.textMuted)
                        if (monthChange != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text2(
                                text = "${formatSignedPercent(monthChange)} vs last month",
                                size = 12,
                                weight = FontWeight.SemiBold,
                                color = if (monthChange > 0) colors.negative else colors.positive
                            )
                        }
                    }

                    if (topCategories.isNotEmpty()) {
                        Box(contentAlignment = Alignment.Center) {
                            CategoryDonutChart(
                                slices = topCategories.mapIndexed { index, item ->
                                    DonutSlice(
                                        label = item.name,
                                        value = item.amount,
                                        color = colors.chartPalette[index % colors.chartPalette.size]
                                    )
                                },
                                modifier = Modifier.size(90.dp),
                                strokeWidth = 16.dp,
                                centerContent = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text2(text = "Top Category", size = 8, weight = FontWeight.Normal, color = colors.textMuted)
                                        if (topCategory != null) {
                                            Text2(
                                                text = topCategory.name,
                                                size = 9,
                                                weight = FontWeight.Bold,
                                                color = colors.text,
                                                align = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. QUICK ACTIONS
        item(key = "quick_actions") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickAction(icon = Icons.Default.Insights, label = "View Insights") {
                        navController.navigate(Stats.route) { launchSingleTop = true }
                    }
                    QuickAction(icon = Icons.Default.Description, label = "View Report") {
                        navController.navigate(Stats.route) { launchSingleTop = true }
                    }
                    QuickAction(icon = Icons.Default.Category, label = "Categories") {
                        navController.navigate(Stats.route) { launchSingleTop = true }
                    }
                    QuickAction(icon = Icons.Default.Repeat, label = "Recurring") {
                        navController.navigate(Tracker.route) { launchSingleTop = true }
                    }
                    QuickAction(icon = Icons.Default.FileDownload, label = "Export") {
                        if (isExporting) return@QuickAction
                        isExporting = true
                        runCatching {
                            generateTransactionPdf(
                                context = context,
                                expenses = expenses,
                                income = emptyList(),
                                reportTitle = "Expense Report"
                            )
                        }.onSuccess {
                            Toast.makeText(context, "Saved to Downloads: $it", Toast.LENGTH_LONG).show()
                        }.onFailure {
                            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                        }
                        isExporting = false
                    }
                }
            }
        }

        // 4. RECENT TRANSACTIONS
        item(key = "recent_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text2(text = "Recent Transactions", size = 17, weight = FontWeight.Bold, color = colors.text)
                if (recentExpenses.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            navController.navigate(Transactions.route) { launchSingleTop = true }
                        }
                    ) {
                        Text2(text = "View all", size = 13, weight = FontWeight.SemiBold, color = colors.accent)
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (recentExpenses.isEmpty()) {
            item(key = "recent_empty") {
                EmptyStateCard(
                    icon = Icons.Default.Storefront,
                    title = "No expenses yet",
                    subtitle = "Tap + to record your first expense."
                )
            }
        } else {
            items(recentExpenses, key = { "e${it.id}" }) { expense ->
                ListComponent(navController = navController, transaction = TransactionUi.Expense(expense))
            }
        }

        // 5. EXPENSES BY CATEGORY
        if (topCategories.isNotEmpty()) {
            item(key = "category_header") {
                Text2(text = "Expenses by Category", size = 17, weight = FontWeight.Bold, color = colors.text)
            }

            item(key = "category_grid") {
                val rows = topCategories.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { category ->
                                CategoryTile(category = category, modifier = Modifier.weight(1f))
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 6. TOP MERCHANTS (grouped by the free-text note users add to an expense)
        if (topMerchants.isNotEmpty()) {
            item(key = "merchants_header") {
                Text2(text = "Top Merchants", size = 17, weight = FontWeight.Bold, color = colors.text)
            }

            item(key = "merchants_row") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(topMerchants, key = { it.name }) { merchant ->
                        MerchantTile(merchant = merchant)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

@Composable
private fun Text2(
    text: String,
    size: Int,
    weight: FontWeight,
    color: androidx.compose.ui.graphics.Color,
    align: TextAlign? = null
) {
    androidx.compose.material3.Text(
        text = text,
        fontSize = size.sp,
        fontWeight = weight,
        color = color,
        textAlign = align,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun HeaderIconButton(icon: ImageVector, onClick: () -> Unit) {
    val colors = appColors()
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(colors.surfaceMuted)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.text, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = appColors()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.onAccentSoft, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text2(text = label, size = 10, weight = FontWeight.Medium, color = colors.text, align = TextAlign.Center)
    }
}

@Composable
private fun CategoryTile(category: CategoryTotal, modifier: Modifier = Modifier) {
    val colors = appColors()
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (category.emoji.isNotBlank()) {
                    Text2(text = category.emoji, size = 16, weight = FontWeight.Normal, color = colors.text)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text2(
                    text = category.name,
                    size = 12,
                    weight = FontWeight.SemiBold,
                    color = colors.text
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text2(text = "₹%,.0f".format(category.amount), size = 15, weight = FontWeight.Bold, color = colors.text)
            Text2(text = "%.1f%%".format(category.share), size = 11, weight = FontWeight.Medium, color = colors.textMuted)
        }
    }
}

@Composable
private fun MerchantTile(merchant: CategoryTotal) {
    val colors = appColors()
    Card(
        modifier = Modifier.width(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceMuted),
                contentAlignment = Alignment.Center
            ) {
                Text2(
                    text = merchant.name.trim().firstOrNull()?.uppercase() ?: "?",
                    size = 14,
                    weight = FontWeight.Bold,
                    color = colors.text
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text2(text = merchant.name, size = 12, weight = FontWeight.SemiBold, color = colors.text)
            Spacer(modifier = Modifier.height(2.dp))
            Text2(text = "₹%,.0f".format(merchant.amount), size = 13, weight = FontWeight.Bold, color = colors.text)
            Text2(text = "%.1f%%".format(merchant.share), size = 10, weight = FontWeight.Medium, color = colors.textMuted)
        }
    }
}

@Composable
private fun EmptyStateCard(icon: ImageVector, title: String, subtitle: String) {
    val colors = appColors()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(44.dp), tint = colors.textMuted.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            Text2(text = title, size = 14, weight = FontWeight.SemiBold, color = colors.text)
            Spacer(modifier = Modifier.height(2.dp))
            Text2(text = subtitle, size = 12, weight = FontWeight.Normal, color = colors.textMuted, align = TextAlign.Center)
        }
    }
}
