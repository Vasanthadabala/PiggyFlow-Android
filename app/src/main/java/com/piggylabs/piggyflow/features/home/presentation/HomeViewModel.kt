package com.piggylabs.piggyflow.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.piggyflow.core.database.AppEvents
import com.piggylabs.piggyflow.core.domain.model.Category
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.Income
import com.piggylabs.piggyflow.core.domain.usecase.AddCategoryUseCase
import com.piggylabs.piggyflow.core.domain.usecase.AddExpenseUseCase
import com.piggylabs.piggyflow.core.domain.usecase.AddIncomeUseCase
import com.piggylabs.piggyflow.core.domain.usecase.DeleteCategoryUseCase
import com.piggylabs.piggyflow.core.domain.usecase.DeleteExpenseUseCase
import com.piggylabs.piggyflow.core.domain.usecase.DeleteIncomeUseCase
import com.piggylabs.piggyflow.core.domain.usecase.ObserveCategoriesUseCase
import com.piggylabs.piggyflow.core.domain.usecase.ObserveExpenseByIdUseCase
import com.piggylabs.piggyflow.core.domain.usecase.ObserveExpensesUseCase
import com.piggylabs.piggyflow.core.domain.usecase.ObserveIncomeByIdUseCase
import com.piggylabs.piggyflow.core.domain.usecase.ObserveIncomeUseCase
import com.piggylabs.piggyflow.core.domain.usecase.UpdateExpenseUseCase
import com.piggylabs.piggyflow.core.domain.usecase.UpdateIncomeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Shared ViewModel for the personal ledger (home, transactions, add-data, stats, profile).
 * Holds no data access of its own - every read and write goes through a use case.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    observeCategories: ObserveCategoriesUseCase,
    observeExpenses: ObserveExpensesUseCase,
    observeIncome: ObserveIncomeUseCase,
    private val observeExpenseByIdUseCase: ObserveExpenseByIdUseCase,
    private val observeIncomeByIdUseCase: ObserveIncomeByIdUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val addIncomeUseCase: AddIncomeUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val updateIncomeUseCase: UpdateIncomeUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val deleteIncomeUseCase: DeleteIncomeUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    /**
     * Bumped when a Drive restore swaps the Room instance, which re-subscribes the streams
     * below against the new database.
     */
    private val restarts = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = restarts
        .flatMapLatest {
            combine(
                observeCategories(),
                observeExpenses(),
                observeIncome()
            ) { categories, expenses, income ->
                HomeUiState(
                    categories = categories,
                    expenses = expenses,
                    income = income,
                    isLoading = false
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = HomeUiState()
        )

    init {
        viewModelScope.launch {
            AppEvents.dbRecreated.collect { restarts.update { it + 1 } }
        }
    }

    fun observeExpenseById(id: Int): Flow<Expense?> = observeExpenseByIdUseCase(id)

    fun observeIncomeById(id: Int): Flow<Income?> = observeIncomeByIdUseCase(id)

    fun addExpense(
        categoryType: String,
        amount: Double,
        note: String,
        date: String,
        categoryName: String,
        categoryEmoji: String
    ) = viewModelScope.launch {
        addExpenseUseCase(
            Expense(
                categoryType = categoryType,
                amount = amount,
                note = note,
                date = date,
                categoryName = categoryName,
                categoryEmoji = categoryEmoji
            )
        )
    }

    fun addIncome(
        categoryType: String,
        categoryName: String,
        categoryEmoji: String,
        amount: Double,
        note: String,
        date: String
    ) = viewModelScope.launch {
        addIncomeUseCase(
            Income(
                categoryType = categoryType,
                amount = amount,
                note = note,
                date = date,
                categoryName = categoryName,
                categoryEmoji = categoryEmoji
            )
        )
    }

    /** Saves an already-built [Expense], for screens that fill in more than the six core fields. */
    fun addExpense(expense: Expense) = viewModelScope.launch {
        addExpenseUseCase(expense)
    }

    /** Saves an already-built [Income], for screens that fill in more than the six core fields. */
    fun addIncome(income: Income) = viewModelScope.launch {
        addIncomeUseCase(income)
    }

    fun addCategory(name: String, emoji: String) = viewModelScope.launch {
        addCategoryUseCase(Category(name = name, emoji = emoji))
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        updateExpenseUseCase(expense)
    }

    fun updateIncome(income: Income) = viewModelScope.launch {
        updateIncomeUseCase(income)
    }

    fun deleteExpenseById(id: Int) = viewModelScope.launch {
        deleteExpenseUseCase(id)
    }

    fun deleteIncomeById(id: Int) = viewModelScope.launch {
        deleteIncomeUseCase(id)
    }

    fun deleteCategoryById(id: Int) = viewModelScope.launch {
        deleteCategoryUseCase(id)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
