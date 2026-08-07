package com.piggylabs.piggyflow.features.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.navigation.TopBar
import com.piggylabs.piggyflow.core.designsystem.theme.ThemeMode
import com.piggylabs.piggyflow.core.designsystem.theme.ThemePreference
import com.piggylabs.piggyflow.core.designsystem.theme.appColors

/** Topics the generic [InfoScreen] can render — either an honest "coming soon" page
 * for a feature that doesn't exist yet, or a static single-value info page for a
 * setting the app doesn't yet make configurable. */
object InfoTopic {
    const val SECURITY = "security"
    const val PAYMENT_METHODS = "payment_methods"
    const val PREMIUM = "premium"
    const val CURRENCY = "currency"
    const val LANGUAGE = "language"
    const val HELP_CENTER = "help_center"

    // Add New sheet rows with no feature behind them yet.
    const val TRANSFER = "transfer"
    const val GOAL = "goal"
    const val PURCHASE = "purchase"
    const val NOTE = "note"
    const val VEHICLE = "vehicle"
    const val SPLIT_EXPENSE = "split_expense"
}

@ExperimentalMaterial3Api
@Composable
fun InfoScreen(navController: NavHostController, topic: String) {
    Scaffold(topBar = { TopBar(name = infoTitleFor(topic), navController = navController) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(padding)
        ) {
            when (topic) {
                InfoTopic.SECURITY -> ComingSoonContent(
                    title = "Biometric Security",
                    description = "Fingerprint and face unlock for PiggyFlow are on the roadmap. For now, your device's own lock screen keeps the app protected."
                )
                InfoTopic.PAYMENT_METHODS -> ComingSoonContent(
                    title = "Payment Methods",
                    description = "Saving cards or UPI IDs here for faster expense entry is coming in a future update."
                )
                InfoTopic.PREMIUM -> ComingSoonContent(
                    title = "Premium Membership",
                    description = "PiggyFlow doesn't have a paid tier yet — every feature you see today is free to use."
                )
                InfoTopic.CURRENCY -> StaticInfoContent(
                    value = "INR (₹)",
                    description = "PiggyFlow currently supports the Indian Rupee only. Multi-currency support may be added in a future update."
                )
                InfoTopic.LANGUAGE -> StaticInfoContent(
                    value = "English",
                    description = "PiggyFlow is available in English only for now. More languages may be added later."
                )
                InfoTopic.HELP_CENTER -> HelpCenterContent()
                InfoTopic.TRANSFER -> ComingSoonContent(
                    title = "Add Transfer",
                    description = "Moving money between your own accounts and recording it as a transfer (not income or expense) is coming in a future update."
                )
                InfoTopic.GOAL -> ComingSoonContent(
                    title = "Add Goal",
                    description = "Dedicated savings goals with their own progress tracking are coming in a future update."
                )
                InfoTopic.PURCHASE -> ComingSoonContent(
                    title = "Add Purchase",
                    description = "A dedicated purchase/shopping-item entry, separate from a plain expense, is coming in a future update."
                )
                InfoTopic.NOTE -> ComingSoonContent(
                    title = "Add Note",
                    description = "Standalone notes not tied to a transaction are coming in a future update."
                )
                InfoTopic.VEHICLE -> ComingSoonContent(
                    title = "Add Vehicle",
                    description = "Vehicle-specific expense tracking (fuel, service, insurance) is coming in a future update."
                )
                InfoTopic.SPLIT_EXPENSE -> ComingSoonContent(
                    title = "Split Expense",
                    description = "Splitting a single expense across multiple accounts or people is coming in a future update."
                )
                else -> ComingSoonContent(title = "Coming soon", description = "This section isn't available yet.")
            }
        }
    }
}

private fun infoTitleFor(topic: String): String = when (topic) {
    InfoTopic.SECURITY -> "Security"
    InfoTopic.PAYMENT_METHODS -> "Payment Methods"
    InfoTopic.PREMIUM -> "Premium Membership"
    InfoTopic.CURRENCY -> "Currency"
    InfoTopic.LANGUAGE -> "Language"
    InfoTopic.HELP_CENTER -> "Help Center"
    InfoTopic.TRANSFER -> "Add Transfer"
    InfoTopic.GOAL -> "Add Goal"
    InfoTopic.PURCHASE -> "Add Purchase"
    InfoTopic.NOTE -> "Add Note"
    InfoTopic.VEHICLE -> "Add Vehicle"
    InfoTopic.SPLIT_EXPENSE -> "Split Expense"
    else -> "Back"
}

@Composable
private fun ComingSoonContent(title: String, description: String) {
    val colors = appColors()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Construction, contentDescription = null, tint = colors.onAccentSoft, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.text)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            fontSize = 14.sp,
            color = colors.textMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Coming soon", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent)
    }
}

@Composable
private fun StaticInfoContent(value: String, description: String) {
    val colors = appColors()
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.text)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = description, fontSize = 14.sp, color = colors.textMuted, lineHeight = 20.sp)
            }
        }
    }
}

private data class FaqEntry(val question: String, val answer: String)

private val faqEntries = listOf(
    FaqEntry(
        "What does Scan Bill do?",
        "It's a shortcut from Add New straight into the expense form so you can enter a bill's details quickly. Automatic AI extraction from a photo is on the roadmap, not built yet."
    ),
    FaqEntry(
        "How do Tracker reminders work?",
        "Add a subscription or EMI in the Tracker tab with its due date. It shows up under Due Soon / Due This Month, and a reminder badge appears on the Home bell icon as the date approaches."
    ),
    FaqEntry(
        "What does Cloud Sync do?",
        "Signing in with Google backs up your categories, expenses, income, and tracker items to the cloud so you can restore them on a new device. It's optional — the app works fully offline without it."
    ),
    FaqEntry(
        "Can I switch between Personal and Business mode?",
        "Yes, from Profile → Switch to Business/Personal. Each mode keeps its own local data and its own sign-in."
    ),
    FaqEntry(
        "Where is my data stored?",
        "Everything is stored locally on your device by default. It's only sent anywhere if you explicitly sign in and sync."
    )
)

@Composable
private fun HelpCenterContent() {
    val colors = appColors()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            Text(text = "Frequently asked questions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.text)
        }
        items(faqEntries.size) { index ->
            val entry = faqEntries[index]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = entry.question, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = entry.answer, fontSize = 13.sp, color = colors.textMuted, lineHeight = 18.sp)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@ExperimentalMaterial3Api
@Composable
fun AppearanceScreen(navController: NavHostController) {
    val context = LocalContext.current
    val colors = appColors()
    var selected by remember { mutableStateOf(ThemePreference.mode) }

    Scaffold(topBar = { TopBar(name = "Appearance", navController = navController) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Choose how PiggyFlow looks",
                fontSize = 14.sp,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(16.dp))

            listOf(
                ThemeMode.SYSTEM to "System default",
                ThemeMode.LIGHT to "Light",
                ThemeMode.DARK to "Dark"
            ).forEach { (mode, label) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable {
                            selected = mode
                            ThemePreference.set(context, mode)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.text)
                        RadioButton(
                            selected = selected == mode,
                            onClick = {
                                selected = mode
                                ThemePreference.set(context, mode)
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = colors.accent)
                        )
                    }
                }
            }
        }
    }
}
