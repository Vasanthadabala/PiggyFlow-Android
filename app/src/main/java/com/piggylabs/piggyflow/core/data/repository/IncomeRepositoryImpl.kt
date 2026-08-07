package com.piggylabs.piggyflow.core.data.repository

import com.piggylabs.piggyflow.core.database.dao.IncomeDao
import com.piggylabs.piggyflow.core.database.mapper.toDomain
import com.piggylabs.piggyflow.core.database.mapper.toEntity
import com.piggylabs.piggyflow.core.di.IoDispatcher
import com.piggylabs.piggyflow.core.domain.model.Income
import com.piggylabs.piggyflow.core.domain.repository.IncomeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider

class IncomeRepositoryImpl @Inject constructor(
    private val daoProvider: Provider<IncomeDao>,
    @IoDispatcher private val io: CoroutineDispatcher
) : IncomeRepository {

    /** Resolved per call so a restored database swap is picked up. */
    private val dao get() = daoProvider.get()

    override fun observeIncome(): Flow<List<Income>> =
        dao.getAllIncome().map { list -> list.map { it.toDomain() } }.flowOn(io)

    override fun observeIncomeById(id: Int): Flow<Income?> =
        dao.observeIncomeById(id).map { it?.toDomain() }.flowOn(io)

    override suspend fun addIncome(income: Income) = withContext(io) {
        dao.insertIncome(income.toEntity())
    }

    override suspend fun updateIncome(income: Income) = withContext(io) {
        dao.updateIncome(income.id, income.amount, income.note, income.date)
    }

    override suspend fun deleteIncome(id: Int) = withContext(io) {
        dao.deleteIncomeById(id)
    }
}
