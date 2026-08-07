package com.piggylabs.piggyflow.features.tracker.presentation

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.piggylabs.piggyflow.core.domain.model.BudgetPriority
import com.piggylabs.piggyflow.features.home.presentation.Category
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The four things a tracker can be. Drives the whole form below the type selector. */
enum class TrackerType(val label: String, val icon: ImageVector) {
    BUDGET("Budget", Icons.Default.TrackChanges),
    GOAL("Goal", Icons.Outlined.Stars),
    SUBSCRIPTION("Subscription", Icons.Default.EventRepeat),
    EMI("EMI / Loan", Icons.Outlined.Assignment);

    val title: String
        get() = when (this) {
            BUDGET -> "Add Tracker"
            GOAL -> "Add Goal Tracker"
            SUBSCRIPTION -> "Add Subscription"
            EMI -> "Add EMI / Loan Tracker"
        }

    val subtitle: String
        get() = when (this) {
            BUDGET -> "Track what matters. Stay on top of your goals."
            GOAL -> "Track your goal and celebrate milestones."
            SUBSCRIPTION -> "Track your recurring payments and never miss one."
            EMI -> "Track your loans and never miss an EMI"
        }

    val submitLabel: String
        get() = when (this) {
            BUDGET -> "Create Tracker"
            GOAL -> "Create Goal Tracker"
            SUBSCRIPTION -> "Create Subscription"
            EMI -> "Create EMI / Loan Tracker"
        }
}

private val PERIODS = listOf("Weekly", "Monthly", "Quarterly", "Yearly")
private val BILLING_CYCLES = listOf("Weekly", "Monthly", "Quarterly", "Yearly")
private val CURRENCIES = listOf("INR (₹)", "USD ($)", "EUR (€)", "GBP (£)")
private val REMINDERS = listOf("On due date", "1 day before", "2 days before", "1 week before")
private val PRIORITIES = listOf("High", "Medium", "Low")
private val LOAN_TYPES = listOf("Home Loan", "Car Loan", "Personal Loan", "Education Loan", "Gold Loan")
private val LENDERS = listOf("HDFC Bank", "SBI", "ICICI Bank", "Axis Bank", "Kotak Bank")
private val TENURES = listOf("5 Years", "10 Years", "15 Years", "20 Years", "25 Years", "30 Years")
private val ACCOUNTS = listOf("HDFC Bank •••• 2345", "SBI Bank •••• 1234", "Cash", "PhonePe Wallet")
private val OWNER_ACCOUNTS = listOf("Personal Account", "Business Account")

private val SWATCHES = listOf(
    Color(0xFF4CAF50),
    Color(0xFF3B82F6),
    Color(0xFF8B5CF6),
    Color(0xFFEC4899),
    Color(0xFFF97316),
    Color(0xFFF5C518),
    Color(0xFF14B8A6)
)

private data class Milestone(val percent: Int, val amount: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTrackerScreen(navController: NavHostController, initialType: String? = null) {
    val context = LocalContext.current
    val colors = appColors()
    val viewModel: AddTrackerViewModel = hiltViewModel()

    var type by rememberSaveable {
        mutableStateOf(
            when (initialType?.lowercase(Locale.ENGLISH)) {
                "goal" -> TrackerType.GOAL
                "subscription" -> TrackerType.SUBSCRIPTION
                "emi" -> TrackerType.EMI
                else -> TrackerType.BUDGET
            }
        )
    }

    // Shared across types so switching the selector keeps what the user typed.
    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf(CURRENCIES.first()) }
    var category by rememberSaveable { mutableStateOf(Category.FOOD.categoryName) }
    var description by rememberSaveable { mutableStateOf("") }
    var reminder by rememberSaveable { mutableStateOf(REMINDERS.first()) }
    var colorIndex by rememberSaveable { mutableStateOf(0) }

    // Budget
    var period by rememberSaveable { mutableStateOf("Monthly") }
    var startDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var endDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }

    // Goal
    var priority by rememberSaveable { mutableStateOf("Medium") }
    var currentAmount by rememberSaveable { mutableStateOf("") }
    var targetDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    val milestones = remember { mutableStateListOf<Milestone>() }

    // Subscription
    var billingCycle by rememberSaveable { mutableStateOf("Monthly") }
    var nextBillingDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var paymentMethod by rememberSaveable { mutableStateOf(ACCOUNTS.first()) }
    var ownerAccount by rememberSaveable { mutableStateOf(OWNER_ACCOUNTS.first()) }
    var vendor by rememberSaveable { mutableStateOf("") }
    var autoDeducts by rememberSaveable { mutableStateOf(true) }

    // EMI / Loan
    var loanType by rememberSaveable { mutableStateOf(LOAN_TYPES.first()) }
    var lender by rememberSaveable { mutableStateOf(LENDERS.first()) }
    var loanAmount by rememberSaveable { mutableStateOf("") }
    var interestRate by rememberSaveable { mutableStateOf("") }
    var tenure by rememberSaveable { mutableStateOf("20 Years") }
    var emiStartDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var nextEmiDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    val amountValue = amount.toDoubleOrNull() ?: 0.0

    fun create() {
        val name = title.trim()
        if (name.isBlank()) {
            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }
        if (amountValue <= 0.0) {
            Toast.makeText(context, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        when (type) {
            TrackerType.SUBSCRIPTION -> {
                viewModel.addSubscription(
                    type = "subscription",
                    name = name,
                    subType = billingCycle.lowercase(Locale.ENGLISH),
                    amount = amountValue,
                    dueDate = nextBillingDate.toString()
                )
                Toast.makeText(context, "Subscription created", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }

            TrackerType.EMI -> {
                viewModel.addSubscription(
                    type = "emi",
                    name = name,
                    subType = "monthly",
                    amount = amountValue,
                    dueDate = nextEmiDate.toString()
                )
                Toast.makeText(context, "EMI tracker created", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }

            TrackerType.BUDGET -> {
                val picked = Category.entries.firstOrNull { it.categoryName == category }
                viewModel.addBudgetGoal(
                    categoryName = category,
                    categoryEmoji = picked?.emoji.orEmpty(),
                    priority = BudgetPriority.NEEDS,
                    monthlyLimit = amountValue
                )
                Toast.makeText(context, "Budget tracker created", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }

            // No entity backs goal trackers yet, so nothing is written.
            TrackerType.GOAL -> Toast.makeText(
                context,
                "Goal trackers aren't saved yet",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.textMuted.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.text,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = type.title,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = type.subtitle,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = colors.textMuted,
                        maxLines = 2
                    )
                }

                Spacer(Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Templates coming soon", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Templates",
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF1B6B37))
                        .clickable { create() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircleOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = type.submitLabel,
                            color = Color.White,
                            fontSize = 17.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Save as Template",
                    color = colors.accent,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Templates coming soon", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            FieldLabel("Tracker Type")
            Spacer(Modifier.height(6.dp))
            TrackerTypeSelector(selected = type, onSelect = { type = it })

            Spacer(Modifier.height(14.dp))

            when (type) {
                TrackerType.BUDGET -> {
                    LabeledField("Title") {
                        TextFieldBox(
                            value = title,
                            onValueChange = { if (it.length <= 40) title = it },
                            placeholder = "e.g. Monthly Groceries",
                            leadingIcon = Icons.Default.Edit,
                            counter = "${title.length}/40"
                        )
                    }
                    LabeledField("Category") {
                        PickerBox(
                            value = category,
                            options = Category.entries.map { it.categoryName },
                            onSelect = { category = it },
                            leadingIcon = Icons.Default.Storefront
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("Amount / Budget", Modifier.weight(1f)) {
                            AmountBox(
                                value = amount,
                                onValueChange = { amount = it },
                                currency = currency,
                                onCurrencySelect = { currency = it }
                            )
                        }
                        LabeledField("Period", Modifier.weight(1f)) {
                            PickerBox(
                                value = period,
                                options = PERIODS,
                                onSelect = { period = it },
                                leadingIcon = Icons.Default.CalendarMonth
                            )
                        }
                    }
                    LabeledField("Start Date") {
                        DateBox(date = startDate, onDateChange = { startDate = it })
                    }
                    LabeledField("End Date (Optional)") {
                        DateBox(
                            date = endDate,
                            onDateChange = { endDate = it },
                            emptyLabel = "No end date"
                        )
                    }
                    LabeledField("Description (Optional)") {
                        TextFieldBox(
                            value = description,
                            onValueChange = { if (it.length <= 100) description = it },
                            placeholder = "Add details about this tracker...",
                            leadingIcon = Icons.Default.Description,
                            counter = "${description.length}/100",
                            minHeight = 92.dp,
                            singleLine = false
                        )
                    }
                    LabeledField("Set Alert / Reminder") {
                        ReminderBox(
                            value = reminder,
                            onSelect = { reminder = it }
                        )
                    }
                    LabeledField("Icon & Color") {
                        IconAndColorRow(
                            icon = Icons.Default.Storefront,
                            selectedColor = colorIndex,
                            onColorSelect = { colorIndex = it }
                        )
                    }
                    PromoBanner(
                        title = "Stay consistent, achieve more!",
                        body = "Track your progress and achieve your financial goals."
                    )
                }

                TrackerType.GOAL -> {
                    LabeledField("Goal Title") {
                        TextFieldBox(
                            value = title,
                            onValueChange = { if (it.length <= 40) title = it },
                            placeholder = "e.g. Buy New Laptop",
                            leadingIcon = Icons.Default.Edit,
                            counter = "${title.length}/40"
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("Category", Modifier.weight(1f)) {
                            PickerBox(
                                value = category,
                                options = Category.entries.map { it.categoryName },
                                onSelect = { category = it },
                                leadingIcon = Icons.Default.Storefront
                            )
                        }
                        LabeledField("Priority", Modifier.weight(1f)) {
                            PickerBox(
                                value = priority,
                                options = PRIORITIES,
                                onSelect = { priority = it },
                                leadingIcon = Icons.Default.TrackChanges
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("Target Amount", Modifier.weight(1f)) {
                            AmountBox(
                                value = amount,
                                onValueChange = { amount = it },
                                currency = currency,
                                onCurrencySelect = { currency = it }
                            )
                        }
                        LabeledField("Current Amount (Optional)", Modifier.weight(1f)) {
                            AmountBox(
                                value = currentAmount,
                                onValueChange = { currentAmount = it },
                                currency = null,
                                onCurrencySelect = {}
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("Target Date", Modifier.weight(1f)) {
                            DateBox(date = targetDate, onDateChange = { targetDate = it })
                        }
                        LabeledField("Remind me", Modifier.weight(1f)) {
                            PickerBox(
                                value = reminder,
                                options = REMINDERS,
                                onSelect = { reminder = it },
                                leadingIcon = Icons.Default.NotificationsNone
                            )
                        }
                    }
                    LabeledField("Milestones (Optional)") {
                        MilestoneList(
                            milestones = milestones,
                            onDelete = { milestones.remove(it) },
                            onAdd = {
                                val next = when (milestones.size) {
                                    0 -> 25
                                    1 -> 50
                                    2 -> 75
                                    else -> 100
                                }
                                milestones.add(Milestone(next, amountValue * next / 100))
                            }
                        )
                    }
                    LabeledField("Description (Optional)") {
                        TextFieldBox(
                            value = description,
                            onValueChange = { if (it.length <= 100) description = it },
                            placeholder = "Add a note about your goal...",
                            leadingIcon = Icons.Default.Description,
                            counter = "${description.length}/100",
                            minHeight = 92.dp,
                            singleLine = false
                        )
                    }
                    LabeledField("Icon & Color") {
                        IconAndColorRow(
                            icon = Icons.Outlined.Stars,
                            selectedColor = colorIndex,
                            onColorSelect = { colorIndex = it }
                        )
                    }
                    PromoBanner(
                        title = "Stay focused. Achieve your dreams!",
                        body = "Track your progress and turn your goals into reality."
                    )
                }

                TrackerType.SUBSCRIPTION -> {
                    LabeledField("Subscription Name") {
                        TextFieldBox(
                            value = title,
                            onValueChange = { if (it.length <= 40) title = it },
                            placeholder = "e.g. Netflix Premium",
                            leadingIcon = Icons.Default.EventRepeat,
                            counter = "${title.length}/40"
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("Category", Modifier.weight(1f)) {
                            PickerBox(
                                value = category,
                                options = Category.entries.map { it.categoryName },
                                onSelect = { category = it },
                                leadingIcon = Icons.Default.Storefront
                            )
                        }
                        LabeledField("Billing Cycle", Modifier.weight(1f)) {
                            PickerBox(
                                value = billingCycle,
                                options = BILLING_CYCLES,
                                onSelect = { billingCycle = it },
                                leadingIcon = Icons.Default.EventRepeat
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("Amount", Modifier.weight(1f)) {
                            AmountBox(
                                value = amount,
                                onValueChange = { amount = it },
                                currency = currency,
                                onCurrencySelect = { currency = it }
                            )
                        }
                        LabeledField("Next Billing Date", Modifier.weight(1f)) {
                            DateBox(
                                date = nextBillingDate,
                                onDateChange = { nextBillingDate = it }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("Payment Method", Modifier.weight(1f)) {
                            PickerBox(
                                value = paymentMethod,
                                options = ACCOUNTS,
                                onSelect = { paymentMethod = it },
                                leadingIcon = Icons.Default.AccountBalance
                            )
                        }
                        LabeledField("Account", Modifier.weight(1f)) {
                            PickerBox(
                                value = ownerAccount,
                                options = OWNER_ACCOUNTS,
                                onSelect = { ownerAccount = it },
                                leadingIcon = Icons.Default.AccountBalanceWallet
                            )
                        }
                    }
                    LabeledField("Vendor / Provider (Optional)") {
                        TextFieldBox(
                            value = vendor,
                            onValueChange = { if (it.length <= 40) vendor = it },
                            placeholder = "e.g. Netflix, Spotify, Amazon Prime",
                            leadingIcon = Icons.Default.Storefront,
                            counter = "${vendor.length}/40"
                        )
                    }
                    ToggleBox(
                        title = "Auto-deducts from account",
                        subtitle = "Enable if the amount is auto-deducted on due date",
                        checked = autoDeducts,
                        onCheckedChange = { autoDeducts = it }
                    )
                    Spacer(Modifier.height(14.dp))
                    LabeledField("Set Reminder") {
                        ReminderBox(value = reminder, onSelect = { reminder = it })
                    }
                    LabeledField("Notes (Optional)") {
                        TextFieldBox(
                            value = description,
                            onValueChange = { if (it.length <= 100) description = it },
                            placeholder = "Add a note about this subscription...",
                            leadingIcon = Icons.Default.Description,
                            counter = "${description.length}/100",
                            minHeight = 92.dp,
                            singleLine = false
                        )
                    }
                    LabeledField("Icon & Color") {
                        IconAndColorRow(
                            icon = Icons.Default.EventRepeat,
                            selectedColor = colorIndex,
                            onColorSelect = { colorIndex = it }
                        )
                    }
                    SubscriptionSummary(
                        amount = amountValue,
                        cycle = billingCycle,
                        nextBillingDate = nextBillingDate
                    )
                }

                TrackerType.EMI -> {
                    LabeledField("Loan / EMI Name") {
                        TextFieldBox(
                            value = title,
                            onValueChange = { if (it.length <= 40) title = it },
                            placeholder = "e.g. Home Loan, Car Loan",
                            leadingIcon = Icons.Default.AccountBalance,
                            counter = "${title.length}/40"
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("Loan Type", Modifier.weight(1f)) {
                            PickerBox(
                                value = loanType,
                                options = LOAN_TYPES,
                                onSelect = { loanType = it },
                                leadingIcon = Icons.Default.AccountBalance
                            )
                        }
                        LabeledField("Lender / Bank", Modifier.weight(1f)) {
                            PickerBox(
                                value = lender,
                                options = LENDERS,
                                onSelect = { lender = it },
                                leadingIcon = Icons.Default.AccountBalance
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("Loan Amount", Modifier.weight(1f)) {
                            AmountBox(
                                value = loanAmount,
                                onValueChange = { loanAmount = it },
                                currency = null,
                                onCurrencySelect = {}
                            )
                        }
                        LabeledField("Interest Rate (p.a.)", Modifier.weight(1f)) {
                            AmountBox(
                                value = interestRate,
                                onValueChange = { interestRate = it },
                                currency = null,
                                onCurrencySelect = {},
                                leadingIcon = Icons.Default.Percent,
                                placeholder = "0.00",
                                suffix = "%"
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("EMI Amount", Modifier.weight(1f)) {
                            AmountBox(
                                value = amount,
                                onValueChange = { amount = it },
                                currency = null,
                                onCurrencySelect = {}
                            )
                        }
                        LabeledField("Tenure", Modifier.weight(1f)) {
                            PickerBox(
                                value = tenure,
                                options = TENURES,
                                onSelect = { tenure = it },
                                leadingIcon = Icons.Default.Schedule
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LabeledField("EMI Start Date", Modifier.weight(1f)) {
                            DateBox(date = emiStartDate, onDateChange = { emiStartDate = it })
                        }
                        LabeledField("Next EMI Date", Modifier.weight(1f)) {
                            DateBox(date = nextEmiDate, onDateChange = { nextEmiDate = it })
                        }
                    }
                    LabeledField("Payment Account") {
                        PickerBox(
                            value = paymentMethod,
                            options = ACCOUNTS,
                            onSelect = { paymentMethod = it },
                            leadingIcon = Icons.Default.AccountBalanceWallet
                        )
                    }
                    LabeledField("Set Reminder") {
                        ReminderBox(value = reminder, onSelect = { reminder = it })
                    }
                    LoanSummary(
                        emiAmount = amountValue,
                        tenureYears = tenure.filter { it.isDigit() }.toIntOrNull() ?: 0,
                        loanAmount = loanAmount.toDoubleOrNull() ?: 0.0
                    )
                    Spacer(Modifier.height(14.dp))
                    LabeledField("Notes (Optional)") {
                        TextFieldBox(
                            value = description,
                            onValueChange = { if (it.length <= 100) description = it },
                            placeholder = "Add a note about this loan...",
                            leadingIcon = Icons.Default.Description,
                            counter = "${description.length}/100",
                            minHeight = 92.dp,
                            singleLine = false
                        )
                    }
                    LabeledField("Icon & Color") {
                        IconAndColorRow(
                            icon = Icons.Default.AccountBalance,
                            selectedColor = colorIndex,
                            onColorSelect = { colorIndex = it }
                        )
                    }
                    PromoBanner(
                        title = "Stay on track and become debt free!",
                        body = "We'll remind you so you never miss an important EMI."
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Building blocks
// ---------------------------------------------------------------------------

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        color = appColors().textMuted
    )
}

/** Label above a control, with the spacing the form uses between rows. */
@Composable
private fun ColumnScope.LabeledField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.padding(bottom = 14.dp)) {
        FieldLabel(label)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun RowScope.LabeledField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.padding(bottom = 14.dp)) {
        FieldLabel(label)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun TrackerTypeSelector(selected: TrackerType, onSelect: (TrackerType) -> Unit) {
    val colors = appColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.textMuted.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
    ) {
        TrackerType.entries.forEach { entry ->
            val isSelected = entry == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) colors.accentSoft else Color.Transparent)
                    .then(
                        if (isSelected) {
                            Modifier.border(
                                1.5.dp,
                                colors.accent.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onSelect(entry) }
                    .padding(horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    tint = if (isSelected) colors.accent else colors.text,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = entry.label,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colors.accent else colors.text,
                    maxLines = 1
                )
            }
        }
    }
}

/** The bordered white container every control sits in. */
@Composable
private fun FieldContainer(
    modifier: Modifier = Modifier,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val colors = appColors()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.textMuted.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** Round tinted chip that leads every field. */
@Composable
private fun LeadingChip(icon: ImageVector, tint: Color = appColors().accent) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(appColors().accentSoft),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TextFieldBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    counter: String? = null,
    minHeight: androidx.compose.ui.unit.Dp = 56.dp,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val colors = appColors()

    FieldContainer(minHeight = minHeight) {
        Column {
            LeadingChip(leadingIcon)
        }

        Spacer(Modifier.width(10.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    color = colors.textMuted
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    color = colors.text
                ),
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (counter != null) {
            Spacer(Modifier.width(8.dp))
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.heightIn(min = 36.dp)
            ) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = counter,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun AmountBox(
    value: String,
    onValueChange: (String) -> Unit,
    currency: String?,
    onCurrencySelect: (String) -> Unit,
    leadingIcon: ImageVector = Icons.Default.CurrencyRupee,
    placeholder: String = "0.00",
    suffix: String? = null
) {
    val colors = appColors()
    var expanded by remember { mutableStateOf(false) }

    FieldContainer {
        LeadingChip(leadingIcon)

        Spacer(Modifier.width(10.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    color = colors.textMuted
                )
            }
            BasicTextField(
                value = value,
                onValueChange = { input ->
                    onValueChange(input.filter { it.isDigit() || it == '.' })
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.text
                ),
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (suffix != null) {
            Text(
                text = suffix,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                color = colors.text
            )
        }

        if (currency != null) {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { expanded = true }
                ) {
                    Text(
                        text = currency,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        color = colors.text,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    CURRENCIES.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onCurrencySelect(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerBox(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    leadingIcon: ImageVector
) {
    val colors = appColors()
    var expanded by remember { mutableStateOf(false) }

    Box {
        FieldContainer(onClick = { expanded = true }) {
            LeadingChip(leadingIcon)
            Spacer(Modifier.width(10.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DateBox(
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
    emptyLabel: String = "Select date"
) {
    val colors = appColors()
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH) }
    val anchor = date ?: LocalDate.now()

    FieldContainer(
        onClick = {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    onDateChange(LocalDate.of(year, month + 1, dayOfMonth))
                },
                anchor.year,
                anchor.monthValue - 1,
                anchor.dayOfMonth
            ).show()
        }
    ) {
        LeadingChip(Icons.Default.CalendarMonth)
        Spacer(Modifier.width(10.dp))
        Text(
            text = date?.format(formatter) ?: emptyLabel,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = if (date != null) FontWeight.Medium else FontWeight.Normal,
            color = if (date != null) colors.text else colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ReminderBox(value: String, onSelect: (String) -> Unit) {
    val colors = appColors()
    var expanded by remember { mutableStateOf(false) }

    Box {
        FieldContainer(onClick = { expanded = true }) {
            LeadingChip(Icons.Default.NotificationsNone)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Remind me",
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
                color = colors.text,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                color = colors.text,
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            REMINDERS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ToggleBox(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = appColors()

    FieldContainer(minHeight = 66.dp) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = colors.textMuted
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.accent
            )
        )
    }
}

@Composable
private fun IconAndColorRow(
    icon: ImageVector,
    selectedColor: Int,
    onColorSelect: (Int) -> Unit
) {
    val colors = appColors()
    val accentColor = SWATCHES[selectedColor.coerceIn(SWATCHES.indices)]

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accentColor.copy(alpha = 0.15f))
                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface)
                .border(1.dp, colors.textMuted.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SWATCHES.forEachIndexed { index, swatch ->
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .then(
                            if (index == selectedColor) {
                                Modifier.border(2.dp, swatch, CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(swatch)
                        .clickable { onColorSelect(index) }
                )
            }
        }
    }
}

@Composable
private fun MilestoneList(
    milestones: List<Milestone>,
    onDelete: (Milestone) -> Unit,
    onAdd: () -> Unit
) {
    val colors = appColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.textMuted.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        milestones.forEach { milestone ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 3.dp.toPx()
                        drawArc(
                            color = colors.accent.copy(alpha = 0.15f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = colors.accent,
                            startAngle = -90f,
                            sweepAngle = 360f * milestone.percent / 100f,
                            useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "${milestone.percent}%",
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${milestone.percent}% of goal",
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Text(
                        text = "₹%,.0f".format(milestone.amount),
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = colors.textMuted
                    )
                }

                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit milestone",
                    tint = colors.textMuted,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDelete(milestone) }
                )
                Spacer(Modifier.width(14.dp))
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete milestone",
                    tint = colors.textMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDelete(milestone) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    1.dp,
                    colors.textMuted.copy(alpha = 0.35f),
                    RoundedCornerShape(12.dp)
                )
                .clickable { onAdd() },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Add Milestone",
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accent
                )
            }
        }
    }
}

@Composable
private fun SubscriptionSummary(amount: Double, cycle: String, nextBillingDate: LocalDate) {
    val colors = appColors()
    val formatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH) }
    val perYear = when (cycle) {
        "Weekly" -> 52
        "Quarterly" -> 4
        "Yearly" -> 1
        else -> 12
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.accentSoft)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = colors.accent,
                    startAngle = -90f,
                    sweepAngle = 300f,
                    useCenter = false,
                    style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "You'll pay ₹%,.2f every %s".format(
                    amount,
                    cycle.lowercase(Locale.ENGLISH).removeSuffix("ly").ifBlank { "month" }
                        .let { if (cycle == "Monthly") "month" else cycle.lowercase(Locale.ENGLISH) }
                ),
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Spacer(Modifier.height(6.dp))
            SummaryLine(
                icon = Icons.Default.Schedule,
                label = "Next billing date",
                value = nextBillingDate.format(formatter)
            )
            Spacer(Modifier.height(3.dp))
            SummaryLine(
                icon = Icons.Default.EventRepeat,
                label = "Annual cost",
                value = "₹%,.2f".format(amount * perYear)
            )
        }
    }
}

@Composable
private fun SummaryLine(icon: ImageVector, label: String, value: String) {
    val colors = appColors()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            color = colors.textMuted,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent
        )
    }
}

@Composable
private fun LoanSummary(emiAmount: Double, tenureYears: Int, loanAmount: Double) {
    val colors = appColors()
    val totalEmis = tenureYears * 12
    val totalPayable = emiAmount * totalEmis
    val totalInterest = (totalPayable - loanAmount).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.accentSoft)
            .padding(16.dp)
    ) {
        Text(
            text = "Loan Summary",
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            LoanStat("Total Payable", "₹ %,.0f".format(totalPayable), colors.accent, Modifier.weight(1.1f))
            LoanStat("Total Interest", "₹ %,.0f".format(totalInterest), colors.text, Modifier.weight(1f))
            LoanStat("Total EMIs", "$totalEmis", colors.text, Modifier.weight(0.7f))
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0 of $totalEmis EMIs paid",
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = colors.textMuted
            )
            Text(
                text = "0%",
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = colors.textMuted
            )
        }

        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.textMuted.copy(alpha = 0.25f))
        )
    }
}

@Composable
private fun LoanStat(label: String, value: String, valueColor: Color, modifier: Modifier) {
    val colors = appColors()
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            color = colors.textMuted,
            maxLines = 1
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PromoBanner(title: String, body: String) {
    val colors = appColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.accentSoft)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = body,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = colors.textMuted
            )
        }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TrackChanges,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
