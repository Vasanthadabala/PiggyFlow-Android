package com.piggylabs.piggyflow.core.data.repository

import com.piggylabs.piggyflow.core.database.dao.ExpenseDao
import com.piggylabs.piggyflow.core.database.mapper.toDomain
import com.piggylabs.piggyflow.core.database.mapper.toEntity
import com.piggylabs.piggyflow.core.di.IoDispatcher
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider

class ExpenseRepositoryImpl @Inject constructor(
    private val daoProvider: Provider<ExpenseDao>,
    @IoDispatcher private val io: CoroutineDispatcher
) : ExpenseRepository {

    /** Resolved per call so a restored database swap is picked up. */
    private val dao get() = daoProvider.get()

    override fun observeExpenses(): Flow<List<Expense>> =
        dao.getAllExpenses().map { list -> list.map { it.toDomain() } }.flowOn(io)

    override fun observeExpenseById(id: Int): Flow<Expense?> =
        dao.observeExpenseById(id).map { it?.toDomain() }.flowOn(io)

    override suspend fun addExpense(expense: Expense) = withContext(io) {
        dao.insertExpense(expense.toEntity())
    }

    override suspend fun updateExpense(expense: Expense) = withContext(io) {
        dao.updateExpense(expense.id, expense.amount, expense.note, expense.date)
    }

    override suspend fun deleteExpense(id: Int) = withContext(io) {
        dao.deleteExpenseById(id)
    }
}
