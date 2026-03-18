package com.piggylabs.piggyflow.ui.screens.personal.home

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.navigation.AddData
import com.piggylabs.piggyflow.navigation.Notification
import com.piggylabs.piggyflow.navigation.Profile
import com.piggylabs.piggyflow.navigation.components.BottomBar
import com.piggylabs.piggyflow.ui.screens.common.notification.getClearedTrackerNotificationKeys
import com.piggylabs.piggyflow.ui.screens.personal.home.components.ListComponent
import com.piggylabs.piggyflow.ui.screens.personal.home.components.TransactionUi
import com.piggylabs.piggyflow.ui.screens.personal.home.viewmodel.HomeViewModel
import com.piggylabs.piggyflow.ui.screens.common.notification.calculateTrackerReminderCount
import com.piggylabs.piggyflow.ui.screens.common.tracker.viewmodel.TrackerViewModel
import com.piggylabs.piggyflow.ui.theme.appColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

enum class Category(val label: String) {
    FOOD("🍔 Food"),
    HOME("🏠 Home"),
    GROCERIES("🛒 Groceries"),
    TRANSPORT("🚌 Transport"),
    ENTERTAINMENT("🎉 Entertainment"),
    DRINKS("🍹 Drinks"),
    SHOPPING("🛍️ Shopping"),
    POWER_BILL("💡 Power Bill"),
    PHONE("📱 Phone"),
    INTERNET("🌐 Internet"),
    FUEL("⛽ Fuel"),
    SALARY("💼 Salary"),
    BUSINESS("🏢 Business"),
    FREELANCE("🧑‍💻 Freelance"),
    INVESTMENTS("📈 Investments"),
    RENTAL("🏠 Rental Income"),
    INTEREST("💰 Interest"),
    BONUS("🎁 Bonus"),
    GIFTS("🎉 Gifts"),
    REFUND("🔄 Refund"),
    OTHERS("🔖 Others");

    val emoji:String
        get() = label.substringBefore(" ")

    val categoryName: String
        get() = label.substringAfter(" ")
}

fun isSameDay(date1: LocalDate, date2: LocalDate): Boolean {
    return date1 == date2
}

fun isSameWeek(date: LocalDate, today: LocalDate): Boolean {
    val week1 = date.get(WeekFields.ISO.weekOfWeekBasedYear())
    val week2 = today.get(WeekFields.ISO.weekOfWeekBasedYear())
    return week1 == week2 && date.year == today.year
}

fun isSameMonth(date: LocalDate, today: LocalDate): Boolean {
    return date.month == today.month && date.year == today.year
}

fun parseDbDate(dbDate: String): LocalDate {
    return LocalDate.parse(dbDate, DateTimeFormatter.ISO_DATE)
}


@ExperimentalMaterial3Api
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel){

    Scaffold(
        bottomBar = {BottomBar(navController = navController)}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)

        ){
            HomeScreenComponent(
                navController = navController,
                viewModel = viewModel
            )

            // Floating Action Button positioned at the bottom-right corner
            FloatingActionButton(
                onClick = {
                    navController.navigate(AddData.route){
                        launchSingleTop = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Aligning at the bottom-right corner
                    .padding(bottom = 20.dp, end = 20.dp),
                containerColor = appColors().green,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Data",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun HomeScreenComponent(navController: NavHostController, viewModel: HomeViewModel){
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val keyboardController = LocalSoftwareKeyboardController.current
    val trackerViewModel: TrackerViewModel = viewModel()

    val sharedPreferences = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)

    val userName = sharedPreferences.getString("userName","Guest")
    val greetings by remember { mutableStateOf( getGreetingByTime()) }
    val reminderCount = calculateTrackerReminderCount(
        trackerViewModel.subscriptions,
        getClearedTrackerNotificationKeys(context)
    )

    //Search and Filter
    var search by remember{ mutableStateOf("") }

    val options = listOf("Day", "Week", "Month")
    var selectedOption by remember { mutableStateOf("Month") }
    var optionsExpanded by remember { mutableStateOf(false) }

    //Date picker
    var showDatePicker by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            date = LocalDate.of(year, month + 1, dayOfMonth)
            selectedOption = "Month" //Auto Switch filter to month
            showDatePicker = false
        },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth
    ).apply {
        setOnDismissListener {
            showDatePicker = false
        }
    }

    //transactions
    val transactions = remember(viewModel.expenses, viewModel.income) {
        val expenseList = viewModel.expenses.map { TransactionUi.Expense(it) }
        val incomeList = viewModel.income.map { TransactionUi.Income(it) }

        (expenseList + incomeList)
            .sortedByDescending {
                when (it) {
                    is TransactionUi.Expense -> it.data.id
                    is TransactionUi.Income -> it.data.id
                }
            }
    }

    val filteredTransactions = remember(
        transactions,
        selectedOption,
        search,
        date
    ) {

        val referenceDate = date

        transactions
            // ✅ Date Filter
            .filter { txn ->
                val dateStr = when (txn) {
                    is TransactionUi.Expense -> txn.data.date
                    is TransactionUi.Income -> txn.data.date
                }

                val txnDate = parseDbDate(dateStr)

                when (selectedOption) {
                    "Day" -> isSameDay(txnDate, referenceDate)
                    "Week" -> isSameWeek(txnDate, referenceDate)
                    "Month" -> isSameMonth(txnDate, referenceDate)
                    else -> true
                }
            }

            // ✅ Search Filter
            .filter { txn ->
                if (search.isBlank()) return@filter true

                val text = when (txn) {
                    is TransactionUi.Expense ->
                        "${txn.data.categoryName} ${txn.data.note}"

                    is TransactionUi.Income ->
                        "${txn.data.categoryType} ${txn.data.note}"
                }

                text.contains(search, ignoreCase = true)
            }

            // ✅ Final Sort by Date (latest first)
            .sortedByDescending { txn ->
                val dateStr = when (txn) {
                    is TransactionUi.Expense -> txn.data.date
                    is TransactionUi.Income -> txn.data.date
                }
                parseDbDate(dateStr)
            }
    }

    val filteredIncome = filteredTransactions
        .filterIsInstance<TransactionUi.Income>()
        .sumOf { it.data.amount }

    val filteredExpense = filteredTransactions
        .filterIsInstance<TransactionUi.Expense>()
        .sumOf { it.data.amount }

    val filteredBalance = filteredIncome - filteredExpense
    val totalIncome = filteredIncome
    val totalExpense = filteredExpense
    val leftBalance = filteredBalance
    val hasActiveFilters = search.isNotBlank() || selectedOption != "Month" || date != LocalDate.now()
    val periodLabel = when (selectedOption) {
        "Day" -> "Selected day"
        "Week" -> "Selected week"
        else -> "Selected month"
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {
            Row{
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(appColors().green)
                        .clickable{
                            navController.navigate(Profile.route){
                                launchSingleTop = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName?.trim()?.firstOrNull()?.uppercase() ?: "",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy((-4).dp)
                ) {
                    Text(
                        text = greetings,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = appColors().text
                    )
                    Text(
                        text = "$userName",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500,
                        color = appColors().text
                    )
                }
            }

            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(appColors().container)
                        .clickable {
                            navController.navigate(Notification.route) {
                                launchSingleTop = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "",
                        modifier = Modifier.size(24.dp),
                        tint = appColors().text
                    )
                }

                if (reminderCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(appColors().red),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reminderCount.coerceAtMost(99).toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ){
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (isDark) {
                                    listOf(
                                        Color(0xFF0B3D2E),
                                        Color(0xFF14532D)
                                    )
                                } else {
                                    listOf(
                                        Color(0xFF0B3D2E),
                                        Color(0xFF166534)
                                    )
                                }
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp, horizontal = 18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "Overview",
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "₹ %.2f".format(leftBalance),
                                    textAlign = TextAlign.Center,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = periodLabel,
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = date.format(dateFormatter),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            HomeMetricText(
                                modifier = Modifier.weight(1f),
                                label = "Income",
                                amount = "₹ %.2f".format(totalIncome),
                                accent = appColors().green
                            )
                            HomeMetricText(
                                modifier = Modifier.weight(1f),
                                label = "Spent",
                                amount = "₹ %.2f".format(totalExpense),
                                accent = appColors().red
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                ) {
                    OutlinedTextField(
                        value = search,
                        singleLine = true,
                        onValueChange = { search = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = {
                            Text(
                                text = "Search category or note",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W500,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Start
                                )
                            )
                        },
                        shape = RoundedCornerShape(20),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = appColors().container,
                            unfocusedContainerColor = appColors().container,
                            cursorColor = appColors().text
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done // Use Done for the last field
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        leadingIcon = {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Gray
                                )
                            }
                        },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.W500,
                            fontSize = 16.sp,
                            color = appColors().text
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(0.35f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(appColors().container)
                            .clickable { optionsExpanded = true }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedOption,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = appColors().text
                            )

                            Icon(
                                imageVector = if (optionsExpanded)
                                    Icons.Default.KeyboardArrowUp
                                else
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = appColors().text
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = optionsExpanded,
                        onDismissRequest = { optionsExpanded = false },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = if (isDark) Color.Black else Color.LightGray,
                        tonalElevation = 2.dp,
                        shadowElevation = 4.dp
                    ) {
                        options.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = appColors().text
                                    )
                                },
                                onClick = {
                                    selectedOption = option
                                    optionsExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (hasActiveFilters) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChipCard(text = selectedOption, accent = appColors().green)
                    FilterChipCard(text = date.format(dateFormatter), accent = appColors().blue)
                    if (search.isNotBlank()) {
                        FilterChipCard(text = "\"$search\"", accent = appColors().red)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Calendar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = appColors().text
                    )

                    Card(
                        modifier = Modifier
                            .clickable {
                                if (!showDatePicker) {  // Only open if not already showing
                                    showDatePicker = true
                                    datePickerDialog.show()
                                }
                            },

                        colors = CardDefaults.cardColors(
                            containerColor = appColors().container
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = date.format(dateFormatter),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W600,
                                color = appColors().text
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date",
                                tint = appColors().text,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (transactions.isEmpty()){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "",
                        modifier = Modifier
                            .size(128.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No transactions yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W500,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tap the + button to add your first expense or income.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            } else if (filteredTransactions.isEmpty()) {
                HomeEmptyState(
                    title = "No transactions found",
                    message = "Try a different period, date, or search term."
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors().text
                    )
                    Text(
                        text = "${filteredTransactions.size} items",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(filteredTransactions.size) { index ->
                        ListComponent(navController = navController, transaction = filteredTransactions[index])
                    }
                }
            }

        }
    }
}

fun getGreetingByTime(): String {
    val hour = LocalTime.now().hour

    return when (hour) {
        in 5..11 -> "Good Morning,"
        in 12..16 -> "Good Afternoon,"
        in 17..20 -> "Good Evening,"
        else -> "Good Night,"
    }
}

@Composable
private fun HomeMetricText(
    modifier: Modifier = Modifier,
    label: String,
    amount: String,
    accent: Color
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = accent,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (label == "Spent") Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = amount,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FilterChipCard(text: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = appColors().text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HomeEmptyState(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Storefront,
            contentDescription = null,
            modifier = Modifier.size(128.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.W500,
            color = Color.Gray.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
