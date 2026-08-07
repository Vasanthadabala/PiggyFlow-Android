package com.piggylabs.piggyflow.features.accounts.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.navigation.AddAccount
import com.piggylabs.piggyflow.core.navigation.Stats
import com.piggylabs.piggyflow.core.navigation.Transactions
import com.piggylabs.piggyflow.core.designsystem.theme.appColors

data class DonutSlice(val value: Double, val color: Color, val label: String)

// ---------------------------------------------------------------------------
// Static presentation data for the Accounts design build.
// ---------------------------------------------------------------------------

private const val TOTAL_BALANCE_WHOLE = "₹1,64,970"
private const val TOTAL_BALANCE_DECIMALS = ".00"
private const val TOTAL_BALANCE_CHANGE = "8.5%"

private val BankAccountsColor = Color(0xFF2E7D32)
private val CreditCardsColor = Color(0xFF8BC34A)
private val WalletsColor = Color(0xFF4DB6AC)
private val CashColor = Color(0xFFF3E5A9)

private data class SummaryBucket(
    val label: String,
    val amount: String,
    val value: Double,
    val color: Color,
    val isNegative: Boolean = false
)

private val SUMMARY_BUCKETS = listOf(
    SummaryBucket("Bank Accounts", "₹1,05,560", 105560.0, BankAccountsColor),
    SummaryBucket("Credit Cards", "-₹32,340", 32340.0, CreditCardsColor, isNegative = true),
    SummaryBucket("Wallets", "₹8,750", 8750.0, WalletsColor),
    SummaryBucket("Cash", "₹8,660", 8660.0, CashColor)
)

/** Square brand badge: either a glyph or a short lettermark such as "AMEX". */
private sealed interface AccountBadge {
    data class Glyph(val icon: ImageVector) : AccountBadge
    data class Lettermark(val text: String, val fontSize: Int) : AccountBadge
}

private data class AccountRowData(
    val name: String,
    val subtitle: String,
    val amount: String,
    val amountCaption: String?,
    val badge: AccountBadge,
    val badgeColor: Color,
    val isDebt: Boolean = false
)

private val ACCOUNTS = listOf(
    AccountRowData(
        name = "SBI Bank",
        subtitle = "Savings Account •••• 1234",
        amount = "₹48,560.00",
        amountCaption = "Available Balance",
        badge = AccountBadge.Glyph(Icons.Default.AccountBalance),
        badgeColor = Color(0xFF2E7D32)
    ),
    AccountRowData(
        name = "HDFC Bank",
        subtitle = "Current Account •••• 5678",
        amount = "₹57,000.00",
        amountCaption = "Available Balance",
        badge = AccountBadge.Glyph(Icons.Default.AccountBalance),
        badgeColor = Color(0xFF9B1B3F)
    ),
    AccountRowData(
        name = "HDFC Regalia Credit Card",
        subtitle = "Credit Card •••• 2345",
        amount = "-₹28,340.00",
        amountCaption = "Outstanding",
        badge = AccountBadge.Lettermark("AMEX", 9),
        badgeColor = Color(0xFF1565C0),
        isDebt = true
    ),
    AccountRowData(
        name = "PhonePe Wallet",
        subtitle = "Wallet",
        amount = "₹5,250.00",
        amountCaption = "Available Balance",
        badge = AccountBadge.Lettermark("पे", 18),
        badgeColor = Color(0xFF5F259F)
    ),
    AccountRowData(
        name = "Cash",
        subtitle = "Physical Cash",
        amount = "₹8,660.00",
        amountCaption = null,
        badge = AccountBadge.Glyph(Icons.Default.AccountBalanceWallet),
        badgeColor = Color(0xFFF5A623)
    )
)

@Composable
fun AccountsSummaryDonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: androidx.compose.ui.unit.Dp = 18.dp
) {
    val total = slices.sumOf { it.value }
    val proportions = if (total > 0) {
        slices.map { (it.value / total).toFloat() }
    } else {
        slices.map { 0f }
    }

    Canvas(modifier = modifier) {
        var startAngle = -90f
        val stroke = strokeWidth.toPx()

        if (total == 0.0) {
            drawArc(
                color = Color.LightGray,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(stroke, cap = StrokeCap.Butt)
            )
            return@Canvas
        }

        proportions.forEachIndexed { index, proportion ->
            val sweepAngle = proportion * 360f
            if (sweepAngle > 0) {
                drawArc(
                    color = slices[index].color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Butt)
                )
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, amount: String, amountColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                color = appColors().text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = amount,
                color = amountColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(navController: NavHostController) {
    val colors = appColors()
    var hideBalance by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = { navController.popBackStack() }
                    )

                    Text(
                        text = "Accounts",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )

                    CircleIconButton(
                        icon = Icons.Default.MoreHoriz,
                        contentDescription = "More",
                        onClick = { navController.navigate(Stats.route) }
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Manage your cash, bank accounts, cards and wallets",
                    fontSize = 13.sp,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. ACCOUNTS SUMMARY
            item(key = "summary") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Accounts Summary",
                                fontWeight = FontWeight.SemiBold,
                                color = colors.text,
                                fontSize = 15.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { hideBalance = !hideBalance }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle balance",
                                    tint = colors.accent,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = if (hideBalance) "Show Balance" else "Hide Balance",
                                    color = colors.accent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Total Balance",
                                    color = colors.textMuted,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = if (hideBalance) {
                                        buildAnnotatedString {
                                            withStyle(
                                                SpanStyle(
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            ) { append("₹ ••••••") }
                                        }
                                    } else {
                                        buildAnnotatedString {
                                            withStyle(
                                                SpanStyle(
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            ) { append(TOTAL_BALANCE_WHOLE) }
                                            withStyle(
                                                SpanStyle(
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            ) { append(TOTAL_BALANCE_DECIMALS) }
                                        }
                                    },
                                    color = colors.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropUp,
                                        contentDescription = null,
                                        tint = colors.positive,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = TOTAL_BALANCE_CHANGE,
                                        color = colors.positive,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "vs last month",
                                        color = colors.textMuted,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(Modifier.width(6.dp))

                            AccountsSummaryDonutChart(
                                slices = SUMMARY_BUCKETS.map {
                                    DonutSlice(it.value, it.color, it.label)
                                },
                                modifier = Modifier.size(76.dp),
                                strokeWidth = 18.dp
                            )

                            Spacer(Modifier.width(8.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.width(92.dp)
                            ) {
                                SUMMARY_BUCKETS.forEach { bucket ->
                                    LegendItem(
                                        color = bucket.color,
                                        label = bucket.label,
                                        amount = if (hideBalance) "••••" else bucket.amount,
                                        amountColor = if (bucket.isNegative) {
                                            colors.negative
                                        } else {
                                            colors.text
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. YOUR ACCOUNTS
            item(key = "accounts_list") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Accounts",
                                fontWeight = FontWeight.Bold,
                                color = colors.text,
                                fontSize = 17.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { /* Reorder */ }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Reorder",
                                    color = colors.accent,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        ACCOUNTS.forEachIndexed { index, account ->
                            if (index > 0) Spacer(Modifier.height(10.dp))
                            AccountListRow(
                                account = account,
                                hideBalance = hideBalance,
                                onClick = { navController.navigate(AddAccount.route) }
                            )
                        }
                    }
                }
            }

            // 3. SHORTCUT TILES
            item(key = "shortcuts") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ShortcutTile(
                        icon = Icons.Default.Sync,
                        iconTint = colors.accent,
                        iconBackground = Color(0xFFDFF1E3),
                        containerColor = Color(0xFFF1F7F2),
                        title = "Recent Transactions",
                        description = "View recent transactions from all accounts",
                        actionLabel = "View All",
                        onClick = { navController.navigate(Transactions.route) },
                        modifier = Modifier.weight(1f)
                    )
                    ShortcutTile(
                        icon = Icons.Default.PieChart,
                        iconTint = Color(0xFFE0A42B),
                        iconBackground = Color(0xFFFDECC4),
                        containerColor = Color(0xFFFDF8EC),
                        title = "Spending by Account",
                        description = "See how much you've spent from each account",
                        actionLabel = "View Report",
                        onClick = { navController.navigate(Stats.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. ADD ACCOUNT
            item(key = "add_account") {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1B6B37))
                            .clickable { navController.navigate(AddAccount.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Add Account",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Your data is secure and encrypted",
                            color = colors.textMuted,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val colors = appColors()
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(colors.accentSoft)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colors.text,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun AccountListRow(
    account: AccountRowData,
    hideBalance: Boolean,
    onClick: () -> Unit
) {
    val colors = appColors()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = colors.textMuted.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(account.badgeColor),
                contentAlignment = Alignment.Center
            ) {
                when (val badge = account.badge) {
                    is AccountBadge.Glyph -> Icon(
                        imageVector = badge.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    is AccountBadge.Lettermark -> Text(
                        text = badge.text,
                        color = Color.White,
                        fontSize = badge.fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = account.subtitle,
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (hideBalance) "••••" else account.amount,
                    fontWeight = FontWeight.Bold,
                    color = if (account.isDebt) colors.negative else colors.text,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                if (account.amountCaption != null) {
                    Text(
                        text = account.amountCaption,
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ShortcutTile(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    containerColor: Color,
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = appColors()

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                fontSize = 15.sp,
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = description,
                color = colors.textMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = actionLabel,
                    color = colors.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
