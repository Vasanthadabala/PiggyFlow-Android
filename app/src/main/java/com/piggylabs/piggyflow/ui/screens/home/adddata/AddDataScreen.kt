package com.piggylabs.piggyflow.ui.screens.home.adddata

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiSymbols
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.data.local.entity.UserCategoryEntity
import com.piggylabs.piggyflow.ui.navigation.components.TopBar
import com.piggylabs.piggyflow.ui.screens.home.Category
import com.piggylabs.piggyflow.ui.screens.home.components.CategoryUi
import com.piggylabs.piggyflow.ui.screens.home.components.CombinedCategoryGrid
import com.piggylabs.piggyflow.ui.screens.home.viewmodel.HomeViewModel
import com.piggylabs.piggyflow.ui.theme.appColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ExperimentalMaterial3Api
@Composable
fun AddDataScreen(navController: NavHostController, viewModel: HomeViewModel){
    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController)}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ){
            AddDataScreenComponent(navController = navController, viewModel = viewModel)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun AddDataScreenComponent(navController: NavHostController, viewModel: HomeViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    val typeOptions = listOf("Expense", "Income")
    var selectedTypeOption by remember { mutableStateOf("Expense") }

    //Combined Categories
    val allCategories: List<CategoryUi> = viewModel.categories.map { CategoryUi.UserCategory(it) } +
                Category.entries.map { CategoryUi.EnumCategory(it) }

    var price by remember { mutableStateOf("") }
    var income by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var selectedEnumCategory by remember { mutableStateOf<Category?>(null) }
    var selectedUserCategory by remember { mutableStateOf<UserCategoryEntity?>(null) }

    var isAddLoading by remember { mutableStateOf(false) }

    //Add category Bottom Sheet
    var showAddCategorySheet by remember { mutableStateOf(false) }
    val addCategorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedCategoryForDelete by remember {
        mutableStateOf<UserCategoryEntity?>(null)
    }

    LaunchedEffect(addCategorySheetState.isVisible) {
        if (!addCategorySheetState.isVisible) {
            showAddCategorySheet = false
        }
    }

    var categoryName by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var isAddCategoryLoading by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(LocalDate.now()) }
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
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = appColors().container,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    typeOptions.forEach { option ->
                        SegmentedButton(
                            selected = selectedTypeOption == option,
                            onClick = { selectedTypeOption = option },
                            label = {
                                Text(
                                    text = option,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = appColors().green,
                                inactiveContainerColor = Color.Transparent,
                                inactiveBorderColor = Color.Transparent,
                                activeBorderColor = Color.Transparent,
                                activeContentColor = Color.White,
                                inactiveContentColor = appColors().text
                            ),
                            modifier = Modifier
                                .padding(horizontal = 0.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTypeOption == "Expense") {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Category",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors().text
                        )

                        Button(
                            onClick = {
                                showAddCategorySheet = true
                            },
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 1.dp,
                                pressedElevation = 5.dp,
                            ),
                            modifier = Modifier
                                .height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = appColors().green,
                                contentColor = Color.White
                            ),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = ""
                                )
                                Text(
                                    text = "Add Category",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CombinedCategoryGrid(
                        categories = allCategories,
                        selectedEnum = selectedEnumCategory,
                        selectedUser = selectedUserCategory,

                        onEnumClick = { enumCat ->
                            selectedEnumCategory = enumCat
                            selectedUserCategory = null
                            Log.d("HomeScreen", "Enum Selected = $enumCat")
                        },

                        onUserClick = { userCat ->
                            selectedUserCategory = userCat
                            selectedEnumCategory = null
                            Log.d("HomeScreen", "User Selected = $userCat")
                        },

                        onUserLongClick = { userCat ->
                            selectedCategoryForDelete = userCat
                            showDeleteDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                                value = price,
                                singleLine = true,
                                onValueChange = { price = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
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
                                    keyboardType = KeyboardType.Number,
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
                                    tint = appColors().text,
                                    modifier = Modifier.size(20.dp)
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
                                value = note,
                                minLines = 2,
                                maxLines = 2,
                                onValueChange = { note = it },
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

                            if (price.isBlank() || (selectedEnumCategory == null && selectedUserCategory == null)) {
                                Toast.makeText(
                                    context,
                                    "Please fill all fields",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            val amount = price.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                Toast.makeText(
                                    context,
                                    "Enter a valid amount",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            val finalCategoryName = selectedEnumCategory?.categoryName
                                ?: selectedUserCategory?.name
                                ?: ""

                            val finalCategoryEmoji = selectedEnumCategory?.emoji
                                ?: selectedUserCategory?.emoji
                                ?: ""

                            isAddLoading = true

                            scope.launch {
                                try {
                                    if (selectedTypeOption == "Expense") {
                                        viewModel.addExpense(
                                            categoryType = "Expense",
                                            amount = price.toDouble(),
                                            note = note,
                                            date = date.toString(),
                                            categoryName = finalCategoryName,
                                            categoryEmoji = finalCategoryEmoji
                                        )

                                        price = ""
                                        note = ""
                                        selectedEnumCategory = null
                                        selectedUserCategory = null

                                        delay(1000L)

                                        navController.popBackStack()

                                        Toast.makeText(
                                            context,
                                            "Expense Added",
                                            Toast.LENGTH_LONG
                                        ).show()

                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error Adding Expense",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("HomeScreen", "$e")
                                } finally {
                                    delay(800L)
                                    isAddLoading = false
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
                                text = "Add",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W500,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            } else if (selectedTypeOption == "Income") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Category",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors().text
                        )

                        Button(
                            onClick = {
                                showAddCategorySheet = true
                            },
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 1.dp,
                                pressedElevation = 5.dp,
                            ),
                            modifier = Modifier
                                .height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = appColors().green,
                                contentColor = Color.White
                            ),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = ""
                                )
                                Text(
                                    text = "Add Category",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CombinedCategoryGrid(
                        categories = allCategories,
                        selectedEnum = selectedEnumCategory,
                        selectedUser = selectedUserCategory,

                        onEnumClick = { enumCat ->
                            selectedEnumCategory = enumCat
                            selectedUserCategory = null
                            Log.d("HomeScreen", "Enum Selected = $enumCat")
                        },

                        onUserClick = { userCat ->
                            selectedUserCategory = userCat
                            selectedEnumCategory = null
                            Log.d("HomeScreen", "User Selected = $userCat")
                        },

                        onUserLongClick = { userCat ->
                            selectedCategoryForDelete = userCat
                            showDeleteDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Enter Income",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = appColors().text
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = income,
                            singleLine = true,
                            onValueChange = { income = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
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
                                keyboardType = KeyboardType.Number,
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
                                containerColor = appColors().container,
                                contentColor = appColors().text
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
                                    fontWeight = FontWeight.W600
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select date",
                                    modifier = Modifier.size(20.dp)
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
                                value = note,
                                minLines = 2,
                                maxLines = 2,
                                onValueChange = { note = it },
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

                            if (income.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please fill all fields",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                return@Button
                            }

                            val amount = income.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT)
                                    .show()
                                return@Button
                            }

                            isAddLoading = true

                            val finalCategoryName = selectedEnumCategory?.categoryName
                                ?: selectedUserCategory?.name
                                ?: ""

                            val finalCategoryEmoji = selectedEnumCategory?.emoji
                                ?: selectedUserCategory?.emoji
                                ?: ""

                            scope.launch {
                                try {

                                    if (selectedTypeOption == "Income") {
                                        viewModel.addIncome(
                                            categoryType = "Income",
                                            amount = amount,
                                            note = note,
                                            date = date.toString(),
                                            categoryName = finalCategoryName,
                                            categoryEmoji = finalCategoryEmoji,
                                        )

                                        income = ""
                                        note = ""
                                        selectedEnumCategory = null
                                        selectedUserCategory = null

                                        delay(1000L)

                                        navController.popBackStack()

                                        Toast.makeText(context, "Income Added", Toast.LENGTH_LONG)
                                            .show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error Adding Income",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                } catch (e: Exception) {
                                    Log.e("HomeScreen", "$e")
                                } finally {
                                    delay(800L)
                                    isAddLoading = false
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
                                text = "Add",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W500,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    if (showAddCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    addCategorySheetState.hide()
                }
            },
            sheetState = addCategorySheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = appColors().background
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current

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
                                    addCategorySheetState.hide()
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

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Add New Category",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.W500,
                    textAlign = TextAlign.Center,
                    color = appColors().text,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column {

                    Text(
                        text = "Enter Category Name",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = appColors().text
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = categoryName,
                            singleLine = true,
                            onValueChange = { categoryName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            placeholder = {
                                Text(
                                    text = "Category name",
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
                                        imageVector = Icons.Default.Category,
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

                Column {

                    Text(
                        text = "Emoji",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = appColors().text
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = emoji,
                            singleLine = true,
                            onValueChange = { emoji = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            placeholder = {
                                Text(
                                    text = "Provide emoji (Optional)",
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
                                        imageVector = Icons.Default.EmojiSymbols,
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

                Button(
                    onClick = {
                        if (categoryName.isBlank()) {
                            Toast.makeText(
                                context,
                                "Provide Category Name",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        isAddCategoryLoading = true

                        scope.launch {
                            try {
                                viewModel.addCategory(
                                    name = categoryName,
                                    emoji = emoji
                                )

                                categoryName = ""
                                emoji = ""

                                Toast.makeText(
                                    context,
                                    "category added",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } catch (e: Exception) {
                                Log.e("HomeScreen", "$e")
                            } finally {
                                delay(500L)
                                isAddCategoryLoading = false
                                addCategorySheetState.hide()
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
                    enabled = !isAddCategoryLoading,
                ) {
                    if (isAddCategoryLoading) {
                        // Show a loading spinner when the button is disabled
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Add Category",
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

    if (showDeleteDialog && selectedCategoryForDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },

            title = {
                Text("Delete Category", color = appColors().text)
            },

            text = {
                Text(
                    "This category will be permanently deleted.",
                    color = appColors().text
                )
            },

            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategoryById(
                            selectedCategoryForDelete!!.id
                        )

                        showDeleteDialog = false
                        selectedCategoryForDelete = null

                        Toast.makeText(
                            context,
                            "Category Deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red.copy(alpha = 0.8f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            },

            dismissButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    selectedCategoryForDelete = null
                }) {
                    Text("Cancel")
                }
            },

            containerColor = appColors().background
        )
    }
}