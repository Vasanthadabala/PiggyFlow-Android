package com.piggylabs.piggyflow.core.data.repository

import com.piggylabs.piggyflow.core.database.dao.BudgetGoalDao
import com.piggylabs.piggyflow.core.database.mapper.toDomain
import com.piggylabs.piggyflow.core.database.mapper.toEntity
import com.piggylabs.piggyflow.core.di.IoDispatcher
import com.piggylabs.piggyflow.core.domain.model.BudgetGoal
import com.piggylabs.piggyflow.core.domain.repository.BudgetGoalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider

class BudgetGoalRepositoryImpl @Inject constructor(
    private val daoProvider: Provider<BudgetGoalDao>,
    @IoDispatcher private val io: CoroutineDispatcher
) : BudgetGoalRepository {

    /** Resolved per call so a restored database swap is picked up. */
    private val dao get() = daoProvider.get()

    override fun observeBudgetGoals(): Flow<List<BudgetGoal>> =
        dao.getAllBudgetGoals().map { list -> list.map { it.toDomain() } }.flowOn(io)

    override fun observeBudgetGoalById(id: Int): Flow<BudgetGoal?> =
        dao.observeBudgetGoalById(id).map { it?.toDomain() }.flowOn(io)

    override suspend fun addBudgetGoal(goal: BudgetGoal): Long = withContext(io) {
        dao.insertBudgetGoal(goal.toEntity())
    }

    override suspend fun updateBudgetGoal(goal: BudgetGoal) = withContext(io) {
        dao.updateBudgetGoal(goal.toEntity())
    }

    override suspend fun deleteBudgetGoal(id: Int) = withContext(io) {
        dao.deleteBudgetGoalById(id)
    }
}
