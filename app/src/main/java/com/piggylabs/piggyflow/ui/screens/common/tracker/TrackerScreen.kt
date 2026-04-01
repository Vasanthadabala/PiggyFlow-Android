package com.piggylabs.piggyflow.ui.screens.common.tracker

import android.app.DatePickerDialog
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.data.local.entity.SubscriptionEntity
import com.piggylabs.piggyflow.navigation.components.BottomBar
import com.piggylabs.piggyflow.ui.screens.common.tracker.components.TrackerListComponent
import com.piggylabs.piggyflow.ui.screens.common.tracker.viewmodel.TrackerViewModel
import com.piggylabs.piggyflow.ui.theme.appColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(navController: NavHostController, viewModel: TrackerViewModel) {
    var showAddSheet by remember { mutableStateOf(false) }
    var selectedTrackerType by remember { mutableStateOf("subscription") }
    var editingSubscription by remember { mutableStateOf<SubscriptionEntity?>(null) }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            TrackerScreenComponent(
                viewModel = viewModel,
                selectedTrackerType = selectedTrackerType,
                onTypeChange = { selectedTrackerType = it },
                onEdit = { editingSubscription = it },
                onDelete = { viewModel.deleteSubscription(it.id) }
            )

            FloatingActionButton(
                onClick = { showAddSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 20.dp, end = 20.dp),
                containerColor = appColors().green,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subscription",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    if (showAddSheet) {
        AddSubscriptionBottomSheet(
            initialType = selectedTrackerType,
            initialName = "",
            initialSubType = "monthly",
            initialAmount = "",
            initialDueDate = LocalDate.now(),
            submitLabel = "Add",
            onDismiss = { showAddSheet = false },
            onSubmit = { type, name, subType, amount, dueDate ->
                viewModel.addSubscription(
                    type = type,
                    name = name,
                    subType = subType,
                    amount = amount,
                    dueDate = dueDate
                )
                showAddSheet = false
            }
        )
    }

    editingSubscription?.let { item ->
        AddSubscriptionBottomSheet(
            initialType = item.type,
            initialName = item.name,
            initialSubType = item.subType,
            initialAmount = item.amount.toString(),
            initialDueDate = runCatching { LocalDate.parse(item.dueDate) }.getOrDefault(LocalDate.now()),
            submitLabel = "Save",
            onDismiss = { editingSubscription = null },
            onSubmit = { type, name, subType, amount, dueDate ->
                viewModel.updateSubscription(
                    id = item.id,
                    type = type,
                    name = name,
                    subType = subType,
                    amount = amount,
                    dueDate = dueDate
                )
                editingSubscription = null
            }
        )
    }
}

@Composable
fun TrackerScreenComponent(
    viewModel: TrackerViewModel,
    selectedTrackerType: String,
    onTypeChange: (String) -> Unit,
    onEdit: (SubscriptionEntity) -> Unit,
    onDelete: (SubscriptionEntity) -> Unit
) {
    val subscriptions = remember(viewModel.subscriptions, selectedTrackerType) {
        viewModel.subscriptions
            .filter { it.type.equals(selectedTrackerType, ignoreCase = true) }
            .sortedByDescending { it.id }
    }

    val totalMonthlyAmount = subscriptions.sumOf { subscription ->
        if (subscription.subType.equals("yearly", ignoreCase = true)) {
            subscription.amount / 12
        } else {
            subscription.amount
        }
    }

    val totalYearlyAmount = subscriptions.sumOf { subscription ->
        if (subscription.subType.equals("monthly", ignoreCase = true)) {
            subscription.amount * 12
        } else {
            subscription.amount
        }
    }

    val activeCount = subscriptions.size
    val selectedTitle = if (selectedTrackerType == "emi") "EMI" else "Subscriptions"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
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
                listOf("subscription" to "Subscriptions", "emi" to "EMI").forEach { (value, label) ->
                    SegmentedButton(
                        selected = selectedTrackerType == value,
                        onClick = { onTypeChange(value) },
                        label = {
                            Text(
                                text = label,
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
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = selectedTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors().text
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "₹ %.2f".format(totalMonthlyAmount),
                fontSize = 40.sp,
                fontWeight = FontWeight.Medium,
                color = appColors().text
            )

            Text(
                text = "per month",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "$activeCount active",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )

                Text(
                    text = "•",
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Text(
                    text = "₹ %.2f / year".format(totalYearlyAmount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = selectedTitle,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = appColors().text
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (subscriptions.isEmpty()) {
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
                    modifier = Modifier.size(96.dp),
                    tint = Color.Gray.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No $selectedTitle added yet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W500,
                    color = Color.Gray.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tap the + button to add $selectedTitle data.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(subscriptions.size) { index ->
                    TrackerListComponent(
                        subscription = subscriptions[index],
                        onEdit = { onEdit(subscriptions[index]) },
                        onDelete = { onDelete(subscriptions[index]) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSubscriptionBottomSheet(
    initialType: String,
    initialName: String,
    initialSubType: String,
    initialAmount: String,
    initialDueDate: LocalDate,
    submitLabel: String,
    onDismiss: () -> Unit,
    onSubmit: (type: String, name: String, subType: String, amount: Double, dueDate: String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val typeOptions = listOf("emi", "subscription")
    val subTypeOptions = listOf("yearly", "monthly")

    var selectedType by remember{
        mutableStateOf(initialType)
    }
    var selectedSubType by remember { mutableStateOf(initialSubType) }
    var name by remember { mutableStateOf(initialName) }
    var amount by remember { mutableStateOf(initialAmount) }

    //Date picker
    var showDatePicker by remember { mutableStateOf(false) }
    var dueDate by remember { mutableStateOf(initialDueDate) }

    var typeExpanded by remember { mutableStateOf(false) }
    var subTypeExpanded by remember { mutableStateOf(false) }

    val amountValue = amount.toDoubleOrNull()

    val isValid = name.isNotBlank() && amountValue != null && amountValue > 0
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            dueDate = LocalDate.of(year, month + 1, dayOfMonth)
            showDatePicker = false
        },
        dueDate.year,
        dueDate.monthValue - 1,
        dueDate.dayOfMonth
    ).apply {
        setOnDismissListener {
            showDatePicker = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = appColors().background,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (selectedType == "emi") "Add EMI" else "Add Subscription",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors().text
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Type",
                fontSize = 14.sp,
                color = Color.Gray
            )

            DropdownField(
                value = selectedType,
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it },
                options = typeOptions,
                onSelect = {
                    selectedType = it
                    typeExpanded = false
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column {

                Text(
                    text = "Name",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )

                Spacer(modifier = Modifier.height(5.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        singleLine = true,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = {
                            Text(
                                text = (if (selectedType == "emi") "EMI Name" else "Subscription Name"),
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
                            keyboardType = KeyboardType.Text
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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sub Type",
                fontSize = 14.sp,
                color = Color.Gray
            )

            DropdownField(
                value = selectedSubType,
                expanded = subTypeExpanded,
                onExpandedChange = { subTypeExpanded = it },
                options = subTypeOptions,
                onSelect = {
                    selectedSubType = it
                    subTypeExpanded = false
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column {

                Text(
                    text = "Amount",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )

                Spacer(modifier = Modifier.height(5.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amount,
                        singleLine = true,
                        onValueChange = { amount = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = {
                            Text(
                                text = "Enter Amount",
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
                            keyboardType = KeyboardType.Number
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

            Spacer(modifier = Modifier.height(12.dp))

            Column {

                Text(
                    text = "Date",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )

                Spacer(modifier = Modifier.height(5.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dueDate.format(dateFormatter),
                        readOnly = true,
                        singleLine = true,
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(20),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = appColors().container,
                            unfocusedContainerColor = appColors().container,
                            cursorColor = appColors().text
                        ),
                        leadingIcon  = {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .size(20.dp),
                                    tint = Color.Gray
                                )
                            }
                        },
                        trailingIcon = {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable {
                                            if (!showDatePicker) {  // Only open if not already showing
                                                showDatePicker = true
                                                datePickerDialog.show()
                                            }
                                        },
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

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    onSubmit(
                        selectedType,
                        name.trim(),
                        selectedSubType,
                        amountValue ?: 0.0,
                        dueDate.toString()
                    )
                },
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp,
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors().green,
                    contentColor = Color.White
                ),
                enabled = isValid
            ) {
                Text(
                    text = submitLabel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            singleLine = true,
            shape = RoundedCornerShape(20),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = appColors().container,
                unfocusedContainerColor = appColors().container,
                cursorColor = appColors().text
            ),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(12.dp),
            containerColor = if (isDark) Color.Black else Color.LightGray,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.replaceFirstChar { it.uppercase() }) },
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}
