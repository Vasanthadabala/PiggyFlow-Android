package com.piggylabs.piggyflow.features.tracker.presentation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.domain.model.Subscription
import com.piggylabs.piggyflow.core.navigation.AddTracker
import com.piggylabs.piggyflow.core.navigation.BottomBar
import com.piggylabs.piggyflow.features.tracker.presentation.components.TrackerListComponent
import com.piggylabs.piggyflow.features.tracker.presentation.TrackerViewModel
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(navController: NavHostController, viewModel: TrackerViewModel) {
    var selectedTrackerType by remember { mutableStateOf("subscription") }
    var editingSubscription by remember { mutableStateOf<Subscription?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Upcoming Payments",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            text = "Track and manage all your upcoming bills, subscriptions and EMIs",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
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
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
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
                onDelete = { viewModel.deleteSubscription(it.id) },
                onAddClick = { navController.navigate(AddTracker.forType(selectedTrackerType)) }
            )

            FloatingActionButton(
                onClick = { navController.navigate(AddTracker.forType(selectedTrackerType)) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 80.dp, end = 20.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreenComponent(
    viewModel: TrackerViewModel,
    selectedTrackerType: String,
    onTypeChange: (String) -> Unit,
    onEdit: (Subscription) -> Unit,
    onDelete: (Subscription) -> Unit,
    onAddClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val monthFormatter = DateTimeFormatter.ofPattern("MMM d")

    val allSubscriptions = uiState.subscriptions

    // Due Soon = due within next 7 days
    val dueSoon = remember(allSubscriptions) {
        allSubscriptions
            .filter { sub ->
                runCatching {
                    val d = LocalDate.parse(sub.dueDate)
                    val days = java.time.temporal.ChronoUnit.DAYS.between(today, d)
                    days in 0..7
                }.getOrDefault(false)
            }
            .sortedBy { it.dueDate }
    }

    // Due this month
    val dueThisMonth = remember(allSubscriptions) {
        allSubscriptions
            .filter { sub ->
                runCatching {
                    val d = LocalDate.parse(sub.dueDate)
                    d.month == today.month && d.year == today.year &&
                        java.time.temporal.ChronoUnit.DAYS.between(today, d) > 7
                }.getOrDefault(false)
            }
            .sortedBy { it.dueDate }
    }

    val totalAmountThisMonth = allSubscriptions
        .filter { sub ->
            runCatching {
                val d = LocalDate.parse(sub.dueDate)
                d.month == today.month && d.year == today.year
            }.getOrDefault(false)
        }
        .sumOf { it.amount }

    val paidThisMonth = 2 // static for now
    val totalPaymentsThisMonth = allSubscriptions.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. STATS SUMMARY ROW
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = appColors().container),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TrackerStatBox(
                        label = "Due Soon",
                        value = "${dueSoon.size}",
                        subLabel = "Next 7 days",
                        valueColor = Color(0xFF15803D)
                    )
                    TrackerStatDivider()
                    TrackerStatBox(
                        label = "This Month",
                        value = "$totalPaymentsThisMonth",
                        subLabel = "Total payments",
                        valueColor = Color(0xFFD97706)
                    )
                    TrackerStatDivider()
                    TrackerStatBox(
                        label = "Total Amount",
                        value = "₹%,.0f".format(totalAmountThisMonth),
                        subLabel = "This month",
                        valueColor = Color(0xFFEF4444)
                    )
                    TrackerStatDivider()
                    TrackerStatBox(
                        label = "Paid This Month",
                        value = "$paidThisMonth",
                        subLabel = "Payments",
                        valueColor = Color(0xFF3B82F6)
                    )
                }
            }
        }

        // 2. DUE SOON SECTION
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due Soon",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors().text
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "Next 7 days",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (dueSoon.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors().container)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nothing due in the next 7 days!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors().text
                        )
                    }
                }
            }
        } else {
            items(dueSoon.size) { index ->
                val sub = dueSoon[index]
                val dueDate = runCatching { LocalDate.parse(sub.dueDate) }.getOrNull()
                val daysUntil = dueDate?.let { java.time.temporal.ChronoUnit.DAYS.between(today, it) } ?: 0
                val dueBadge = when {
                    daysUntil == 0L -> "Due today"
                    daysUntil == 1L -> "Due tomorrow"
                    else -> "Due in $daysUntil days"
                }
                DueSoonCard(
                    subscription = sub,
                    dueBadge = dueBadge,
                    formattedDate = dueDate?.format(dateFormatter) ?: sub.dueDate,
                    onEdit = { onEdit(sub) },
                    onDelete = { onDelete(sub) }
                )
            }
        }

        // 3. DUE THIS MONTH SECTION
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due This Month",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors().text
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = today.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        fontSize = 12.sp,
                        color = Color(0xFF15803D),
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (dueThisMonth.isEmpty() && dueSoon.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors().container)
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
                            tint = Color.Gray.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No subscriptions or EMIs added yet",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button to add your first subscription or EMI",
                            fontSize = 11.sp,
                            color = Color.Gray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(dueThisMonth.size) { index ->
                val sub = dueThisMonth[index]
                val dueDate = runCatching { LocalDate.parse(sub.dueDate) }.getOrNull()
                DueThisMonthCard(
                    subscription = sub,
                    formattedDate = dueDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: sub.dueDate,
                    onEdit = { onEdit(sub) },
                    onDelete = { onDelete(sub) }
                )
            }
        }

        // 4. BOTTOM ENABLE REMINDERS BANNER
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Never miss a payment. Enable reminders for all your bills.",
                            fontSize = 11.sp,
                            color = Color(0xFF15803D),
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { }
                    ) {
                        Text(
                            text = "Enable Reminders",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun TrackerStatBox(label: String, value: String, subLabel: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = subLabel,
            fontSize = 9.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun TrackerStatDivider() {
    Box(
        modifier = Modifier
            .height(36.dp)
            .width(1.dp)
            .background(Color.Gray.copy(alpha = 0.2f))
    )
}

@Composable
private fun DueSoonCard(
    subscription: Subscription,
    dueBadge: String,
    formattedDate: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = appColors()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.container),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subscription.name.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = subscription.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Text(
                        text = "${subscription.type.replaceFirstChar { it.uppercase() }} \u2022 ${subscription.subType.replaceFirstChar { it.uppercase() }}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = dueBadge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹%,.0f".format(subscription.amount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier
                    .size(16.dp)
                    .padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun DueThisMonthCard(
    subscription: Subscription,
    formattedDate: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = appColors()
    val isSubscription = subscription.type.equals("subscription", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.container),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSubscription) Color(0xFFE8F5E9) else Color(0xFFFFE8E8)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subscription.name.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSubscription) Color(0xFF15803D) else Color(0xFFEF4444)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = subscription.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${subscription.type.replaceFirstChar { it.uppercase() }} \u2022 ${subscription.subType.replaceFirstChar { it.uppercase() }}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = formattedDate,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹%,.0f".format(subscription.amount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSubscription) Color(0xFFE8F5E9) else Color(0xFFFFE8E8)
                ) {
                    Text(
                        text = subscription.type.replaceFirstChar { it.uppercase() },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSubscription) Color(0xFF15803D) else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier
                    .size(16.dp)
                    .padding(start = 4.dp)
            )
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

    var selectedType by remember { mutableStateOf(initialType) }
    var selectedSubType by remember { mutableStateOf(initialSubType) }
    var name by remember { mutableStateOf(initialName) }
    var amount by remember { mutableStateOf(initialAmount) }

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
        setOnDismissListener { showDatePicker = false }
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

            Text(text = "Type", fontSize = 14.sp, color = Color.Gray)
            DropdownField(
                value = selectedType,
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = it },
                options = typeOptions,
                onSelect = { selectedType = it; typeExpanded = false }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Name", fontSize = 18.sp, fontWeight = FontWeight.Normal, color = appColors().text)
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedTextField(
                value = name,
                singleLine = true,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                placeholder = {
                    Text(
                        text = if (selectedType == "emi") "EMI Name" else "Subscription Name",
                        style = TextStyle(fontSize = 14.sp, color = Color.Gray)
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
                textStyle = TextStyle(fontWeight = FontWeight.W500, fontSize = 16.sp, color = appColors().text)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Sub Type", fontSize = 14.sp, color = Color.Gray)
            DropdownField(
                value = selectedSubType,
                expanded = subTypeExpanded,
                onExpandedChange = { subTypeExpanded = it },
                options = subTypeOptions,
                onSelect = { selectedSubType = it; subTypeExpanded = false }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Amount", fontSize = 18.sp, fontWeight = FontWeight.Normal, color = appColors().text)
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedTextField(
                value = amount,
                singleLine = true,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                placeholder = { Text("Enter Amount", style = TextStyle(fontSize = 14.sp, color = Color.Gray)) },
                shape = RoundedCornerShape(20),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = appColors().container,
                    unfocusedContainerColor = appColors().container,
                    cursorColor = appColors().text
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.CurrencyRupee, contentDescription = "", modifier = Modifier.size(20.dp), tint = Color.Gray)
                },
                textStyle = TextStyle(fontWeight = FontWeight.W500, fontSize = 16.sp, color = appColors().text)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Date", fontSize = 18.sp, fontWeight = FontWeight.Normal, color = appColors().text)
            Spacer(modifier = Modifier.height(5.dp))
            OutlinedTextField(
                value = dueDate.format(dateFormatter),
                readOnly = true,
                singleLine = true,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(20),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = appColors().container,
                    unfocusedContainerColor = appColors().container,
                    cursorColor = appColors().text
                ),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "", modifier = Modifier.size(20.dp), tint = Color.Gray)
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "",
                        modifier = Modifier.size(20.dp).clickable {
                            if (!showDatePicker) { showDatePicker = true; datePickerDialog.show() }
                        },
                        tint = Color.Gray
                    )
                },
                textStyle = TextStyle(fontWeight = FontWeight.W500, fontSize = 16.sp, color = appColors().text)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    onSubmit(selectedType, name.trim(), selectedSubType, amountValue ?: 0.0, dueDate.toString())
                },
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appColors().green, contentColor = Color.White),
                enabled = isValid
            ) {
                Text(
                    text = submitLabel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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
            modifier = Modifier.menuAnchor().fillMaxWidth(),
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
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
