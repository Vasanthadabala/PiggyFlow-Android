package com.piggylabs.piggyflow.features.accounts.domain.usecase

import com.piggylabs.piggyflow.core.domain.model.Account
import com.piggylabs.piggyflow.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAccountsUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(): Flow<List<Account>> = repository.observeAccounts()
}

class ObserveAccountByIdUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    operator fun invoke(id: Int): Flow<Account?> = repository.observeAccountById(id)
}

class AddAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(account: Account): Long = repository.addAccount(account)
}

class UpdateAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(account: Account) = repository.updateAccount(account)
}

class DeleteAccountUseCase @Inject constructor(
    private val repository: AccountRepository
) {
    suspend operator fun invoke(id: Int) = repository.deleteAccount(id)
}
