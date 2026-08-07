package com.piggylabs.piggyflow.features.budget.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.designsystem.theme.AppColors
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.piggylabs.piggyflow.core.domain.model.BudgetGoal
import com.piggylabs.piggyflow.core.domain.model.BudgetPriority
import com.piggylabs.piggyflow.core.navigation.BottomBar
import com.piggylabs.piggyflow.core.navigation.Stats
import com.piggylabs.piggyflow.features.budget.presentation.components.BudgetGoalSheet
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private val MonthLabel: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

/** Whole-rupee display. Budget figures are round numbers, so decimals only add noise. */
private fun Double.asBudgetAmount(): String = "₹%,.0f".format(this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetGoalsScreen(
    navController: NavHostController,
    viewModel: BudgetGoalsViewModel
) {
    val colors = appColors()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddSheet by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<BudgetGoal?>(null) }

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
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.onAccentSoft,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Budget Goals",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showAddSheet = true }
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Add Goal",
                            color = colors.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Set monthly category budgets and stay on track.",
                    fontSize = 13.sp,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. MONTH OVERVIEW
            item(key = "overview") {
                MonthOverviewCard(
                    uiState = uiState,
                    onSelectMonth = viewModel::selectMonth
                )
            }

            // 2. STATUS SUMMARY TILES
            item(key = "status_tiles") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryTile(
                        icon = Icons.Default.TrackChanges,
                        iconTint = colors.positive,
                        iconBackground = colors.positiveSoft,
                        title = "On Track",
                        count = uiState.onTrackCount,
                        caption = "Within budget",
                        captionColor = colors.positive,
                        selected = uiState.statusFilter == BudgetStatusFilter.ON_TRACK,
                        onClick = { viewModel.toggleStatusFilter(BudgetStatusFilter.ON_TRACK) },
                        modifier = Modifier.weight(1f)
                    )
                    SummaryTile(
                        icon = Icons.Default.ErrorOutline,
                        iconTint = colors.warning,
                        iconBackground = colors.warningSoft,
                        title = "At Risk",
                        count = uiState.atRiskCount,
                        caption = "Above 80%",
                        captionColor = colors.warning,
                        selected = uiState.statusFilter == BudgetStatusFilter.AT_RISK,
                        onClick = { viewModel.toggleStatusFilter(BudgetStatusFilter.AT_RISK) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. LIST HEADER: SORT + PRIORITY FILTER
            item(key = "list_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Budget Goals",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SortSelector(
                            current = uiState.sort,
                            onSelect = viewModel::setSort
                        )
                        PriorityFilterButton(
                            current = uiState.priorityFilter,
                            onSelect = viewModel::setPriorityFilter
                        )
                    }
                }
            }

            // 4. GOALS
            item(key = "goals") {
                when {
                    uiState.goals.isEmpty() && !uiState.isLoading -> EmptyGoalsCard(
                        onAddGoal = { showAddSheet = true }
                    )

                    uiState.visibleGoals.isEmpty() && !uiState.isLoading -> NoMatchesCard()

                    else -> Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column {
                            uiState.visibleGoals.forEachIndexed { index, item ->
                                if (index > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(colors.textMuted.copy(alpha = 0.12f))
                                    )
                                }
                                BudgetGoalRow(
                                    item = item,
                                    onClick = { editingGoal = item.goal }
                                )
                            }
                        }
                    }
                }
            }

            // 5. INSIGHTS BANNER
            item(key = "insights") {
                InsightsBanner(onViewInsights = { navController.navigate(Stats.route) })
            }

            item(key = "footer_space") { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showAddSheet) {
        BudgetGoalSheet(
            goal = null,
            categories = uiState.categories,
            isDuplicate = { name -> viewModel.hasGoalFor(name) },
            onDismiss = { showAddSheet = false },
            onSave = { name, emoji, priority, limit ->
                viewModel.addGoal(name, emoji, priority, limit)
                showAddSheet = false
            },
            onDelete = null
        )
    }

    editingGoal?.let { goal ->
        BudgetGoalSheet(
            goal = goal,
            categories = uiState.categories,
            isDuplicate = { name -> viewModel.hasGoalFor(name, excludingId = goal.id) },
            onDismiss = { editingGoal = null },
            onSave = { name, emoji, priority, limit ->
                viewModel.updateGoal(
                    goal.copy(
                        categoryName = name,
                        categoryEmoji = emoji,
                        priority = priority,
                        monthlyLimit = limit
                    )
                )
                editingGoal = null
            },
            onDelete = {
                viewModel.deleteGoal(goal.id)
                editingGoal = null
            }
        )
    }
}

@Composable
private fun MonthOverviewCard(
    uiState: BudgetGoalsUiState,
    onSelectMonth: (YearMonth) -> Unit
) {
    val colors = appColors()
    var monthMenuOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceMuted),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { monthMenuOpen = true }
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "${uiState.month.format(MonthLabel)} Overview",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Change month",
                        tint = colors.text,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = monthMenuOpen,
                    onDismissRequest = { monthMenuOpen = false },
                    containerColor = colors.surface
                ) {
                    uiState.selectableMonths.forEach { month ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = month.format(MonthLabel),
                                    color = if (month == uiState.month) colors.accent else colors.text,
                                    fontSize = 14.sp,
                                    fontWeight = if (month == uiState.month) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            },
                            onClick = {
                                onSelectMonth(month)
                                monthMenuOpen = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OverviewStat(
                    label = "Total Budget",
                    value = uiState.totalBudget.asBudgetAmount(),
                    valueColor = colors.text,
                    caption = uiState.budgetPercentOfIncome
                        ?.let { "$it% of income" }
                        ?: "No income logged",
                    modifier = Modifier.weight(1f)
                )
                OverviewDivider()
                OverviewStat(
                    label = "Total Spent",
                    value = uiState.totalSpent.asBudgetAmount(),
                    valueColor = colors.text,
                    caption = "${uiState.spentPercent}% of budget",
                    modifier = Modifier.weight(1f)
                )
                OverviewDivider()
                OverviewStat(
                    label = "Remaining",
                    value = uiState.totalRemaining.asBudgetAmount(),
                    valueColor = if (uiState.totalRemaining < 0) colors.negative else colors.positive,
                    caption = if (uiState.totalRemaining < 0) {
                        "Over budget"
                    } else {
                        "${uiState.remainingPercent}% left"
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(14.dp))

            ProgressTrack(
                fraction = uiState.spentPercent / 100f,
                color = if (uiState.totalRemaining < 0) colors.negative else colors.positive,
                height = 8.dp
            )
        }
    }
}

@Composable
private fun OverviewStat(
    label: String,
    value: String,
    valueColor: Color,
    caption: String,
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    Column(modifier = modifier) {
        Text(text = label, fontSize = 12.sp, color = colors.textMuted, maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = caption,
            fontSize = 11.sp,
            color = colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OverviewDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .width(1.dp)
            .height(52.dp)
            .background(appColors().textMuted.copy(alpha = 0.2f))
    )
}

@Composable
private fun SummaryTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    count: Int,
    caption: String,
    captionColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = appColors()

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    // The active filter keeps a ring so it is clear the list is narrowed.
                    if (selected) {
                        Modifier.border(1.5.dp, iconTint, RoundedCornerShape(16.dp))
                    } else {
                        Modifier
                    }
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 12.sp, color = colors.textMuted, maxLines = 1)
                Text(
                    text = "$count",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Text(
                    text = caption,
                    fontSize = 11.sp,
                    color = captionColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SortSelector(
    current: BudgetSort,
    onSelect: (BudgetSort) -> Unit
) {
    val colors = appColors()
    var open by remember { mutableStateOf(false) }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(
                    1.dp,
                    colors.textMuted.copy(alpha = 0.25f),
                    RoundedCornerShape(12.dp)
                )
                .clickable { open = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Sort: ${current.label}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.text
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Change sort",
                tint = colors.textMuted,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            containerColor = colors.surface
        ) {
            BudgetSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.label,
                            fontSize = 14.sp,
                            color = if (option == current) colors.accent else colors.text,
                            fontWeight = if (option == current) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    },
                    onClick = {
                        onSelect(option)
                        open = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PriorityFilterButton(
    current: String?,
    onSelect: (String?) -> Unit
) {
    val colors = appColors()
    var open by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    1.dp,
                    if (current == null) {
                        colors.textMuted.copy(alpha = 0.25f)
                    } else {
                        colors.accent
                    },
                    RoundedCornerShape(12.dp)
                )
                .clickable { open = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter goals",
                tint = if (current == null) colors.text else colors.accent,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false },
            containerColor = colors.surface
        ) {
            val options = listOf<String?>(null) + BudgetPriority.all
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option ?: "All priorities",
                            fontSize = 14.sp,
                            color = if (option == current) colors.accent else colors.text,
                            fontWeight = if (option == current) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    },
                    onClick = {
                        onSelect(option)
                        open = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BudgetGoalRow(
    item: BudgetGoalProgress,
    onClick: () -> Unit
) {
    val colors = appColors()
    val accent = statusColor(item.status, colors)
    val tint = categoryTint(item.goal.categoryName, colors)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (item.goal.categoryEmoji.isNotBlank()) {
                Text(text = item.goal.categoryEmoji, fontSize = 20.sp)
            } else {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.goal.categoryName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.goal.priority,
                fontSize = 11.sp,
                color = colors.textMuted,
                maxLines = 1
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressTrack(
                    fraction = item.percentUsed / 100f,
                    color = accent,
                    height = 5.dp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${item.percentUsed}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${item.spent.asBudgetAmount()} of ${item.goal.monthlyLimit.asBudgetAmount()}",
                fontSize = 11.sp,
                color = colors.textMuted,
                maxLines = 1
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(96.dp)
        ) {
            Text(
                text = abs(item.remaining).asBudgetAmount(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (item.remaining < 0) "Over by" else "Remaining",
                fontSize = 10.sp,
                color = colors.textMuted,
                maxLines = 1
            )
            Spacer(Modifier.height(6.dp))
            StatusChip(status = item.status)
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun StatusChip(status: BudgetStatus) {
    val colors = appColors()
    val fg = statusColor(status, colors)
    val bg = statusBackground(status, colors)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = statusLabel(status),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            maxLines = 1
        )
    }
}

/** Rounded track with a clamped fill, so an over-budget goal still renders a full bar. */
@Composable
private fun ProgressTrack(
    fraction: Float,
    color: Color,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(colors.textMuted.copy(alpha = 0.18f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(color)
        )
    }
}

@Composable
private fun EmptyGoalsCard(onAddGoal: () -> Unit) {
    val colors = appColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = colors.onAccentSoft,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No budget goals yet",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Set a monthly limit for a category and PiggyFlow will track your spending against it.",
                fontSize = 12.sp,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.accent)
                    .clickable { onAddGoal() }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Create your first goal",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NoMatchesCard() {
    val colors = appColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Text(
            text = "No goals match the current filters.",
            fontSize = 13.sp,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}

@Composable
private fun InsightsBanner(onViewInsights: () -> Unit) {
    val colors = appColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.accentSoft),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = colors.onAccentSoft,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Stay in control of your spending",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Review your budgets regularly and adjust to reach your financial goals.",
                    fontSize = 11.sp,
                    color = colors.textMuted,
                    lineHeight = 15.sp
                )
            }

            Spacer(Modifier.width(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface)
                    .clickable { onViewInsights() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "View Insights",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent,
                    maxLines = 1
                )
            }
        }
    }
}

private fun statusLabel(status: BudgetStatus): String = when (status) {
    BudgetStatus.ON_TRACK -> "On Track"
    BudgetStatus.AT_RISK -> "At Risk"
    BudgetStatus.OVER_BUDGET -> "Over Budget"
    BudgetStatus.COMPLETED -> "Completed"
}

private fun statusColor(status: BudgetStatus, colors: AppColors): Color = when (status) {
    BudgetStatus.ON_TRACK -> colors.positive
    BudgetStatus.AT_RISK -> colors.warning
    BudgetStatus.OVER_BUDGET -> colors.negative
    BudgetStatus.COMPLETED -> colors.accent
}

private fun statusBackground(status: BudgetStatus, colors: AppColors): Color = when (status) {
    BudgetStatus.ON_TRACK -> colors.positiveSoft
    BudgetStatus.AT_RISK -> colors.warningSoft
    BudgetStatus.OVER_BUDGET -> colors.negativeSoft
    BudgetStatus.COMPLETED -> colors.accentSoft
}

/** Stable per-category colour, so a category keeps the same tint across sessions. */
private fun categoryTint(categoryName: String, colors: AppColors): Color {
    val palette = colors.chartPalette
    if (palette.isEmpty()) return colors.accent
    val index = abs(categoryName.lowercase().hashCode()) % palette.size
    return palette[index]
}
