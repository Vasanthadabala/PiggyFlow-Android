package com.piggylabs.piggyflow.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.domain.model.AccountType
import com.piggylabs.piggyflow.core.navigation.About
import com.piggylabs.piggyflow.core.navigation.Accounts
import com.piggylabs.piggyflow.core.navigation.AddAccount
import com.piggylabs.piggyflow.core.navigation.AddExpense
import com.piggylabs.piggyflow.core.navigation.AddIncome
import com.piggylabs.piggyflow.core.navigation.BudgetGoals
import com.piggylabs.piggyflow.core.navigation.Notification
import com.piggylabs.piggyflow.core.navigation.Profile
import com.piggylabs.piggyflow.core.navigation.ScanBill
import com.piggylabs.piggyflow.core.navigation.Stats
import com.piggylabs.piggyflow.core.navigation.Tracker
import com.piggylabs.piggyflow.core.navigation.Transactions
import com.piggylabs.piggyflow.core.navigation.BottomBar
import com.piggylabs.piggyflow.features.home.presentation.components.CategoryDonutChart
import com.piggylabs.piggyflow.features.home.presentation.components.DonutSlice
import com.piggylabs.piggyflow.features.home.presentation.components.SavingsGaugeChart
import com.piggylabs.piggyflow.features.home.presentation.components.SpendBarChart
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import java.time.LocalTime

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

    val emoji: String
        get() = label.substringBefore(" ")

    val categoryName: String
        get() = label.substringAfter(" ")
}

// ---------------------------------------------------------------------------
// Static presentation data. The screen is a design build of the Home mockup, so
// every figure below is fixed sample content rather than something read from the
// database.
// ---------------------------------------------------------------------------

private const val USER_NAME = "Vasanth"
private const val NET_BALANCE_WHOLE = "₹1,24,560"
private const val NET_BALANCE_DECIMALS = ".00"
private const val NET_BALANCE_CHANGE = "12.5%"
private const val TOTAL_INCOME = "₹2,45,000"
private const val TOTAL_EXPENSES = "₹1,20,440"
private const val SAVINGS_RATE = "50.7%"
private const val MONTH_TOTAL_SPEND = "₹1,20,440"
private const val DUE_SOON_LABEL = "2 payments due soon"

private val SPEND_SERIES = listOf(
    44.0, 58.0, 80.0, 52.0, 70.0, 64.0, 68.0, 46.0, 96.0, 60.0, 42.0
)
private val SPEND_AXIS_LABELS = listOf("1 May", "8 May", "15 May", "22 May", "31 May")

private data class QuickActionData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val route: String
)

private data class AccountRowData(
    val name: String,
    val type: String,
    val amount: String,
    val icon: ImageVector,
    val isDebt: Boolean
)

private data class UpcomingPaymentData(
    val name: String,
    val type: String,
    val dueLabel: String,
    val amount: String,
    val logo: PaymentLogo
)

private sealed interface PaymentLogo {
    data class Lettermark(
        val letter: String,
        val background: Color,
        val foreground: Color,
        val shape: LogoShape
    ) : PaymentLogo

    data class Glyph(
        val icon: ImageVector,
        val background: Color,
        val foreground: Color,
        val shape: LogoShape
    ) : PaymentLogo
}

private enum class LogoShape { SQUARE, CIRCLE }

private data class CategoryLegendData(
    val name: String,
    val amount: String,
    val share: String,
    val value: Double,
    val color: Color
)

private data class RecentTransactionData(
    val title: String,
    val category: String,
    val amount: String,
    val timestamp: String,
    val lettermark: String,
    val isExpense: Boolean
)

// Chart colours are pinned to the mockup rather than pulled from the theme palette
// so the donut and its legend read exactly as designed.
private val CategoryColors = listOf(
    Color(0xFF2E7D32),
    Color(0xFF8BC34A),
    Color(0xFFF5C518),
    Color(0xFF14B8A6),
    Color(0xFFD5D9D6)
)

private val TOP_CATEGORIES = listOf(
    CategoryLegendData("Food & Dining", "₹32,450", "27%", 32450.0, CategoryColors[0]),
    CategoryLegendData("Shopping", "₹22,300", "19%", 22300.0, CategoryColors[1]),
    CategoryLegendData("Fuel", "₹18,650", "15%", 18650.0, CategoryColors[2]),
    CategoryLegendData("Utilities", "₹12,450", "10%", 12450.0, CategoryColors[3]),
    CategoryLegendData("Others", "₹34,590", "29%", 34590.0, CategoryColors[4])
)

private val ACCOUNTS = listOf(
    AccountRowData("SBI Bank", "Savings Account", "₹48,560", Icons.Default.AccountBalance, false),
    AccountRowData("HDFC Credit Card", "Credit Card", "-₹12,340", Icons.Default.CreditCard, true),
    AccountRowData("Cash", "Wallet", "₹8,750", Icons.Default.AccountBalanceWallet, false)
)

private val UPCOMING_PAYMENTS = listOf(
    UpcomingPaymentData(
        name = "Netflix",
        type = "Subscription",
        dueLabel = "May 18",
        amount = "₹649",
        logo = PaymentLogo.Lettermark(
            letter = "N",
            background = Color(0xFF111111),
            foreground = Color(0xFFE50914),
            shape = LogoShape.SQUARE
        )
    ),
    UpcomingPaymentData(
        name = "Spotify Premium",
        type = "Subscription",
        dueLabel = "May 20",
        amount = "₹119",
        logo = PaymentLogo.Glyph(
            icon = Icons.Default.MusicNote,
            background = Color(0xFF1DB954),
            foreground = Color.White,
            shape = LogoShape.CIRCLE
        )
    ),
    UpcomingPaymentData(
        name = "Home Loan EMI",
        type = "EMI",
        dueLabel = "May 25",
        amount = "₹28,500",
        logo = PaymentLogo.Glyph(
            icon = Icons.Default.AccountBalance,
            background = Color(0xFFE8F5E9),
            foreground = Color(0xFF15803D),
            shape = LogoShape.SQUARE
        )
    )
)

private val RECENT_TRANSACTIONS = listOf(
    RecentTransactionData("Amazon India", "Shopping", "-₹2,450", "Today, 10:30 AM", "a", true),
    RecentTransactionData("Swiggy", "Food & Dining", "-₹580", "Today, 9:05 AM", "S", true),
    RecentTransactionData("Monthly Salary", "Salary", "+₹2,45,000", "Yesterday, 6:00 PM", "M", false),
    RecentTransactionData("Indian Oil", "Fuel", "-₹3,200", "Yesterday, 8:40 AM", "I", true),
    RecentTransactionData("Electricity Bill", "Utilities", "-₹1,860", "14 May, 7:15 PM", "E", true)
)

@ExperimentalMaterial3Api
@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeViewModel) {
    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            HomeScreenComponent(navController = navController)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun HomeScreenComponent(navController: NavHostController) {
    val colors = appColors()
    var showMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. TOP HEADER BAR
        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Card(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = colors.text,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Accounts") },
                            onClick = {
                                showMenu = false
                                navController.navigate(Accounts.route) { launchSingleTop = true }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("All Transactions") },
                            onClick = {
                                showMenu = false
                                navController.navigate(Transactions.route) { launchSingleTop = true }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                showMenu = false
                                navController.navigate(Profile.route) { launchSingleTop = true }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("About") },
                            onClick = {
                                showMenu = false
                                navController.navigate(About.route) { launchSingleTop = true }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Good morning, $USER_NAME",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "👋", fontSize = 15.sp)
                    }
                    Text(
                        text = "Here's your financial overview",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = colors.text,
                        modifier = Modifier
                            .size(25.dp)
                            .clickable {
                                navController.navigate(Notification.route) {
                                    launchSingleTop = true
                                }
                            }
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp, end = 5.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .clickable {
                            navController.navigate(Profile.route) { launchSingleTop = true }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = USER_NAME.first().uppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. NET BALANCE HERO CARD
        item(key = "balance") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(214.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(colors = colors.heroGradient),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(18.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.18f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            start = 6.dp,
                                            end = 10.dp,
                                            top = 3.dp,
                                            bottom = 3.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropUp,
                                            contentDescription = null,
                                            tint = Color(0xFF86EFAC),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = NET_BALANCE_CHANGE,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF86EFAC)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "vs last month",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Net Balance",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Balance visibility",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            SpanStyle(
                                                fontSize = 31.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) { append(NET_BALANCE_WHOLE) }
                                        withStyle(
                                            SpanStyle(
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) { append(NET_BALANCE_DECIMALS) }
                                    },
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HeroStat(
                                    label = "Income",
                                    value = TOTAL_INCOME,
                                    valueColor = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                HeroDivider()
                                HeroStat(
                                    label = "Expenses",
                                    value = TOTAL_EXPENSES,
                                    valueColor = Color(0xFFFCA5A5),
                                    modifier = Modifier.weight(1.05f)
                                )
                                HeroDivider()
                                HeroStat(
                                    label = "Savings Rate",
                                    value = SAVINGS_RATE,
                                    valueColor = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .width(112.dp)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            SavingsGaugeChart(savingsRate = 50.7f)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .clickable { navController.navigate(Stats.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Balance card options",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 3. QUICK ACTIONS
        item(key = "quick_actions") {
            val quickActions = listOf(
                QuickActionData(
                    "Scan Bill", "AI Extract", Icons.Default.CameraAlt,
                    colors.onAccentSoft, colors.accentSoft, ScanBill.route
                ),
                QuickActionData(
                    "Add Expense", "Manually", Icons.Default.Edit,
                    colors.onAccentSoft, colors.accentSoft, AddExpense.route
                ),
                QuickActionData(
                    "Add Income", "Record", Icons.Default.AccountBalanceWallet,
                    colors.onAccentSoft, colors.accentSoft, AddIncome.route
                ),
                QuickActionData(
                    "Trackers", "EMI, Subs", Icons.Default.CalendarMonth,
                    Color(0xFFF97316), Color(0xFFFFF1E4), Tracker.route
                ),
                QuickActionData(
                    "Budget Goals", "Set Limit", Icons.Default.TrackChanges,
                    Color(0xFFEF4444), Color(0xFFFDE8E8), BudgetGoals.route
                )
            )

            // All five share the row width, so the set reads as one strip with no scroll.
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickActions.forEach { action ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(104.dp)
                            .clickable {
                                navController.navigate(action.route) { launchSingleTop = true }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 3.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(action.iconBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = null,
                                    tint = action.iconTint,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = action.title,
                                fontSize = 9.sp,
                                // Explicit line heights: the default 24sp would push the
                                // subtitle past the card's bottom edge.
                                lineHeight = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = action.subtitle,
                                fontSize = 8.sp,
                                lineHeight = 10.sp,
                                color = colors.textMuted,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 4. ACCOUNTS & UPCOMING PAYMENTS
        item(key = "accounts_payments") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(238.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            SectionHeadingRow(
                                title = "Accounts",
                                actionLabel = "View all",
                                onActionClick = { navController.navigate(Accounts.route) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            ACCOUNTS.forEachIndexed { index, account ->
                                if (index > 0) {
                                    RowDivider()
                                }
                                AccountRow(
                                    account = account,
                                    onClick = { navController.navigate(Accounts.route) }
                                )
                            }
                        }

                        SoftActionBar(
                            icon = Icons.Default.Add,
                            label = "Add Account",
                            onClick = { navController.navigate(AddAccount.route) }
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(238.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            SectionHeadingRow(
                                title = "Upcoming Payments",
                                actionLabel = "View all",
                                onActionClick = { navController.navigate(Tracker.route) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            UPCOMING_PAYMENTS.forEachIndexed { index, payment ->
                                if (index > 0) {
                                    RowDivider()
                                }
                                UpcomingPaymentRow(payment = payment)
                            }
                        }

                        SoftActionBar(
                            icon = Icons.Default.AlarmOn,
                            label = DUE_SOON_LABEL,
                            trailingIcon = Icons.Default.ChevronRight,
                            onClick = { navController.navigate(Tracker.route) }
                        )
                    }
                }
            }
        }

        // 5. THIS MONTH OVERVIEW
        item(key = "month_overview") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "This Month Overview",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { navController.navigate(Stats.route) }
                        ) {
                            Text(
                                text = "View Report",
                                fontSize = 13.sp,
                                color = colors.accent,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(0.46f)) {
                            Text(
                                text = "Total Spend",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = MONTH_TOTAL_SPEND,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            SpendBarChart(
                                values = SPEND_SERIES,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(88.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SPEND_AXIS_LABELS.forEach { label ->
                                    Text(
                                        text = label,
                                        fontSize = 8.sp,
                                        color = colors.textMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(0.54f)) {
                            Text(
                                text = "Top Categories",
                                fontSize = 12.sp,
                                color = colors.textMuted,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryDonutChart(
                                    slices = TOP_CATEGORIES.map {
                                        DonutSlice(
                                            label = it.name,
                                            value = it.value,
                                            color = it.color
                                        )
                                    },
                                    modifier = Modifier.size(56.dp),
                                    strokeWidth = 12.dp
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    TOP_CATEGORIES.forEach { category ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(category.color)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = category.name,
                                                fontSize = 9.sp,
                                                color = colors.text,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = category.amount,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.text,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = category.share,
                                                fontSize = 8.sp,
                                                color = colors.textMuted,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SoftActionBar(
                        icon = Icons.Default.Insights,
                        label = "View All Insights",
                        trailingIcon = Icons.Default.ChevronRight,
                        height = 42.dp,
                        onClick = { navController.navigate(Stats.route) }
                    )
                }
            }
        }

        // 6. RECENT TRANSACTIONS
        item(key = "recent_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Text(
                    text = "View all",
                    fontSize = 13.sp,
                    color = colors.accent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        navController.navigate(Transactions.route)
                    }
                )
            }
        }

        items(RECENT_TRANSACTIONS, key = { it.title }) { transaction ->
            RecentTransactionRow(
                transaction = transaction,
                onClick = { navController.navigate(Transactions.route) }
            )
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

@Composable
private fun HeroStat(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1
        )
    }
}

/** Hairline separator between the three stats at the foot of the hero card. */
@Composable
private fun HeroDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .width(1.dp)
            .height(30.dp)
            .background(Color.White.copy(alpha = 0.25f))
    )
}

@Composable
private fun RowDivider() {
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(appColors().textMuted.copy(alpha = 0.15f))
    )
}

@Composable
private fun SectionHeadingRow(
    title: String,
    actionLabel: String?,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors().text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (actionLabel != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = actionLabel,
                fontSize = 9.sp,
                color = appColors().accent,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

/** A tonal, borderless call-to-action bar used at the foot of the summary cards. */
@Composable
private fun SoftActionBar(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    trailingIcon: ImageVector? = null,
    height: Dp = 38.dp
) {
    val colors = appColors()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.accentSoft)
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (trailingIcon != null) {
                Arrangement.SpaceBetween
            } else {
                Arrangement.Center
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onAccentSoft,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onAccentSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = colors.onAccentSoft,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AccountRow(account: AccountRowData, onClick: () -> Unit) {
    val colors = appColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.accent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = account.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.name,
                fontSize = 10.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = account.type,
                fontSize = 8.sp,
                lineHeight = 18.sp,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        Text(
            text = account.amount,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (account.isDebt) colors.negative else colors.text,
            maxLines = 1
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(11.dp)
        )
    }
}

@Composable
private fun UpcomingPaymentRow(payment: UpcomingPaymentData) {
    val colors = appColors()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PaymentLogoBadge(logo = payment.logo)

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = payment.name,
                fontSize = 10.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = payment.type,
                fontSize = 8.sp,
                lineHeight = 18.sp,
                color = colors.textMuted,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = payment.dueLabel,
                fontSize = 8.sp,
                lineHeight = 18.sp,
                color = colors.textMuted,
                maxLines = 1
            )
            Text(
                text = payment.amount,
                fontSize = 9.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PaymentLogoBadge(logo: PaymentLogo) {
    val shape = when (logo) {
        is PaymentLogo.Lettermark -> logo.shape
        is PaymentLogo.Glyph -> logo.shape
    }
    val background = when (logo) {
        is PaymentLogo.Lettermark -> logo.background
        is PaymentLogo.Glyph -> logo.background
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(
                if (shape == LogoShape.CIRCLE) CircleShape else RoundedCornerShape(8.dp)
            )
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        when (logo) {
            is PaymentLogo.Lettermark -> Text(
                text = logo.letter,
                color = logo.foreground,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            is PaymentLogo.Glyph -> Icon(
                imageVector = logo.icon,
                contentDescription = null,
                tint = logo.foreground,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun RecentTransactionRow(transaction: RecentTransactionData, onClick: () -> Unit) {
    val colors = appColors()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceMuted),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.lettermark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.category,
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = transaction.amount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.isExpense) colors.negative else colors.positive,
                    maxLines = 1
                )
                Text(
                    text = transaction.timestamp,
                    fontSize = 11.sp,
                    color = colors.textMuted,
                    maxLines = 1
                )
            }
        }
    }
}

/** Shared icon mapping so Home and the Accounts screen show the same glyph per type. */
fun accountIconFor(type: String): ImageVector = when (type) {
    AccountType.CREDIT_CARD -> Icons.Default.CreditCard
    AccountType.WALLET -> Icons.Default.AccountBalanceWallet
    AccountType.CASH -> Icons.Default.Savings
    AccountType.BUSINESS -> Icons.Default.Storefront
    else -> Icons.Default.AccountBalance
}

fun getGreetingByTime(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        in 17..20 -> "Good evening,"
        else -> "Good night,"
    }
}
