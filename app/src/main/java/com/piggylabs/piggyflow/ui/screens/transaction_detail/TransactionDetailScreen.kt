package com.piggylabs.piggyflow.ui.screens.transaction_detail

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.navigation.components.TopBar
import com.piggylabs.piggyflow.ui.screens.home.components.formatDateForUI
import com.piggylabs.piggyflow.ui.screens.home.viewmodel.HomeViewModel
import com.piggylabs.piggyflow.ui.theme.appColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ExperimentalMaterial3Api
@Composable
fun TransactionDetailScreen(navController: NavHostController, viewModel: HomeViewModel, type: String, listID: String){
    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController)}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ){
            TransactionDetailScreenComponent(navController = navController,viewModel = viewModel, type = type, listID = listID)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun TransactionDetailScreenComponent(navController: NavHostController, viewModel: HomeViewModel, type: String, listID: String){
    val context = LocalContext.current
    val scope  = rememberCoroutineScope()

    val id = listID.toInt()

    Log.d("ListDataDetailsScreen", "$id")

    val expenses by viewModel.observeExpenseById(id).collectAsState(initial = null)
    val incomes by viewModel.observeIncomeById(id).collectAsState(initial = null)

    val expense = if (type == "expense") expenses else null
    val income = if (type == "income") incomes else null

    val isExpense = expense != null

    if (expense == null && income == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val title = if (isExpense) expense.categoryName else income?.categoryName?.ifBlank { "Income" } ?: "Income"
    val emoji = if (isExpense) expense.categoryEmoji else income?.categoryEmoji?.ifBlank { "💰" } ?: "💰"
    val amount = if (isExpense) expense.amount else income!!.amount
    val note = if (isExpense) expense.note else income!!.note
    val date = if (isExpense) expense.date else income!!.date
    val type = if (isExpense) "Expense" else "Income"

    //Data Edit Bottom Sheet
    var showEditSheet by remember { mutableStateOf(false) }
    val editSheetState  =  rememberModalBottomSheetState( skipPartiallyExpanded = true)
    var isAddLoading by remember { mutableStateOf( false ) }

    Log.d("showAddDataSheet", "$showEditSheet")

    var editAmount by remember { mutableStateOf("") }
    var editNote by remember { mutableStateOf("") }
    var editDate by remember { mutableStateOf("") }


    LaunchedEffect(editSheetState.isVisible) {
        if (!editSheetState.isVisible) {
            showEditSheet = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    appColors().container,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp,
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.W600,
                    color = appColors().text
                )

            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Type:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500,
                    color = appColors().text
                )

                Text(
                    text = type,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Amount:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500,
                    color = appColors().text
                )

                Text(
                    text = "$amount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500,
                    color = appColors().text
                )

                Text(
                    text = formatDateForUI(date),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Note:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500,
                    color = appColors().text
                )

                Text(
                    text = note.breakEvery(24),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    scope.launch {
                        if (isExpense){
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
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                ),
                modifier = Modifier
                    .weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors().red.copy(alpha = 0.8f),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Delete",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W500,
                    color = Color.White
                )
            }

            Spacer( modifier = Modifier.width(12.dp) )

            Button(
                onClick = {
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
                },
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 1.dp,
                    pressedElevation = 5.dp,
                ),
                modifier = Modifier
                    .weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors().green,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Edit",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W500,
                    color = Color.White
                )
            }
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
            var date by remember { mutableStateOf(LocalDate.parse(editDate)) }
            val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")

            val datePickerDialog = DatePickerDialog(
                context,
                { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    date = LocalDate.of(year, month + 1, dayOfMonth)
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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
                                    editSheetState.hide()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    if (type == "Expense") {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {

                            Text(
                                text = "Update Expense",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = appColors().text,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Column {

                                Text(
                                    text = "Enter Amount",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = appColors().text
                                )

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
                                                text = "Amount",
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
                                                    imageVector = Icons.Default.CurrencyRupee,
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
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
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
                                            modifier = Modifier.size(20.dp),
                                            tint = appColors().text
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column {

                                Row {
                                    Text(
                                        text = "Note",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = appColors().text
                                    )

                                    Text(
                                        text = " (Optional)",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(5.dp))

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = editNote,
                                        minLines = 3,
                                        maxLines = 3,
                                        onValueChange = { editNote = it },
                                        modifier = Modifier
                                            .fillMaxWidth(),
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
                                        textStyle = TextStyle(
                                            fontWeight = FontWeight.W500,
                                            fontSize = 16.sp,
                                            color = appColors().text
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {

                                    if (editAmount.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Please fill all fields",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    val amount = editAmount.toDoubleOrNull()
                                    if (amount == null || amount <= 0) {
                                        Toast.makeText(
                                            context,
                                            "Enter a valid amount",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }

                                    isAddLoading = true

                                    scope.launch {
                                        try {
                                            viewModel.updateExpense(
                                                expense!!.copy(
                                                    amount = amount,
                                                    note = editNote,
                                                    date = date.toString()
                                                )
                                            )
                                            Toast.makeText(
                                                context,
                                                "Expense Updated",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } catch (e: Exception) {
                                            Log.e("EditExpense", "$e")
                                        } finally {
                                            delay(1000L)
                                            isAddLoading = false
                                            editSheetState.hide()
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
                                enabled = !isAddLoading,
                            ) {
                                if (isAddLoading) {
                                    // Show a loading spinner when the button is disabled
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Update",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.W500,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        Column {

                            Text(
                                text = "Update Income",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = appColors().text,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Enter Income",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = appColors().text
                            )

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
                                            text = "Income",
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
                                                imageVector = Icons.Default.CurrencyRupee,
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
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
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
                                modifier = Modifier
                                    .clickable {
                                        if (!showDatePicker) {  // Only open if not already showing
                                            showDatePicker = true
                                            datePickerDialog.show()
                                        }
                                    },

                                colors = CardDefaults.cardColors(appColors().container ),
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
                                        modifier = Modifier.size(20.dp),
                                        tint = appColors().text
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column {

                            Row {
                                Text(
                                    text = "Note",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = appColors().text
                                )

                                Text(
                                    text = " (Optional)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.height(5.dp))

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = editNote,
                                    minLines = 3,
                                    maxLines = 3,
                                    onValueChange = { editNote = it },
                                    modifier = Modifier
                                        .fillMaxWidth(),
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
                                    textStyle = TextStyle(
                                        fontWeight = FontWeight.W500,
                                        fontSize = 16.sp,
                                        color = appColors().text
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {

                                if (editAmount.isBlank()) {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val amount = editAmount.toDoubleOrNull()
                                if (amount == null || amount <= 0) {
                                    Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isAddLoading = true

                                scope.launch {
                                    try {
                                        viewModel.updateIncome(
                                            income!!.copy(
                                                amount = amount,
                                                note = editNote,
                                                date = date.toString()
                                            )
                                        )

                                        Toast.makeText(context, "Income Updated", Toast.LENGTH_LONG).show()
                                    }catch (e: Exception){
                                        Log.e("EditIncome", "$e")
                                    }finally {
                                        delay(1000L)
                                        isAddLoading = false
                                        editSheetState.hide()
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
                            enabled = !isAddLoading,
                        ) {
                            if (isAddLoading) {
                                // Show a loading spinner when the button is disabled
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Update",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.W500,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
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