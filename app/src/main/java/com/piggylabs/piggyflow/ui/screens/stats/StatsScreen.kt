package com.piggylabs.piggyflow.ui.screens.stats

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.data.local.entity.ExpenseEntity
import com.piggylabs.piggyflow.data.local.entity.IncomeEntity
import com.piggylabs.piggyflow.ui.components.utils.generateTransactionPdf
import com.piggylabs.piggyflow.ui.navigation.components.BottomBar
import com.piggylabs.piggyflow.ui.screens.home.components.formatDateForUI
import com.piggylabs.piggyflow.ui.screens.home.details.limit
import com.piggylabs.piggyflow.ui.screens.home.viewmodel.HomeViewModel
import com.piggylabs.piggyflow.ui.theme.appColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class TransactionUI(
    val id: Int,
    val amount: Double,
    val date: String,
    val note: String,
    val categoryName: String,
    val categoryEmoji: String,
    val type: String // "Income" OR "Expense"
)


@ExperimentalMaterial3Api
@Composable
fun StatsScreen(navController: NavHostController, viewModel: HomeViewModel){
    Scaffold(
        bottomBar = { BottomBar( navController = navController) }
    ) { innerPadding->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ){
            StatsScreenComponent(viewModel = viewModel)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun StatsScreenComponent(viewModel: HomeViewModel) {

    //Bottom Sheet
    var showSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val currentMonthIndex = remember {
        java.time.LocalDate.now().monthValue - 1
    }

    var selectedMonthIndex by remember {
        mutableStateOf(currentMonthIndex)
    }

    val currentYear = remember {
        java.time.LocalDate.now().year
    }

    // Income
    val selectedMonthIncome = viewModel.income.filter { income ->
        val incomeDate = java.time.LocalDate.parse(income.date)
        incomeDate.monthValue - 1 == selectedMonthIndex &&
                incomeDate.year == currentYear
    }

    //Expense
    val selectedMonthExpenses = viewModel.expenses.filter { expense ->
        val expenseDate = java.time.LocalDate.parse(expense.date)
        expenseDate.monthValue - 1 == selectedMonthIndex &&
                expenseDate.year == currentYear
    }

    val groupedTopExpenses = selectedMonthExpenses
        .groupBy { it.categoryName }
        .map { entry ->
            val total = entry.value.sumOf { it.amount }
            val emoji = entry.value.first().categoryEmoji

            ExpenseEntity(
                id = 0, // dummy
                amount = total,
                note = "",
                date = entry.value.first().date,
                categoryName = entry.key,
                categoryEmoji = emoji,
                categoryType = "Expense"
            )
        }
        .sortedByDescending { it.amount }
        .take(5)

    val totalTopExpenseAmount = groupedTopExpenses.sumOf { it.amount }

    //Balance Card
    val totalIncome = selectedMonthIncome.sumOf { it.amount }

    val totalExpense = selectedMonthExpenses.sumOf { it.amount }

    val leftBalance = totalIncome - totalExpense


    val daysInMonth = java.time.YearMonth
        .of(currentYear, selectedMonthIndex + 1)
        .lengthOfMonth()

    val monthlyGraphData = (1..daysInMonth).map { day ->
        selectedMonthExpenses
            .filter {
                java.time.LocalDate.parse(it.date).dayOfMonth == day
            }
            .sumOf { it.amount }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
    ) {
        // ✅ Month Selector
        MonthPager(
            selectedMonthIndex = selectedMonthIndex,
            onMonthChange = { selectedMonthIndex = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
        ) {

            Column() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors().container),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowOutward,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .rotate(180f),
                                    tint = appColors().green
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "Income",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = appColors().text
                                )
                            }

                            Text(
                                text = "₹ %.2f".format(totalIncome),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors().text
                            )
                        }
                    }


                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = appColors().container
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowOutward,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .size(16.dp),
                                    tint = appColors().red.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "Expense",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = appColors().text
                                )
                            }

                            Text(
                                text = "₹ %.2f".format(totalExpense),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors().text
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                Card(
                    modifier = Modifier
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = appColors().container
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Balance,
                                contentDescription = "",
                                modifier = Modifier
                                    .size(16.dp),
                                tint = appColors().green
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "Balance",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = appColors().text
                            )
                        }

                        Text(
                            text = "₹ %.2f".format(leftBalance),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors().text
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (groupedTopExpenses.isEmpty()){
                Spacer(modifier = Modifier.height(128.dp))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "",
                        modifier = Modifier
                            .size(64.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No transactions yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W500,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            } else {

                // ✅ Graph Card
                Text(
                    text = "Monthly Spending Trend",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors().text
                )

                Spacer(modifier = Modifier.height(12.dp))

                LineChart(data = monthlyGraphData)

                Spacer(modifier = Modifier.height(28.dp))

                // ✅ Top Expenses Header
                Text(
                    text = "Top Spending Categories",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors().text
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ✅ Top Expenses List
//        LazyColumn {
//            items(topExpenses) { expense ->
//                TopExpenseItem(
//                    expense = expense,
//                    totalAmount = totalTopExpenseAmount
//                )
//            }
//        }

                Column {
                    groupedTopExpenses.forEach { expense ->
                        TopExpenseItem(
                            expense = expense,
                            totalAmount = totalTopExpenseAmount,
                            onClick = {
                                selectedCategory = expense.categoryName
                                showSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSheet && selectedCategory != null) {
        TransactionBottomSheet(
            categoryName = selectedCategory!!,
            incomes = viewModel.income,
            expenses = viewModel.expenses,
            onDismiss = { showSheet = false }
        )
    }
}

@Composable
fun MonthPager(
    selectedMonthIndex: Int,
    onMonthChange: (Int) -> Unit
) {
    val months = listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )

    val currentYear = remember {
        java.time.LocalDate.now().year
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                color = appColors().container,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp,horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Default.ArrowBackIosNew,
            contentDescription = "",
            tint = appColors().text,
            modifier = Modifier
                .size(24.dp)
                .clickable{
                    if (selectedMonthIndex > 0)
                        onMonthChange(selectedMonthIndex - 1)
                }
        )

        // ✅ Current Month + Year
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = months[selectedMonthIndex],
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = appColors().text
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = currentYear.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = appColors().text
            )
        }

        Icon(
            imageVector = Icons.Default.ArrowBackIosNew,
            contentDescription = "",
            tint = appColors().text,
            modifier = Modifier
                .size(24.dp)
                .clickable{
                    if (selectedMonthIndex < 11)
                        onMonthChange(selectedMonthIndex + 1)
                }
                .rotate(180f)
        )
    }
}

@Composable
fun LineChart(
    data: List<Double>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(220.dp)
) {
    val isDark = isSystemInDarkTheme()

    if (data.size < 2) return

    val maxValue = data.maxOrNull() ?: 1.0
    val minValue = data.minOrNull() ?: 0.0
    val graphColor = Color(0xFFD32F2F)

    val isElevated by remember { mutableStateOf(true) }

    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isElevated) 1.02f else 1f,
        animationSpec = androidx.compose.animation.core.tween(600)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                shape = RoundedCornerShape(22.dp)
                clip = false
            }
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = appColors().container
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Canvas(
                modifier = modifier
                    .padding(16.dp)
                    .background(
                        color = Color.Transparent
                    )
            ) {
                val spacingX = size.width / (data.size - 1)
                val height = size.height

                val points = data.mapIndexed { index, value ->
                    Offset(
                        x = index * spacingX,
                        y = (height - ((value - minValue) / (maxValue - minValue)) * height).toFloat()
                    )
                }

                /* ✅ GRID LINES */
                repeat(5) { i ->
                    val y = size.height * i / 4
                    drawLine(
                        color = if(isDark) Color.LightGray.copy(alpha = 0.3f) else Color.Gray.copy(0.6f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                /* ✅ CURVE PATH */
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        quadraticBezierTo(
                            (points[i - 1].x + points[i].x) / 2,
                            points[i - 1].y,
                            points[i].x,
                            points[i].y
                        )
                    }
                }

                /* ✅ FILL GRADIENT */
                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    addPath(path)
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            graphColor.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )

                /* ✅ DRAW MAIN CURVE */
                drawPath(
                    path = path,
                    color = graphColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 5f,
                        cap = StrokeCap.Round
                    )
                )

                /* ✅ DRAW POINT DOTS */
                points.forEachIndexed { index, point ->
                    drawCircle(
                        color = if (index == points.lastIndex) graphColor else Color.White,
                        radius = if (index == points.lastIndex) 10f else 7f,
                        center = point
                    )

                    drawCircle(
                        color = graphColor,
                        radius = if (index == points.lastIndex) 6f else 4f,
                        center = point
                    )
                }
            }
        }
    }
}


@Composable
fun TopExpenseItem(expense: ExpenseEntity, totalAmount: Double, onClick: () -> Unit) {

    val percentage =
        if (totalAmount == 0.0) 0
        else ((expense.amount / totalAmount) * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 4.dp)
            .clickable {
                onClick()   // 🔥 trigger sheet
            },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (expense.categoryEmoji.isBlank()){
                        Text(
                            text = expense.categoryName?.trim()?.firstOrNull()?.uppercase() ?: "",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = expense.categoryEmoji,
                            fontSize = 24.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column{
                    Text(
                        text = expense.categoryName,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().text
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.LightGray.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = percentage / 100f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Red)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹ ${expense.amount}",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500,
                    color = appColors().text
                )

                Text(
                    text = "$percentage%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = Color.Gray
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun TransactionBottomSheet(
    categoryName: String,
    incomes: List<IncomeEntity>,
    expenses: List<ExpenseEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Combine income + expense of same category
    val allTransactions = remember(categoryName) {

        val incomeTx = incomes
            .filter { it.categoryName == categoryName }
            .map {
                TransactionUI(
                    id = it.id,
                    amount = it.amount,
                    date = it.date,
                    note = it.note,
                    categoryName = it.categoryName,
                    categoryEmoji = "💰",
                    type = "Income"
                )
            }

        val expenseTx = expenses
            .filter { it.categoryName == categoryName }
            .map {
                TransactionUI(
                    id = it.id,
                    amount = it.amount,
                    date = it.date,
                    note = it.note,
                    categoryName = it.categoryName,
                    categoryEmoji = it.categoryEmoji,
                    type = "Expense"
                )
            }

        // merge + sort
        (incomeTx + expenseTx).sortedByDescending { it.date }
    }

    val incomeTx = incomes
        .filter { it.categoryName == categoryName }
        .map {
            TransactionUI(
                id = it.id,
                amount = it.amount,
                date = it.date,
                note = it.note,
                categoryName = it.categoryName,
                categoryEmoji = "💰",
                type = "Income"
            )
        }.sortedByDescending { it.date }

    val expenseTx = expenses
        .filter { it.categoryName == categoryName }
        .map {
            TransactionUI(
                id = it.id,
                amount = it.amount,
                date = it.date,
                note = it.note,
                categoryName = it.categoryName,
                categoryEmoji = it.categoryEmoji,
                type = "Expense"
            )
        }.sortedByDescending { it.date }

    val incomeTotal = incomeTx.filter { it.type == "Income" }.sumOf { it.amount }
    val expenseTotal = expenseTx.filter { it.type == "Expense" }.sumOf { it.amount }

    val leftAmount = incomeTotal - expenseTotal

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = appColors().background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .clickable {
                            scope.launch {
                                sheetState.hide()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = categoryName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = appColors().text,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Divider(
                color = Color.LightGray,
                thickness = 0.6.dp,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 0.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            if(incomeTotal > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Text(
                            text = "Amount: ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                        Text(
                            text = "$incomeTotal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors().text
                        )
                    }

                    Row {
                        Text(
                            text = "Available: ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.Gray
                        )
                        Text(
                            text = "$leftAmount",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors().text
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (incomeTx.isEmpty() && expenseTx.isEmpty()) {
                Text(
                    text = "No transactions found",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "Expenses",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().text
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        items(expenseTx) { item ->
                            TransactionItem(item)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Incomes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().text
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        items(incomeTx) { item ->
                            TransactionItem(item)
                        }
                    }
                }
            }

            Button(
                onClick = {
                    isLoading = true

                    scope.launch {
                        try{
                            val resultMessage = generateTransactionPdf(
                                context = context,
                                categoryName = categoryName,
                                transactions = allTransactions
                            )

                            Toast.makeText(context, resultMessage, Toast.LENGTH_LONG).show()
                        } catch (e: Exception){
                            Log.e("StatsScreen", "Error: $e")
                        } finally {
                            delay(500L)
                            isLoading = false
                        }
                    }
                },
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 1.dp,
                    pressedElevation = 5.dp,
                ),
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors().green,
                    contentColor = Color.White
                ),
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    // Show a loading spinner when the button is disabled
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Export Pdf",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W500,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(txn: TransactionUI) {
    val isExpense = txn.type == "Expense"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isExpense) Color.Red.copy(alpha = 0.15f) else Color(
                                0xFF27C152
                            ).copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    if (txn.categoryEmoji.isBlank()) {
                        Text(
                            text = txn.categoryName?.trim()?.firstOrNull()?.uppercase() ?: "",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = txn.categoryEmoji,
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = txn.note.ifBlank { txn.categoryName }.limit(16),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().text
                    )

                    Text(
                        text = formatDateForUI(txn.date),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(isExpense) {
                    Text(
                        text = "-",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().red
                    )
                }else{
                    Text(
                        text = "+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().green
                    )
                }
                Text(
                    text = "₹${txn.amount}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (txn.type == "Income") appColors().green else appColors().red
                )
            }
        }
    }
}