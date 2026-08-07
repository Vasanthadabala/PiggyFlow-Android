package com.piggylabs.piggyflow.core.domain.repository

import com.piggylabs.piggyflow.core.domain.model.BusinessEntry
import com.piggylabs.piggyflow.core.domain.model.BusinessParty
import com.piggylabs.piggyflow.core.domain.model.Category
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.Income
import com.piggylabs.piggyflow.core.domain.model.Subscription

/** Everything held locally, as one value - the unit the Firestore backup ships around. */
data class BackupSnapshot(
    val categories: List<Category> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val incomes: List<Income> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val businessParties: List<BusinessParty> = emptyList(),
    val businessEntries: List<BusinessEntry> = emptyList()
) {
    val isEmpty: Boolean
        get() = categories.isEmpty() && expenses.isEmpty() && incomes.isEmpty() &&
            subscriptions.isEmpty() && businessParties.isEmpty() && businessEntries.isEmpty()
}

/**
 * Bulk local-store access for backup, restore and account switching. Keeps the sync
 * screens off Room - they deal in [BackupSnapshot] and never touch a DAO.
 */
interface BackupRepository {

    /** Reads everything currently stored on the device. */
    suspend fun snapshot(): BackupSnapshot

    /** Inserts every row in [snapshot], replacing any local row with a matching id. */
    suspend fun insertAll(snapshot: BackupSnapshot)

    /** Wipes all local records. */
    suspend fun clearAll()
}
