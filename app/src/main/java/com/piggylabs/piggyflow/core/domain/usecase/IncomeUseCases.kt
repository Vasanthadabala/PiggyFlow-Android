package com.piggylabs.piggyflow.core.domain.usecase

import com.piggylabs.piggyflow.core.domain.model.Income
import com.piggylabs.piggyflow.core.domain.repository.IncomeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveIncomeUseCase @Inject constructor(
    private val repository: IncomeRepository
) {
    operator fun invoke(): Flow<List<Income>> = repository.observeIncome()
}

class ObserveIncomeByIdUseCase @Inject constructor(
    private val repository: IncomeRepository
) {
    operator fun invoke(id: Int): Flow<Income?> = repository.observeIncomeById(id)
}

class AddIncomeUseCase @Inject constructor(
    private val repository: IncomeRepository
) {
    suspend operator fun invoke(income: Income) = repository.addIncome(income)
}

class UpdateIncomeUseCase @Inject constructor(
    private val repository: IncomeRepository
) {
    suspend operator fun invoke(income: Income) = repository.updateIncome(income)
}

class DeleteIncomeUseCase @Inject constructor(
    private val repository: IncomeRepository
) {
    suspend operator fun invoke(id: Int) = repository.deleteIncome(id)
}
