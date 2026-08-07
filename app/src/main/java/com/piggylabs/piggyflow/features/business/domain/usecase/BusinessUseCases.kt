package com.piggylabs.piggyflow.features.business.domain.usecase

import com.piggylabs.piggyflow.core.domain.model.BusinessEntry
import com.piggylabs.piggyflow.core.domain.model.BusinessEntryType
import com.piggylabs.piggyflow.core.domain.model.BusinessParty
import com.piggylabs.piggyflow.core.domain.repository.BusinessRepository
import com.piggylabs.piggyflow.features.business.domain.model.BusinessPartySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Ledger balances per party, newest activity first. The balance arithmetic used to live in
 * the ViewModel; it is business logic, so it belongs here where it can be unit tested.
 */
class ObservePartySummariesUseCase @Inject constructor(
    private val repository: BusinessRepository
) {
    operator fun invoke(): Flow<List<BusinessPartySummary>> =
        combine(repository.observeParties(), repository.observeEntries()) { parties, entries ->
            parties.map { party ->
                val partyEntries = entries.filter { it.partyId == party.id }
                BusinessPartySummary(
                    party = party,
                    balance = partyEntries.sumOf { entry ->
                        if (entry.type == BusinessEntryType.GAVE) entry.amount else -entry.amount
                    },
                    lastEntryAt = partyEntries.maxOfOrNull { it.createdAt }
                )
            }.sortedByDescending { it.lastEntryAt ?: it.party.updatedAt }
        }
}

class ObservePartyUseCase @Inject constructor(
    private val repository: BusinessRepository
) {
    operator fun invoke(partyId: Int): Flow<BusinessParty?> = repository.observePartyById(partyId)
}

class ObservePartyEntriesUseCase @Inject constructor(
    private val repository: BusinessRepository
) {
    operator fun invoke(partyId: Int): Flow<List<BusinessEntry>> =
        repository.observeEntriesForParty(partyId)
}

class AddPartyUseCase @Inject constructor(
    private val repository: BusinessRepository
) {
    suspend operator fun invoke(name: String, phone: String, address: String): Int =
        repository.addParty(
            BusinessParty(name = name.trim(), phone = phone.trim(), address = address.trim())
        ).toInt()
}

/** Adds a ledger entry and bumps the party's activity timestamp in one step. */
class AddEntryUseCase @Inject constructor(
    private val repository: BusinessRepository
) {
    suspend operator fun invoke(
        partyId: Int,
        type: String,
        amount: Double,
        note: String
    ): Int {
        val now = System.currentTimeMillis()
        val entryId = repository.addEntry(
            BusinessEntry(
                partyId = partyId,
                type = type,
                amount = amount,
                note = note.trim(),
                createdAt = now
            )
        )
        repository.touchParty(partyId, now)
        return entryId.toInt()
    }
}

/** Books the counter-entry that zeroes a party's outstanding balance. */
class SettleBalanceUseCase @Inject constructor(
    private val addEntry: AddEntryUseCase
) {
    suspend operator fun invoke(partyId: Int, balance: Double) {
        if (balance == 0.0) return
        addEntry(
            partyId = partyId,
            type = if (balance > 0) BusinessEntryType.GOT else BusinessEntryType.GAVE,
            amount = kotlin.math.abs(balance),
            note = "Settlement"
        )
    }
}

/**
 * Records an accepted customer request against the matching party, creating that party
 * first if this is the first time the owner has seen them.
 */
class SaveAcceptedCustomerRequestUseCase @Inject constructor(
    private val repository: BusinessRepository,
    private val addParty: AddPartyUseCase,
    private val addEntry: AddEntryUseCase
) {
    suspend operator fun invoke(
        partyName: String,
        partyPhone: String,
        type: String,
        amount: Double,
        note: String
    ) {
        val existing = repository.observeParties().first().firstOrNull {
            it.name.equals(partyName, ignoreCase = true) &&
                (partyPhone.isBlank() || it.phone == partyPhone)
        }

        val partyId = existing?.id ?: addParty(
            name = partyName.trim().ifBlank { "Customer" },
            phone = partyPhone.trim(),
            address = ""
        )

        addEntry(
            partyId = partyId,
            type = type,
            amount = amount,
            note = note.ifBlank { "Accepted request" }
        )
    }
}

class DeleteEntryUseCase @Inject constructor(
    private val repository: BusinessRepository
) {
    suspend operator fun invoke(entryId: Int) = repository.deleteEntry(entryId)
}

class DeletePartyUseCase @Inject constructor(
    private val repository: BusinessRepository
) {
    suspend operator fun invoke(partyId: Int) = repository.deleteParty(partyId)
}
