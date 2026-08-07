package com.piggylabs.piggyflow.features.notification.presentation

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import com.piggylabs.piggyflow.core.domain.model.Subscription
import com.piggylabs.piggyflow.core.navigation.TopBar
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.piggylabs.piggyflow.features.tracker.presentation.TrackerViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.piggylabs.piggyflow.core.di.preferencesManager

data class TrackerReminderUi(
    val id: Int,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val dueDate: LocalDate,
    val isDueToday: Boolean,
    val notificationKey: String
)


@ExperimentalMaterial3Api
@Composable
fun NotificationScreen(navController: NavHostController){
    val context = LocalContext.current
    val trackerViewModel: TrackerViewModel = hiltViewModel()
    val uiState by trackerViewModel.uiState.collectAsStateWithLifecycle()
    var clearedKeys by remember { mutableStateOf(getClearedTrackerNotificationKeys(context)) }

    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ){
            NotificationScreenComponent(
                subscriptions = uiState.subscriptions,
                clearedKeys = clearedKeys,
                onClearOne = { key ->
                    clearedKeys = clearedKeys + key
                    saveClearedTrackerNotificationKeys(context, clearedKeys)
                },
                onClearAll = { visibleKeys ->
                    clearedKeys = clearedKeys + visibleKeys
                    saveClearedTrackerNotificationKeys(context, clearedKeys)
                }
            )
        }
    }
}

@Composable
fun NotificationScreenComponent(
    subscriptions: List<Subscription>,
    clearedKeys: Set<String>,
    onClearOne: (String) -> Unit,
    onClearAll: (Set<String>) -> Unit
){
    val reminders = buildTrackerReminders(subscriptions, clearedKeys)

    if (reminders.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Notifications",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Clear all",
                    color = appColors().green,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        onClearAll(reminders.map { it.notificationKey }.toSet())
                    }
                )
            }
        }
        items(reminders, key = { it.id }) { reminder ->
            ReminderCard(
                reminder = reminder,
                onClear = {
                    onClearOne(reminder.notificationKey)
                }
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ReminderCard(reminder: TrackerReminderUi, onClear: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (reminder.isDueToday) "Due today" else "Upcoming reminder",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (reminder.isDueToday) Color(0xFFBC4749) else appColors().green
                )
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear notification",
                        tint = Color.Gray
                    )
                }
            }
            Text(
                text = reminder.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors().text
            )
            Text(
                text = reminder.subtitle,
                fontSize = 13.sp,
                color = Color.Gray
            )
            Text(
                text = "₹ ${reminder.amount} • ${reminder.dueDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = appColors().text
            )
        }
    }
}

fun calculateTrackerReminderCount(subscriptions: List<Subscription>, clearedKeys: Set<String> = emptySet()): Int {
    return buildTrackerReminders(subscriptions, clearedKeys).size
}

fun buildTrackerReminders(
    subscriptions: List<Subscription>,
    clearedKeys: Set<String> = emptySet()
): List<TrackerReminderUi> {
    return subscriptions.mapNotNull(::buildReminder)
        .filter {
            val days = ChronoUnit.DAYS.between(LocalDate.now(), it.dueDate)
            days in 0..7 && it.notificationKey !in clearedKeys
        }
        .sortedBy { it.dueDate }
}

/** A subscription/EMI resolved to its next due date, for the Home "Upcoming Payments" card. */
data class UpcomingPaymentUi(
    val id: Int,
    val name: String,
    val subType: String,
    val amount: Double,
    val dueDate: LocalDate,
    val logoUrl: String
) {
    val daysUntilDue: Long get() = ChronoUnit.DAYS.between(LocalDate.now(), dueDate)
}

/**
 * The next [limit] payments coming due, soonest first. Recurring trackers roll forward
 * to their next occurrence rather than showing the date they were first created.
 */
fun buildUpcomingPayments(
    subscriptions: List<Subscription>,
    limit: Int = 3
): List<UpcomingPaymentUi> {
    return subscriptions.mapNotNull { subscription ->
        val baseDate = runCatching { LocalDate.parse(subscription.dueDate) }.getOrNull()
            ?: return@mapNotNull null
        UpcomingPaymentUi(
            id = subscription.id,
            name = subscription.name,
            subType = subscription.subType,
            amount = subscription.amount,
            dueDate = calculateNextDueDate(baseDate, subscription.subType),
            logoUrl = subscription.logoUrl
        )
    }
        .filter { it.daysUntilDue >= 0 }
        .sortedBy { it.dueDate }
        .take(limit)
}

/** How many of [subscriptions] fall due within the next [withinDays] days. */
fun countPaymentsDueSoon(subscriptions: List<Subscription>, withinDays: Long = 7): Int =
    buildUpcomingPayments(subscriptions, limit = Int.MAX_VALUE)
        .count { it.daysUntilDue <= withinDays }

private fun buildReminder(subscription: Subscription): TrackerReminderUi? {
    val baseDate = runCatching { LocalDate.parse(subscription.dueDate) }.getOrNull() ?: return null
    val nextDueDate = calculateNextDueDate(baseDate, subscription.subType)

    return TrackerReminderUi(
        id = subscription.id,
        title = "${subscription.name} ${subscription.type.uppercase()} reminder",
        subtitle = when (subscription.subType.lowercase()) {
            "monthly" -> "Monthly payment reminder"
            "yearly" -> "Yearly payment reminder"
            else -> "${subscription.subType.replaceFirstChar { it.uppercase() }} payment reminder"
        },
        amount = subscription.amount,
        dueDate = nextDueDate,
        isDueToday = nextDueDate == LocalDate.now(),
        notificationKey = "${subscription.id}|$nextDueDate"
    )
}

private fun calculateNextDueDate(baseDate: LocalDate, subType: String): LocalDate {
    val today = LocalDate.now()
    return when (subType.lowercase()) {
        "monthly" -> {
            var candidate = withClampedDay(YearMonth.from(today), baseDate.dayOfMonth)
            if (candidate.isBefore(today)) {
                candidate = withClampedDay(YearMonth.from(today).plusMonths(1), baseDate.dayOfMonth)
            }
            candidate
        }
        "yearly" -> {
            var candidate = LocalDate.of(today.year, baseDate.month, 1)
                .withDayOfMonth(minOf(baseDate.dayOfMonth, YearMonth.of(today.year, baseDate.month).lengthOfMonth()))
            if (candidate.isBefore(today)) {
                val nextYear = today.year + 1
                candidate = LocalDate.of(nextYear, baseDate.month, 1)
                    .withDayOfMonth(minOf(baseDate.dayOfMonth, YearMonth.of(nextYear, baseDate.month).lengthOfMonth()))
            }
            candidate
        }
        else -> baseDate
    }
}

private fun withClampedDay(yearMonth: YearMonth, targetDay: Int): LocalDate {
    return yearMonth.atDay(minOf(targetDay, yearMonth.lengthOfMonth()))
}

fun getClearedTrackerNotificationKeys(context: Context): Set<String> {
    return preferencesManager(context).snapshotBlocking().clearedTrackerNotifications
}

private fun saveClearedTrackerNotificationKeys(context: Context, keys: Set<String>) {
    preferencesManager(context).setClearedTrackerNotificationsAsync(keys)
}
