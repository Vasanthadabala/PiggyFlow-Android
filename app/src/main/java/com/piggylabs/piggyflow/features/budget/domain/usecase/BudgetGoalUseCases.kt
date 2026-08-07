package com.piggylabs.piggyflow.features.budget.domain.usecase

import com.piggylabs.piggyflow.core.domain.model.BudgetGoal
import com.piggylabs.piggyflow.core.domain.repository.BudgetGoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBudgetGoalsUseCase @Inject constructor(
    private val repository: BudgetGoalRepository
) {
    operator fun invoke(): Flow<List<BudgetGoal>> = repository.observeBudgetGoals()
}

class ObserveBudgetGoalByIdUseCase @Inject constructor(
    private val repository: BudgetGoalRepository
) {
    operator fun invoke(id: Int): Flow<BudgetGoal?> = repository.observeBudgetGoalById(id)
}

class AddBudgetGoalUseCase @Inject constructor(
    private val repository: BudgetGoalRepository
) {
    suspend operator fun invoke(goal: BudgetGoal): Long = repository.addBudgetGoal(goal)
}

class UpdateBudgetGoalUseCase @Inject constructor(
    private val repository: BudgetGoalRepository
) {
    suspend operator fun invoke(goal: BudgetGoal) = repository.updateBudgetGoal(goal)
}

class DeleteBudgetGoalUseCase @Inject constructor(
    private val repository: BudgetGoalRepository
) {
    suspend operator fun invoke(id: Int) = repository.deleteBudgetGoal(id)
}
