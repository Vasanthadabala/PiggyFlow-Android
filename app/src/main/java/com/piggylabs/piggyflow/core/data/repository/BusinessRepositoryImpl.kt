package com.piggylabs.piggyflow.core.data.repository

import com.piggylabs.piggyflow.core.database.dao.BusinessEntryDao
import com.piggylabs.piggyflow.core.database.dao.BusinessPartyDao
import com.piggylabs.piggyflow.core.database.mapper.toDomain
import com.piggylabs.piggyflow.core.database.mapper.toEntity
import com.piggylabs.piggyflow.core.di.IoDispatcher
import com.piggylabs.piggyflow.core.domain.model.BusinessEntry
import com.piggylabs.piggyflow.core.domain.model.BusinessParty
import com.piggylabs.piggyflow.core.domain.repository.BusinessRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider

class BusinessRepositoryImpl @Inject constructor(
    private val partyDaoProvider: Provider<BusinessPartyDao>,
    private val entryDaoProvider: Provider<BusinessEntryDao>,
    @IoDispatcher private val io: CoroutineDispatcher
) : BusinessRepository {

    /** Resolved per call so a restored database swap is picked up. */
    private val partyDao get() = partyDaoProvider.get()

    /** Resolved per call so a restored database swap is picked up. */
    private val entryDao get() = entryDaoProvider.get()

    override fun observeParties(): Flow<List<BusinessParty>> =
        partyDao.getAllBusinessParties().map { list -> list.map { it.toDomain() } }.flowOn(io)

    override fun observePartyById(partyId: Int): Flow<BusinessParty?> =
        partyDao.observeBusinessPartyById(partyId).map { it?.toDomain() }.flowOn(io)

    override fun observeEntries(): Flow<List<BusinessEntry>> =
        entryDao.getAllBusinessEntries().map { list -> list.map { it.toDomain() } }.flowOn(io)

    override fun observeEntriesForParty(partyId: Int): Flow<List<BusinessEntry>> =
        entryDao.getBusinessEntriesForParty(partyId).map { list -> list.map { it.toDomain() } }
            .flowOn(io)

    override suspend fun addParty(party: BusinessParty): Long = withContext(io) {
        partyDao.insertBusinessParty(party.toEntity())
    }

    override suspend fun addEntry(entry: BusinessEntry): Long = withContext(io) {
        entryDao.insertBusinessEntry(entry.toEntity())
    }

    override suspend fun touchParty(partyId: Int, updatedAt: Long) = withContext(io) {
        partyDao.updateBusinessPartyTimestamp(partyId, updatedAt)
    }

    override suspend fun deleteEntry(entryId: Int) = withContext(io) {
        entryDao.deleteBusinessEntryById(entryId)
    }

    override suspend fun deleteParty(partyId: Int) = withContext(io) {
        entryDao.deleteBusinessEntriesForParty(partyId)
        partyDao.deleteBusinessPartyById(partyId)
    }
}
