package com.piggylabs.piggyflow.core.data.repository

import com.piggylabs.piggyflow.core.database.dao.AccountDao
import com.piggylabs.piggyflow.core.database.mapper.toDomain
import com.piggylabs.piggyflow.core.database.mapper.toEntity
import com.piggylabs.piggyflow.core.di.IoDispatcher
import com.piggylabs.piggyflow.core.domain.model.Account
import com.piggylabs.piggyflow.core.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider

class AccountRepositoryImpl @Inject constructor(
    private val daoProvider: Provider<AccountDao>,
    @IoDispatcher private val io: CoroutineDispatcher
) : AccountRepository {

    /** Resolved per call so a restored database swap is picked up. */
    private val dao get() = daoProvider.get()

    override fun observeAccounts(): Flow<List<Account>> =
        dao.getAllAccounts().map { list -> list.map { it.toDomain() } }.flowOn(io)

    override fun observeAccountById(id: Int): Flow<Account?> =
        dao.observeAccountById(id).map { it?.toDomain() }.flowOn(io)

    override suspend fun addAccount(account: Account): Long = withContext(io) {
        dao.insertAccount(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) = withContext(io) {
        dao.updateAccount(account.toEntity())
    }

    override suspend fun deleteAccount(id: Int) = withContext(io) {
        dao.deleteAccountById(id)
    }
}
