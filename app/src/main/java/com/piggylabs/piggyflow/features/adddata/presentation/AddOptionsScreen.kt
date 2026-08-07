package com.piggylabs.piggyflow.features.adddata.presentation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.navigation.AddExpense
import com.piggylabs.piggyflow.core.navigation.AddIncome
import com.piggylabs.piggyflow.core.navigation.InfoScreen
import com.piggylabs.piggyflow.core.navigation.Tracker
import com.piggylabs.piggyflow.core.navigation.BottomBar
import com.piggylabs.piggyflow.features.profile.presentation.InfoTopic
import com.piggylabs.piggyflow.core.designsystem.theme.appColors

@ExperimentalMaterial3Api
@Composable
fun AddOptionsScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            AddOptionsScreenComponent(navController = navController)
        }
    }
}

private data class AddOptionEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: (NavHostController) -> Unit
)

@ExperimentalMaterial3Api
@Composable
fun AddOptionsScreenComponent(navController: NavHostController) {
    val colors = appColors()

    val options = listOf(
        AddOptionEntry("Add Expense", "Add a new expense manually", Icons.Default.TrendingDown) {
            it.navigate(AddExpense.route) { launchSingleTop = true }
        },
        AddOptionEntry("Add Income", "Add a new income source", Icons.Default.TrendingUp) {
            it.navigate(AddIncome.route) { launchSingleTop = true }
        },
        AddOptionEntry("Add Transfer", "Transfer between accounts", Icons.Default.SwapHoriz) {
            it.navigate(InfoScreen.forTopic(InfoTopic.TRANSFER)) { launchSingleTop = true }
        },
        AddOptionEntry("Add Goal", "Create a new savings goal", Icons.Default.Flag) {
            it.navigate(InfoScreen.forTopic(InfoTopic.GOAL)) { launchSingleTop = true }
        },
        AddOptionEntry("Add Subscription", "Add a subscription or recurring bill", Icons.Default.Event) {
            it.navigate(Tracker.route) { launchSingleTop = true }
        },
        AddOptionEntry("Add EMI", "Add a new EMI or loan payment", Icons.Default.CreditCard) {
            it.navigate(Tracker.route) { launchSingleTop = true }
        },
        AddOptionEntry("Add Purchase", "Add a purchase or shopping item", Icons.Default.ShoppingBag) {
            it.navigate(InfoScreen.forTopic(InfoTopic.PURCHASE)) { launchSingleTop = true }
        },
        AddOptionEntry("Upload Bill", "Add an expense from a bill or receipt", Icons.Default.ReceiptLong) {
            it.navigate(AddExpense.route) { launchSingleTop = true }
        }
    )

    val quickActions = listOf(
        AddOptionEntry("Add Note", "", Icons.Default.NoteAlt) {
            it.navigate(InfoScreen.forTopic(InfoTopic.NOTE)) { launchSingleTop = true }
        },
        AddOptionEntry("Add Vehicle", "", Icons.Default.DirectionsCar) {
            it.navigate(InfoScreen.forTopic(InfoTopic.VEHICLE)) { launchSingleTop = true }
        },
        AddOptionEntry("Split Expense", "", Icons.Default.Group) {
            it.navigate(InfoScreen.forTopic(InfoTopic.SPLIT_EXPENSE)) { launchSingleTop = true }
        },
        AddOptionEntry("Add Reminder", "", Icons.Default.Bookmark) {
            it.navigate(Tracker.route) { launchSingleTop = true }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.textMuted.copy(alpha = 0.3f))
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Add New", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.text)
            Text(text = "Choose what you want to add", fontSize = 13.sp, color = colors.textMuted)
        }

        item {
            val rows = options.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { option ->
                            AddOptionTile(option = option, navController = navController, modifier = Modifier.weight(1f))
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.accentSoft)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = colors.onAccentSoft, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Scan Bill with AI", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.onAccentSoft)
                        Text(
                            text = "Quick entry today — automatic AI extraction is on the roadmap",
                            fontSize = 11.sp,
                            color = colors.onAccentSoft
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surface)
                            .clickable {
                                navController.navigate(AddExpense.route) { launchSingleTop = true }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = colors.accent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Scan Now", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent)
                    }
                }
            }
        }

        item {
            Text(text = "Quick Actions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.text)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                quickActions.forEach { action ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(72.dp)
                            .clickable { action.onClick(navController) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = action.icon, contentDescription = null, tint = colors.text, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = action.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.text,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun AddOptionTile(option: AddOptionEntry, navController: NavHostController, modifier: Modifier = Modifier) {
    val colors = appColors()
    Card(
        modifier = modifier.clickable { option.onClick(navController) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = option.icon, contentDescription = null, tint = colors.onAccentSoft, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = option.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.text, maxLines = 1)
                if (option.subtitle.isNotBlank()) {
                    Text(text = option.subtitle, fontSize = 10.sp, color = colors.textMuted, maxLines = 2)
                }
            }
        }
    }
}
