package com.piggylabs.piggyflow.core.domain.usecase

import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveExpensesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<Expense>> = repository.observeExpenses()
}

class ObserveExpenseByIdUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(id: Int): Flow<Expense?> = repository.observeExpenseById(id)
}

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense) = repository.addExpense(expense)
}

class UpdateExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense) = repository.updateExpense(expense)
}

class DeleteExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(id: Int) = repository.deleteExpense(id)
}
