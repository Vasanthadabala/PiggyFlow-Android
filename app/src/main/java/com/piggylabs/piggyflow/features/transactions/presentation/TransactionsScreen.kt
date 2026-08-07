package com.piggylabs.piggyflow.features.transactions.presentation

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.navigation.AddExpense
import com.piggylabs.piggyflow.features.home.presentation.components.ListComponent
import com.piggylabs.piggyflow.features.home.presentation.components.TransactionUi
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.piggylabs.piggyflow.core.utils.DateRanges
import com.piggylabs.piggyflow.core.utils.parseDbDateOrNull
import com.piggylabs.piggyflow.core.utils.windowFor
import java.time.LocalDate
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val ALL_TIME = "All Time"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(navController: NavHostController, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val colors = appColors()

    var search by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    var selectedRange by rememberSaveable { mutableStateOf(ALL_TIME) }

    val filterTypes = listOf("All", "Expenses", "Income")
    val ranges = remember { listOf(ALL_TIME) + DateRanges.all }

    val today = remember { LocalDate.now() }

    val transactions = remember(uiState.expenses, uiState.income) {
        val expenseList = uiState.expenses.map { TransactionUi.Expense(it) }
        val incomeList = uiState.income.map { TransactionUi.Income(it) }

        (expenseList + incomeList).sortedByDescending { txn ->
            val (date, id) = when (txn) {
                is TransactionUi.Expense -> txn.data.date to txn.data.id
                is TransactionUi.Income -> txn.data.date to txn.data.id
            }
            (parseDbDateOrNull(date)?.toEpochDay() ?: 0L) * 100_000L + id
        }
    }

    val filteredTransactions = remember(transactions, search, selectedFilter, selectedRange, today) {
        val window = if (selectedRange == ALL_TIME) null else windowFor(selectedRange, today)

        transactions
            .filter { txn ->
                when (selectedFilter) {
                    "Expenses" -> txn is TransactionUi.Expense
                    "Income" -> txn is TransactionUi.Income
                    else -> true
                }
            }
            .filter { txn ->
                if (window == null) return@filter true
                val date = when (txn) {
                    is TransactionUi.Expense -> parseDbDateOrNull(txn.data.date)
                    is TransactionUi.Income -> parseDbDateOrNull(txn.data.date)
                }
                date != null && window.contains(date)
            }
            .filter { txn ->
                if (search.isBlank()) return@filter true
                // Same searchable fields for both kinds, so a term matches consistently.
                val text = when (txn) {
                    is TransactionUi.Expense -> with(txn.data) {
                        "$categoryName $categoryType $note $amount $date"
                    }
                    is TransactionUi.Income -> with(txn.data) {
                        "$categoryName $categoryType $note $amount $date"
                    }
                }
                text.contains(search.trim(), ignoreCase = true)
            }
    }

    val totalIncome = filteredTransactions.filterIsInstance<TransactionUi.Income>()
        .sumOf { it.data.amount }
    val totalExpense = filteredTransactions.filterIsInstance<TransactionUi.Expense>()
        .sumOf { it.data.amount }
    val netBalance = totalIncome - totalExpense
    val isFiltered = search.isNotBlank() || selectedFilter != "All" || selectedRange != ALL_TIME

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "All Transactions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(AddExpense.route) },
                containerColor = colors.accent,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. SEARCH
            item(key = "search") {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    placeholder = {
                        Text(
                            text = "Search category, note, or amount",
                            fontSize = 14.sp,
                            color = colors.textMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (search.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = colors.textMuted,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { search = "" }
                            )
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(color = colors.text, fontSize = 15.sp),
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface,
                        cursorColor = colors.accent,
                        focusedTextColor = colors.text,
                        unfocusedTextColor = colors.text
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                )
            }

            // 2. TYPE FILTER
            item(key = "type_filter") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filterTypes, key = { it }) { type ->
                        FilterChip(
                            label = type,
                            isSelected = selectedFilter == type,
                            onClick = { selectedFilter = type }
                        )
                    }
                }
            }

            // 3. DATE RANGE FILTER
            item(key = "range_filter") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ranges, key = { it }) { range ->
                        FilterChip(
                            label = range,
                            isSelected = selectedRange == range,
                            onClick = { selectedRange = range },
                            compact = true
                        )
                    }
                }
            }

            // 4. SUMMARY
            item(key = "summary") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = colors.positive,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Income", fontSize = 11.sp, color = colors.textMuted)
                            }
                            Text(
                                text = "₹%,.0f".format(totalIncome),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.positive
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = colors.negative,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Expenses", fontSize = 11.sp, color = colors.textMuted)
                            }
                            Text(
                                text = "₹%,.0f".format(totalExpense),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.negative
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Net Balance",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                            Text(
                                text = "₹%,.0f".format(netBalance),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text
                            )
                        }
                    }
                }
            }

            // 5. LIST HEADER
            item(key = "list_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Text(
                        text = if (filteredTransactions.size == 1) {
                            "1 item"
                        } else {
                            "${filteredTransactions.size} items"
                        },
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }

            // 6. LIST
            if (filteredTransactions.isEmpty()) {
                item(key = "empty") {
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
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = colors.textMuted.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isFiltered) {
                                    "No matching transactions"
                                } else {
                                    "No transactions yet"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.text
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isFiltered) {
                                    "Try a different search, type or date range."
                                } else {
                                    "Tap + to record your first transaction."
                                },
                                fontSize = 12.sp,
                                color = colors.textMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredTransactions,
                    key = { txn ->
                        when (txn) {
                            is TransactionUi.Expense -> "e${txn.data.id}"
                            is TransactionUi.Income -> "i${txn.data.id}"
                        }
                    }
                ) { transaction ->
                    ListComponent(navController = navController, transaction = transaction)
                }
            }

            item { Spacer(modifier = Modifier.height(70.dp)) }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false
) {
    val colors = appColors()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) colors.accent else colors.surface,
        shadowElevation = if (isSelected) 0.dp else 1.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = if (compact) 12.sp else 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else colors.text,
            modifier = Modifier.padding(
                horizontal = if (compact) 14.dp else 16.dp,
                vertical = 8.dp
            )
        )
    }
}
