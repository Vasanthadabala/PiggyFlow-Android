package com.piggylabs.piggyflow.core.data.repository

import com.piggylabs.piggyflow.core.database.AppDatabase
import com.piggylabs.piggyflow.core.database.mapper.toDomain
import com.piggylabs.piggyflow.core.database.mapper.toEntity
import com.piggylabs.piggyflow.core.di.IoDispatcher
import com.piggylabs.piggyflow.core.domain.repository.BackupRepository
import com.piggylabs.piggyflow.core.domain.repository.BackupSnapshot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider

class BackupRepositoryImpl @Inject constructor(
    private val databaseProvider: Provider<AppDatabase>,
    @IoDispatcher private val io: CoroutineDispatcher
) : BackupRepository {

    /** Resolved per call so a restored database swap is picked up. */
    private val db get() = databaseProvider.get()

    override suspend fun snapshot(): BackupSnapshot = withContext(io) {
        val database = db
        BackupSnapshot(
            categories = database.userCategoryDao().getAllCategories().first().map { it.toDomain() },
            expenses = database.expenseDao().getAllExpenses().first().map { it.toDomain() },
            incomes = database.incomeDao().getAllIncome().first().map { it.toDomain() },
            subscriptions = database.subscriptionDao().getAllSubscriptions().first()
                .map { it.toDomain() },
            businessParties = database.businessPartyDao().getAllBusinessParties().first()
                .map { it.toDomain() },
            businessEntries = database.businessEntryDao().getAllBusinessEntries().first()
                .map { it.toDomain() }
        )
    }

    override suspend fun insertAll(snapshot: BackupSnapshot) = withContext(io) {
        val database = db
        if (snapshot.categories.isNotEmpty()) {
            database.userCategoryDao().insertAllCategories(snapshot.categories.map { it.toEntity() })
        }
        if (snapshot.expenses.isNotEmpty()) {
            database.expenseDao().insertAllExpenses(snapshot.expenses.map { it.toEntity() })
        }
        if (snapshot.incomes.isNotEmpty()) {
            database.incomeDao().insertAllIncome(snapshot.incomes.map { it.toEntity() })
        }
        snapshot.subscriptions.forEach {
            database.subscriptionDao().insertSubscription(it.toEntity())
        }
        snapshot.businessParties.forEach {
            database.businessPartyDao().insertBusinessParty(it.toEntity())
        }
        snapshot.businessEntries.forEach {
            database.businessEntryDao().insertBusinessEntry(it.toEntity())
        }
    }

    override suspend fun clearAll() = withContext(io) {
        val database = db
        database.incomeDao().clearAllIncome()
        database.expenseDao().clearAllExpenses()
        database.userCategoryDao().clearAllCategories()

        database.subscriptionDao().getAllSubscriptions().first()
            .forEach { database.subscriptionDao().deleteSubscriptionById(it.id) }
        database.businessEntryDao().getAllBusinessEntries().first()
            .forEach { database.businessEntryDao().deleteBusinessEntryById(it.id) }
        database.businessPartyDao().getAllBusinessParties().first()
            .forEach { database.businessPartyDao().deleteBusinessPartyById(it.id) }
    }
}
