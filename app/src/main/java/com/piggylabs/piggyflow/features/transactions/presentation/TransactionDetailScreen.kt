package com.piggylabs.piggyflow.features.transactions.presentation

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.Income
import com.piggylabs.piggyflow.core.navigation.Transactions
import com.piggylabs.piggyflow.features.home.presentation.components.formatDateForUI
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@ExperimentalMaterial3Api
@Composable
fun TransactionDetailScreen(navController: NavHostController, viewModel: HomeViewModel, type: String, listID: String){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors().background)
    ){
        TransactionDetailScreenComponent(navController = navController, viewModel = viewModel, type = type, listID = listID)
    }
}

@ExperimentalMaterial3Api
@Composable
fun TransactionDetailScreenComponent(navController: NavHostController, viewModel: HomeViewModel, type: String, listID: String){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val colors = appColors()
    val scope  = rememberCoroutineScope()

    val id = listID.toInt()

    val expenses by viewModel.observeExpenseById(id).collectAsState(initial = null)
    val incomes by viewModel.observeIncomeById(id).collectAsState(initial = null)

    val expense = if (type == "expense") expenses else null
    val income = if (type == "income") incomes else null

    val isExpense = expense != null

    if (expense == null && income == null) {
        Scaffold(topBar = { DetailTopBar(navController = navController, onEditClick = null, onDeleteClick = null) }) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.accent)
            }
        }
        return
    }

    val categoryName = if (isExpense) expense.categoryName else income?.categoryName?.ifBlank { "Income" } ?: "Income"
    val emoji = if (isExpense) expense.categoryEmoji else income?.categoryEmoji?.ifBlank { "💰" } ?: "💰"
    val amount = if (isExpense) expense.amount else income!!.amount
    val note = if (isExpense) expense.note else income!!.note
    val date = if (isExpense) expense.date else income!!.date
    val typeLabel = if (isExpense) "Expense" else "Income"
    val accentColor = if (isExpense) colors.negative else colors.positive
    val accentSoft = if (isExpense) colors.negativeSoft else colors.positiveSoft

    //Data Edit Bottom Sheet
    var showEditSheet by remember { mutableStateOf(false) }
    val editSheetState  =  rememberModalBottomSheetState( skipPartiallyExpanded = true)
    var isAddLoading by remember { mutableStateOf( false ) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var editAmount by remember { mutableStateOf("") }
    var editNote by remember { mutableStateOf("") }
    var editDate by remember { mutableStateOf("") }

    fun openEditSheet() {
        if (isExpense) {
            editAmount = expense.amount.toString()
            editNote = expense.note
            editDate = expense.date
        } else {
            editAmount = income!!.amount.toString()
            editNote = income.note
            editDate = income.date
        }
        showEditSheet = true
    }

    LaunchedEffect(editSheetState.isVisible) {
        if (!editSheetState.isVisible) {
            showEditSheet = false
        }
    }

    // Same category, most recent first, excluding this transaction — real data, no new query.
    val relatedTransactions = remember(uiState.expenses, uiState.income, categoryName, id, isExpense) {
        if (isExpense) {
            uiState.expenses
                .filter { it.categoryName == categoryName && it.id != id }
                .sortedByDescending { it.date }
                .take(3)
        } else {
            uiState.income
                .filter { it.categoryName == categoryName && it.id != id }
                .sortedByDescending { it.date }
                .take(3)
        }
    }

    val categoryExpensesThisMonth = remember(uiState.expenses, categoryName, isExpense) {
        if (isExpense) uiState.expenses.filter { it.categoryName == categoryName } else emptyList()
    }
    val totalInCategory = categoryExpensesThisMonth.sumOf { it.amount }
    val countInCategory = categoryExpensesThisMonth.size
    val averageInCategory = if (countInCategory > 0) totalInCategory / countInCategory else 0.0

    Scaffold(
        topBar = {
            DetailTopBar(
                navController = navController,
                onEditClick = { openEditSheet() },
                onDeleteClick = { showDeleteDialog = true }
            )
        },
        bottomBar = {
            DetailBottomActionBar(
                onSplitExpense = {
                    Toast.makeText(context, "Split expense is coming soon", Toast.LENGTH_SHORT).show()
                },
                onMarkFrequent = {
                    Toast.makeText(context, "Marking as frequent is coming soon", Toast.LENGTH_SHORT).show()
                },
                onDelete = { showDeleteDialog = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. HERO
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(accentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = categoryName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formatDateForUI(date),
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isExpense) "-₹%,.2f".format(amount) else "+₹%,.2f".format(amount),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(shape = RoundedCornerShape(10.dp), color = accentSoft) {
                                Text(
                                    text = typeLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. SPLIT DETAILS (static — expenses aren't linked to an account yet)
            item {
                DetailSectionCard(title = "Split Details", trailingLabel = "Edit Split", onTrailingClick = {
                    Toast.makeText(context, "Splitting across accounts is coming soon", Toast.LENGTH_SHORT).show()
                }) {
                    IconRow(
                        icon = Icons.Default.SwapHoriz,
                        title = "Not linked to an account",
                        subtitle = "Full amount shown here",
                        trailing = "100%"
                    )
                }
            }

            // 3. CATEGORY
            item {
                DetailSectionCard(title = "Category", trailingLabel = "Edit Category", onTrailingClick = { openEditSheet() }) {
                    IconRow(
                        icon = Icons.Default.Category,
                        title = categoryName,
                        subtitle = typeLabel,
                        emoji = emoji
                    )
                }
            }

            // 4. NOTES
            item {
                DetailSectionCard(title = "Notes", trailingLabel = "Edit Note", onTrailingClick = { openEditSheet() }) {
                    Text(
                        text = note.ifBlank { "No note added" },
                        fontSize = 14.sp,
                        color = colors.text,
                        lineHeight = 20.sp
                    )
                }
            }

            // 5. RECEIPT (static — no receipt image field on expense/income yet)
            item {
                DetailSectionCard(title = "Receipt", trailingLabel = null, onTrailingClick = null) {
                    IconRow(
                        icon = Icons.Default.ReceiptLong,
                        title = "No receipt attached",
                        subtitle = "Attach one from the bill scanner next time"
                    )
                }
            }

            // 6. SPENDING INSIGHTS (real, category-based since there's no merchant field)
            if (isExpense && countInCategory > 0) {
                item {
                    DetailSectionCard(title = "Spending Insights", trailingLabel = null, onTrailingClick = null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InsightStat(label = "Total in $categoryName", value = "₹%,.0f".format(totalInCategory))
                            InsightStat(label = "Transactions", value = countInCategory.toString())
                            InsightStat(label = "Average", value = "₹%,.0f".format(averageInCategory))
                        }
                    }
                }
            }

            // 7. RELATED TRANSACTIONS (real: same category, most recent 3)
            if (relatedTransactions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Related Transactions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.text)
                        Text(
                            text = "View All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.accent,
                            modifier = Modifier.clickable {
                                navController.navigate(Transactions.route) { launchSingleTop = true }
                            }
                        )
                    }
                }

                items(relatedTransactions.size) { index ->
                    val txn = relatedTransactions[index]
                    val (txnEmoji, txnAmount, txnDate) = when (txn) {
                        is Expense -> Triple(txn.categoryEmoji, txn.amount, txn.date)
                        is Income -> Triple(txn.categoryEmoji, txn.amount, txn.date)
                        else -> Triple("💰", 0.0, "")
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = txnEmoji.ifBlank { "💰" }, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = categoryName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.text)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isExpense) "-₹%,.0f".format(txnAmount) else "+₹%,.0f".format(txnAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                                Text(text = formatDateForUI(txnDate), fontSize = 10.sp, color = colors.textMuted)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showEditSheet){
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    editSheetState.hide()
                }
            },
            sheetState = editSheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = appColors().background
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current

            var showDatePicker by remember { mutableStateOf(false) }
            var editDatePicked by remember { mutableStateOf(LocalDate.parse(editDate)) }
            val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            val editAmountValue = editAmount.toDoubleOrNull()
            val isEditValid = editAmountValue != null && editAmountValue > 0

            val datePickerDialog = DatePickerDialog(
                context,
                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    editDatePicked = LocalDate.of(year, month + 1, dayOfMonth)
                    showDatePicker = false
                },
                editDatePicked.year,
                editDatePicked.monthValue - 1,
                editDatePicked.dayOfMonth
            ).apply {
                setOnDismissListener {
                    showDatePicker = false
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(appColors().negative)
                            .clickable {
                                scope.launch {
                                    editSheetState.hide()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isExpense) "Update Expense" else "Update Income",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = appColors().text,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    EditSummaryCard(
                        title = if (isExpense) "Expense details" else "Income details",
                        subtitle = if (isExpense) {
                            "Update amount, date, or note for this expense."
                        } else {
                            "Update amount, date, or note for this income."
                        },
                        accent = accentColor,
                        dateText = editDatePicked.format(dateFormatter)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column {
                        Text(text = "Amount", fontSize = 18.sp, fontWeight = FontWeight.Normal, color = appColors().text)
                        Spacer(modifier = Modifier.height(5.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = editAmount,
                                singleLine = true,
                                onValueChange = { editAmount = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                placeholder = {
                                    Text(
                                        text = if (isExpense) "Enter expense amount" else "Enter income amount",
                                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W500, color = Color.Gray, textAlign = TextAlign.Start)
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyRupee,
                                        contentDescription = "",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Gray
                                    )
                                },
                                textStyle = TextStyle(fontWeight = FontWeight.W500, fontSize = 16.sp, color = appColors().text)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    EditDatePickerSection(
                        dateText = editDatePicked.format(dateFormatter),
                        onTodayClick = { editDatePicked = LocalDate.now() },
                        onYesterdayClick = { editDatePicked = LocalDate.now().minusDays(1) },
                        onPickDate = {
                            if (!showDatePicker) {
                                showDatePicker = true
                                datePickerDialog.show()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        Row {
                            Text(text = "Note", fontSize = 18.sp, fontWeight = FontWeight.Normal, color = appColors().text)
                            Text(text = " (Optional)", fontSize = 18.sp, fontWeight = FontWeight.Normal, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = editNote,
                                minLines = 3,
                                maxLines = 3,
                                onValueChange = { editNote = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = appColors().container,
                                    unfocusedContainerColor = appColors().container,
                                    cursorColor = appColors().text
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                textStyle = TextStyle(fontWeight = FontWeight.W500, fontSize = 16.sp, color = appColors().text)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val amountValue = editAmount.toDoubleOrNull()
                            if (amountValue == null || amountValue <= 0) {
                                Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isAddLoading = true

                            scope.launch {
                                try {
                                    if (isExpense) {
                                        viewModel.updateExpense(
                                            expense!!.copy(amount = amountValue, note = editNote, date = editDatePicked.toString())
                                        )
                                        Toast.makeText(context, "Expense Updated", Toast.LENGTH_LONG).show()
                                    } else {
                                        viewModel.updateIncome(
                                            income!!.copy(amount = amountValue, note = editNote, date = editDatePicked.toString())
                                        )
                                        Toast.makeText(context, "Income Updated", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("EditTransaction", "$e")
                                } finally {
                                    delay(1000L)
                                    isAddLoading = false
                                    editSheetState.hide()
                                }
                            }
                        },
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp, pressedElevation = 5.dp),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors().accent, contentColor = Color.White),
                        enabled = !isAddLoading && isEditValid,
                    ) {
                        if (isAddLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (isExpense) "Update Expense" else "Update Income",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W500,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete transaction?", color = appColors().text) },
            text = {
                Text(
                    "This transaction will be removed permanently.",
                    color = appColors().text
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            if (isExpense) {
                                viewModel.deleteExpenseById(id)
                            } else {
                                viewModel.deleteIncomeById(id)
                            }

                            Toast.makeText(
                                navController.context,
                                "Deleted Successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            delay(300)
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors().red,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = appColors().background
        )
    }
}

@ExperimentalMaterial3Api
@Composable
private fun DetailTopBar(
    navController: NavHostController,
    onEditClick: (() -> Unit)?,
    onDeleteClick: (() -> Unit)?
) {
    val colors = appColors()
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(text = "Transaction Details", fontSize = 18.sp, fontWeight = FontWeight.W600, color = colors.text)
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
        actions = {
            if (onEditClick != null || onDeleteClick != null) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = colors.text)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        if (onEditClick != null) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEditClick() })
                        }
                        if (onDeleteClick != null) {
                            DropdownMenuItem(text = { Text("Delete", color = colors.negative) }, onClick = { showMenu = false; onDeleteClick() })
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
    )
}

@Composable
private fun DetailBottomActionBar(
    onSplitExpense: () -> Unit,
    onMarkFrequent: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = appColors()
    Surface(color = colors.background, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActionPill(
                icon = Icons.Default.SwapHoriz,
                label = "Split Expense",
                containerColor = colors.surfaceMuted,
                contentColor = colors.text,
                modifier = Modifier.weight(1f),
                onClick = onSplitExpense
            )
            ActionPill(
                icon = Icons.Default.Repeat,
                label = "Mark as Frequent",
                containerColor = colors.surfaceMuted,
                contentColor = colors.text,
                modifier = Modifier.weight(1f),
                onClick = onMarkFrequent
            )
            ActionPill(
                icon = Icons.Default.Close,
                label = "Delete",
                containerColor = colors.negativeSoft,
                contentColor = colors.negative,
                modifier = Modifier.weight(1f),
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun ActionPill(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    trailingLabel: String?,
    onTrailingClick: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    val colors = appColors()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.text)
                if (trailingLabel != null && onTrailingClick != null) {
                    Text(
                        text = trailingLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent,
                        modifier = Modifier.clickable(onClick = onTrailingClick)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun IconRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: String? = null,
    emoji: String? = null
) {
    val colors = appColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            if (!emoji.isNullOrBlank()) {
                Text(text = emoji, fontSize = 17.sp)
            } else {
                Icon(imageVector = icon, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
            Text(text = subtitle, fontSize = 11.sp, color = colors.textMuted)
        }
        if (trailing != null) {
            Text(text = trailing, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.text)
        }
    }
}

@Composable
private fun InsightStat(label: String, value: String) {
    val colors = appColors()
    Column {
        Text(text = label, fontSize = 10.sp, color = colors.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.text)
    }
}

@Composable
private fun EditSummaryCard(
    title: String,
    subtitle: String,
    accent: Color,
    dateText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors().text
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = accent.copy(alpha = 0.12f)
            ) {
                Text(
                    text = dateText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = appColors().text
                )
            }
        }
    }
}

@Composable
private fun EditDatePickerSection(
    dateText: String,
    onTodayClick: () -> Unit,
    onYesterdayClick: () -> Unit,
    onPickDate: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Date",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = appColors().text
            )

            Card(
                modifier = Modifier.clickable(onClick = onPickDate),
                colors = CardDefaults.cardColors(containerColor = appColors().container),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600,
                        color = appColors().text
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date",
                        modifier = Modifier.size(20.dp),
                        tint = appColors().text
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = appColors().container,
                modifier = Modifier.clickable(onClick = onTodayClick)
            ) {
                Text(
                    text = "Today",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = appColors().text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = appColors().container,
                modifier = Modifier.clickable(onClick = onYesterdayClick)
            ) {
                Text(
                    text = "Yesterday",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = appColors().text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = appColors().container,
                modifier = Modifier.clickable(onClick = onPickDate)
            ) {
                Text(
                    text = "Pick date",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    color = appColors().text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun String.limit(max: Int): String {
    return if (this.length > max) this.take(max) + "..." else this
}

fun String.breakEvery(n: Int): String {
    return chunked(n).joinToString("\n")
}
