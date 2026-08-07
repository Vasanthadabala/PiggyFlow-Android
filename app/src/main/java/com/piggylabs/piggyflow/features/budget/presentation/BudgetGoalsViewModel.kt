package com.piggylabs.piggyflow.features.budget.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.piggyflow.core.common.UiState
import com.piggylabs.piggyflow.core.common.toLocalDateOrNull
import com.piggylabs.piggyflow.core.database.AppEvents
import com.piggylabs.piggyflow.core.domain.model.BudgetGoal
import com.piggylabs.piggyflow.core.domain.model.Category
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.Income
import com.piggylabs.piggyflow.core.domain.usecase.ObserveCategoriesUseCase
import com.piggylabs.piggyflow.core.domain.usecase.ObserveExpensesUseCase
import com.piggylabs.piggyflow.core.domain.usecase.ObserveIncomeUseCase
import com.piggylabs.piggyflow.features.budget.domain.usecase.AddBudgetGoalUseCase
import com.piggylabs.piggyflow.features.budget.domain.usecase.DeleteBudgetGoalUseCase
import com.piggylabs.piggyflow.features.budget.domain.usecase.ObserveBudgetGoalsUseCase
import com.piggylabs.piggyflow.features.budget.domain.usecase.UpdateBudgetGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.roundToInt

/** Where a goal sits against its limit for the month being viewed. */
enum class BudgetStatus { ON_TRACK, AT_RISK, OVER_BUDGET, COMPLETED }

/** A goal paired with the spending measured against it for one month. */
data class BudgetGoalProgress(
    val goal: BudgetGoal,
    val spent: Double,
    /** Limit minus spent. Negative once the goal is over budget. */
    val remaining: Double,
    val percentUsed: Int,
    val status: BudgetStatus
)

/** How the goal list is ordered. */
enum class BudgetSort(val label: String) {
    STATUS("Status"),
    NAME("Name"),
    AMOUNT("Amount"),
    PROGRESS("Progress")
}

/** Which slice of the list the summary tiles have narrowed it to. */
enum class BudgetStatusFilter { ALL, ON_TRACK, AT_RISK }

data class BudgetGoalsUiState(
    val month: YearMonth = YearMonth.now(),
    /** Months the user can switch to, newest first. */
    val selectableMonths: List<YearMonth> = emptyList(),
    val goals: List<BudgetGoalProgress> = emptyList(),
    /** [goals] after the status and priority filters, in the chosen sort order. */
    val visibleGoals: List<BudgetGoalProgress> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val onTrackCount: Int = 0,
    val atRiskCount: Int = 0,
    val sort: BudgetSort = BudgetSort.STATUS,
    val statusFilter: BudgetStatusFilter = BudgetStatusFilter.ALL,
    /** Null means every priority is shown. */
    val priorityFilter: String? = null,
    /** Categories offered by the add/edit sheet. */
    val categories: List<Category> = emptyList(),
    override val isLoading: Boolean = true,
    override val errorMessage: String? = null
) : UiState {

    val totalRemaining: Double get() = totalBudget - totalSpent

    /** Share of the month's budget already spent, capped at 100 for the progress bar. */
    val spentPercent: Int
        get() = if (totalBudget <= 0.0) 0 else {
            ((totalSpent / totalBudget) * 100).roundToInt().coerceIn(0, 100)
        }

    val remainingPercent: Int get() = 100 - spentPercent

    /** Share of the month's income the budget commits, null when no income is recorded. */
    val budgetPercentOfIncome: Int?
        get() = if (monthlyIncome <= 0.0) null else {
            ((totalBudget / monthlyIncome) * 100).roundToInt()
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetGoalsViewModel @Inject constructor(
    observeBudgetGoals: ObserveBudgetGoalsUseCase,
    observeExpenses: ObserveExpensesUseCase,
    observeIncome: ObserveIncomeUseCase,
    observeCategories: ObserveCategoriesUseCase,
    private val addBudgetGoalUseCase: AddBudgetGoalUseCase,
    private val updateBudgetGoalUseCase: UpdateBudgetGoalUseCase,
    private val deleteBudgetGoalUseCase: DeleteBudgetGoalUseCase
) : ViewModel() {

    private val restarts = MutableStateFlow(0)
    private val month = MutableStateFlow(YearMonth.now())
    private val sort = MutableStateFlow(BudgetSort.STATUS)
    private val statusFilter = MutableStateFlow(BudgetStatusFilter.ALL)
    private val priorityFilter = MutableStateFlow<String?>(null)

    /** Sort and both filters travel together so [combine] stays inside its arity limit. */
    private data class ViewOptions(
        val sort: BudgetSort,
        val statusFilter: BudgetStatusFilter,
        val priorityFilter: String?
    )

    private val viewOptions = combine(sort, statusFilter, priorityFilter, ::ViewOptions)

    private val sources = restarts.flatMapLatest {
        combine(
            observeBudgetGoals(),
            observeExpenses(),
            observeIncome(),
            observeCategories()
        ) { goals, expenses, incomes, categories ->
            Sources(goals, expenses, incomes, categories)
        }
    }

    private data class Sources(
        val goals: List<BudgetGoal>,
        val expenses: List<Expense>,
        val incomes: List<Income>,
        val categories: List<Category>
    )

    val uiState: StateFlow<BudgetGoalsUiState> =
        combine(sources, month, viewOptions) { data, selectedMonth, options ->
            // Category names differ only by case between screens, so match on a normalised key.
            val spentByCategory = data.expenses
                .filter { it.date.toLocalDateOrNull()?.let(YearMonth::from) == selectedMonth }
                .groupBy { it.categoryName.trim().lowercase() }
                .mapValues { (_, list) -> list.sumOf { it.amount } }

            val monthlyIncome = data.incomes
                .filter { it.date.toLocalDateOrNull()?.let(YearMonth::from) == selectedMonth }
                .sumOf { it.amount }

            val progress = data.goals.map { goal ->
                val spent = spentByCategory[goal.categoryName.trim().lowercase()] ?: 0.0
                val percent = if (goal.monthlyLimit <= 0.0) 0 else {
                    ((spent / goal.monthlyLimit) * 100).roundToInt()
                }
                BudgetGoalProgress(
                    goal = goal,
                    spent = spent,
                    remaining = goal.monthlyLimit - spent,
                    percentUsed = percent,
                    status = statusFor(percent)
                )
            }

            val filtered = progress
                .filter { item ->
                    when (options.statusFilter) {
                        BudgetStatusFilter.ALL -> true
                        BudgetStatusFilter.ON_TRACK -> item.percentUsed < AT_RISK_PERCENT
                        BudgetStatusFilter.AT_RISK -> item.percentUsed >= AT_RISK_PERCENT
                    }
                }
                .filter { options.priorityFilter == null || it.goal.priority == options.priorityFilter }

            BudgetGoalsUiState(
                month = selectedMonth,
                selectableMonths = selectableMonths(),
                goals = progress,
                visibleGoals = sorted(filtered, options.sort),
                totalBudget = data.goals.sumOf { it.monthlyLimit },
                totalSpent = progress.sumOf { it.spent },
                monthlyIncome = monthlyIncome,
                // A clean partition, so the two tiles always add up to the goal count.
                onTrackCount = progress.count { it.percentUsed < AT_RISK_PERCENT },
                atRiskCount = progress.count { it.percentUsed >= AT_RISK_PERCENT },
                sort = options.sort,
                statusFilter = options.statusFilter,
                priorityFilter = options.priorityFilter,
                categories = data.categories,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = BudgetGoalsUiState(selectableMonths = selectableMonths())
        )

    init {
        viewModelScope.launch {
            AppEvents.dbRecreated.collect { restarts.update { it + 1 } }
        }
    }

    fun selectMonth(value: YearMonth) = month.update { value }

    fun setSort(value: BudgetSort) = sort.update { value }

    fun setPriorityFilter(value: String?) = priorityFilter.update { value }

    /** Tapping an already-active summary tile clears the filter rather than reapplying it. */
    fun toggleStatusFilter(value: BudgetStatusFilter) = statusFilter.update {
        if (it == value) BudgetStatusFilter.ALL else value
    }

    fun addGoal(
        categoryName: String,
        categoryEmoji: String,
        priority: String,
        monthlyLimit: Double
    ) = viewModelScope.launch {
        addBudgetGoalUseCase(
            BudgetGoal(
                categoryName = categoryName,
                categoryEmoji = categoryEmoji,
                priority = priority,
                monthlyLimit = monthlyLimit
            )
        )
    }

    fun updateGoal(goal: BudgetGoal) = viewModelScope.launch {
        updateBudgetGoalUseCase(goal)
    }

    fun deleteGoal(id: Int) = viewModelScope.launch {
        deleteBudgetGoalUseCase(id)
    }

    /** True when [categoryName] already has a goal, so the sheet can block a duplicate. */
    fun hasGoalFor(categoryName: String, excludingId: Int = 0): Boolean =
        uiState.value.goals.any {
            it.goal.id != excludingId &&
                it.goal.categoryName.trim().equals(categoryName.trim(), ignoreCase = true)
        }

    private fun statusFor(percent: Int): BudgetStatus = when {
        percent > 100 -> BudgetStatus.OVER_BUDGET
        percent == 100 -> BudgetStatus.COMPLETED
        percent >= AT_RISK_PERCENT -> BudgetStatus.AT_RISK
        else -> BudgetStatus.ON_TRACK
    }

    private fun sorted(
        items: List<BudgetGoalProgress>,
        sort: BudgetSort
    ): List<BudgetGoalProgress> = when (sort) {
        // Worst first, so anything needing attention is at the top.
        BudgetSort.STATUS -> items.sortedWith(
            compareBy<BudgetGoalProgress> { severity(it.status) }.thenByDescending { it.percentUsed }
        )
        BudgetSort.NAME -> items.sortedBy { it.goal.categoryName.lowercase() }
        BudgetSort.AMOUNT -> items.sortedByDescending { it.goal.monthlyLimit }
        BudgetSort.PROGRESS -> items.sortedByDescending { it.percentUsed }
    }

    private fun severity(status: BudgetStatus): Int = when (status) {
        BudgetStatus.OVER_BUDGET -> 0
        BudgetStatus.AT_RISK -> 1
        BudgetStatus.COMPLETED -> 2
        BudgetStatus.ON_TRACK -> 3
    }

    private fun selectableMonths(): List<YearMonth> {
        val now = YearMonth.now()
        return (0 until MONTH_WINDOW).map { now.minusMonths(it.toLong()) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L

        /** Share of a budget at which a goal stops counting as on track. */
        const val AT_RISK_PERCENT = 80

        /** How many past months the overview header can jump back to. */
        const val MONTH_WINDOW = 12
    }
}
