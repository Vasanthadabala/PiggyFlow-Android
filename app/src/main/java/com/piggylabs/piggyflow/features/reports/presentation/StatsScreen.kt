package com.piggylabs.piggyflow.features.reports.presentation

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.navigation.BottomBar
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.piggylabs.piggyflow.core.utils.generateTransactionPdf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.piggylabs.piggyflow.features.reports.presentation.components.DualCashFlowBarChart
import com.piggylabs.piggyflow.features.reports.presentation.components.ReportCategoryItem
import com.piggylabs.piggyflow.features.reports.presentation.components.ReportsCategoryDonutChart

@ExperimentalMaterial3Api
@Composable
fun StatsScreen(navController: NavHostController, viewModel: HomeViewModel) {
    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            ReportsScreenComponent(navController = navController, viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreenComponent(navController: NavHostController, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val dateRanges = listOf("Today", "Yesterday", "This Week", "This Month", "Last Month", "Custom")
    var selectedRange by remember { mutableStateOf("This Month") }

    val categoriesList = listOf(
        ReportCategoryItem("Food & Dining", 32450.0, 26.9f, Color(0xFF15803D)),
        ReportCategoryItem("Shopping", 22300.0, 18.5f, Color(0xFF3B82F6)),
        ReportCategoryItem("Fuel", 18650.0, 15.5f, Color(0xFF8B5CF6)),
        ReportCategoryItem("Utilities", 12450.0, 10.3f, Color(0xFFF97316)),
        ReportCategoryItem("Entertainment", 8900.0, 7.4f, Color(0xFFEF4444)),
        ReportCategoryItem("Travel", 7850.0, 6.5f, Color(0xFFB45309)),
        ReportCategoryItem("Others", 17840.0, 14.9f, Color(0xFF6B7280))
    )

    val topMerchants = listOf(
        Triple("Amazon", "₹12,450", Color(0xFFFF9900)),
        Triple("Zomato", "₹7,890", Color(0xFFCB202D)),
        Triple("Reliance BP", "₹6,250", Color(0xFF004B93)),
        Triple("Swiggy", "₹5,430", Color(0xFFFC8019)),
        Triple("DMart", "₹4,980", Color(0xFF008000))
    )

    val totalExpense = 120440.0
    val totalIncome = 245000.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Reports",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            text = "Insights into your spending and income",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9))
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9))
                            .clickable { generateTransactionPdf(context, uiState.expenses, uiState.income) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors().background)
            )
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. SELECT DATE RANGE
            item {
                Column {
                    Text(
                        text = "Select Date Range",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors().text
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(dateRanges) { range ->
                            val isSelected = selectedRange == range
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF15803D) else appColors().container,
                                modifier = Modifier.clickable { selectedRange = range }
                            ) {
                                Text(
                                    text = range,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else appColors().text,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors().container),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "May 1 – May 31, 2025",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors().text
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. SUMMARY METRICS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors().container),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Summary for May 2025",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors().text
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "vs Apr 1 – Apr 30, 2025",
                                    fontSize = 10.sp,
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryMetricBlock(
                                label = "Total Income",
                                amount = "₹2,45,000",
                                pct = "▲ 12.8%",
                                icon = Icons.Default.ArrowUpward,
                                iconBg = Color(0xFFE8F5E9),
                                iconColor = Color(0xFF15803D),
                                pctColor = Color(0xFF15803D),
                                modifier = Modifier.weight(1f)
                            )
                            SummaryMetricBlock(
                                label = "Total Expenses",
                                amount = "₹1,20,440",
                                pct = "▲ 8.5%",
                                icon = Icons.Default.ArrowDownward,
                                iconBg = Color(0xFFFEE2E2),
                                iconColor = Color(0xFFEF4444),
                                pctColor = Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                            SummaryMetricBlock(
                                label = "Net Balance",
                                amount = "₹1,24,560",
                                pct = "▲ 18.6%",
                                icon = Icons.Default.AccountBalanceWallet,
                                iconBg = Color(0xFFE8F5E9),
                                iconColor = Color(0xFF15803D),
                                pctColor = Color(0xFF15803D),
                                modifier = Modifier.weight(1f)
                            )
                            SummaryMetricBlock(
                                label = "Savings Rate",
                                amount = "50.7%",
                                pct = "▲ 5.3%",
                                icon = Icons.Default.PieChart,
                                iconBg = Color(0xFFE8F5E9),
                                iconColor = Color(0xFF15803D),
                                pctColor = Color(0xFF15803D),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. SPENDING OVERVIEW
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors().container),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spending Overview",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors().text
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { }
                            ) {
                                Text(
                                    text = "View by: Category",
                                    fontSize = 11.sp,
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Left: Donut + legend
                            Column(modifier = Modifier.weight(0.52f)) {
                                Text(
                                    text = "Category-wise Spending",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors().text
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(contentAlignment = Alignment.Center) {
                                        ReportsCategoryDonutChart(
                                            categories = categoriesList,
                                            totalAmount = totalExpense,
                                            modifier = Modifier.size(90.dp)
                                        )
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "Total", fontSize = 8.sp, color = Color.Gray)
                                            Text(
                                                text = "₹1,20,440",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = appColors().text
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        categoriesList.forEach { item ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    Box(
                                                        modifier = Modifier.size(6.dp).clip(CircleShape).background(item.color)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(text = item.name, fontSize = 8.5.sp, color = appColors().text, maxLines = 1)
                                                }
                                                Text(
                                                    text = "%.1f%%".format(item.percentage),
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = appColors().text
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Right: Merchant-wise
                            Column(modifier = Modifier.weight(0.48f)) {
                                Text(
                                    text = "Merchant-wise (Top 5)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors().text
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    topMerchants.forEach { (name, amount, color) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(26.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(color),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = name.take(1),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = name, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = appColors().text)
                                            }
                                            Text(text = amount, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = appColors().text)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "View All Merchants",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF15803D),
                                    modifier = Modifier.clickable { }
                                )
                            }
                        }
                    }
                }
            }

            // 4. CASH FLOW OVERVIEW
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors().container),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Cash Flow Overview",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors().text
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(0.55f)) {
                                Text(
                                    text = "Income vs Expense",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors().text
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                // Y-axis labels
                                val yLabels = listOf("₹30k", "₹20k", "₹10k", "₹0")
                                Column {
                                    yLabels.take(3).forEach { label ->
                                        Text(text = label, fontSize = 7.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(18.dp))
                                    }
                                    Text(text = yLabels.last(), fontSize = 7.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                DualCashFlowBarChart(modifier = Modifier.fillMaxWidth().height(90.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf("1 May", "8 May", "15 May", "22 May", "31 May").forEach { label ->
                                        Text(text = label, fontSize = 7.sp, color = Color.Gray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF15803D)))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Income", fontSize = 9.sp, color = Color.Gray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Expenses", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(0.45f)) {
                                Text(
                                    text = "Daily Average",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors().text
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(text = "Income", fontSize = 9.sp, color = Color.Gray)
                                        Text(text = "₹7,903", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                    }
                                    Column {
                                        Text(text = "Expenses", fontSize = 9.sp, color = Color.Gray)
                                        Text(text = "₹3,885", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(text = "Highest Expense", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = appColors().text)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "May 18, 2025", fontSize = 10.sp, color = Color.Gray)
                                    Text(text = "₹8,560", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = appColors().text)
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = "Lowest Expense", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = appColors().text)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "May 5, 2025", fontSize = 10.sp, color = Color.Gray)
                                    Text(text = "₹1,240", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = appColors().text)
                                }
                            }
                        }
                    }
                }
            }

            // 5. REPORTS SECTION
            item {
                Column {
                    Text(text = "Reports", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = appColors().text)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ReportTypeCard(
                            title = "Date-wise Report",
                            subtitle = "View day-by-day breakdown",
                            modifier = Modifier.weight(1f)
                        )
                        ReportTypeCard(
                            title = "Week-wise Report",
                            subtitle = "View week-by-week analysis",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ReportTypeCard(
                            title = "Month-wise Report",
                            subtitle = "Detailed monthly analysis",
                            modifier = Modifier.weight(1f)
                        )
                        ReportTypeCard(
                            title = "Year-wise Report",
                            subtitle = "Yearly spending summary",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    ReportTypeCard(
                        title = "Custom Report",
                        subtitle = "Select date range and filters",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 6. EXPORT SECTION
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFC8E6C9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Export Your Report",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                                Text(
                                    text = "Download report in multiple formats",
                                    fontSize = 10.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ExportChip(
                                label = "PDF",
                                icon = Icons.Default.PictureAsPdf,
                                onClick = { generateTransactionPdf(context, uiState.expenses, uiState.income) }
                            )
                            ExportChip(
                                label = "Excel",
                                icon = Icons.Default.TableChart,
                                onClick = { Toast.makeText(context, "Exporting Excel...", Toast.LENGTH_SHORT).show() }
                            )
                            ExportChip(
                                label = "CSV",
                                icon = Icons.Default.FileDownload,
                                onClick = { Toast.makeText(context, "Exporting CSV...", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }
}

@Composable
private fun SummaryMetricBlock(
    label: String,
    amount: String,
    pct: String,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    pctColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = amount,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = appColors().text,
            textAlign = TextAlign.Center
        )
        Text(
            text = pct,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = pctColor
        )
    }
}

@Composable
private fun ReportTypeCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable { },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = appColors().text)
                    Text(text = subtitle, fontSize = 9.sp, color = Color.Gray)
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ExportChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF15803D),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
        }
    }
}
