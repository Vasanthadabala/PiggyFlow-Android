package com.piggylabs.piggyflow.ui.screens.business.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.piggyflow.data.local.db.AppEvents
import com.piggylabs.piggyflow.data.local.entity.BusinessEntryEntity
import com.piggylabs.piggyflow.data.local.entity.BusinessPartyEntity
import com.piggylabs.piggyflow.data.local.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class BusinessPartySummary(
    val party: BusinessPartyEntity,
    val balance: Double,
    val lastEntryAt: Long?
)

class BusinessLedgerViewModel(application: Application) : AndroidViewModel(application) {
    private var repo = UserRepository(application)

    val partySummaries: Flow<List<BusinessPartySummary>> =
        combine(
            repo.getAllBusinessParties(),
            repo.getAllBusinessEntries()
        ) { parties, entries ->
            parties.map { party ->
                val partyEntries = entries.filter { it.partyId == party.id }
                val balance = partyEntries.sumOf { entry ->
                    if (entry.type == "gave") entry.amount else -entry.amount
                }
                BusinessPartySummary(
                    party = party,
                    balance = balance,
                    lastEntryAt = partyEntries.maxOfOrNull { it.createdAt }
                )
            }.sortedByDescending { it.lastEntryAt ?: it.party.updatedAt }
        }

    init {
        viewModelScope.launch {
            AppEvents.dbRecreated.collect {
                repo = UserRepository(getApplication())
            }
        }
    }

    fun addParty(
        name: String,
        phone: String,
        address: String,
        onCreated: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val partyId = repo.addBusinessParty(
                BusinessPartyEntity(
                    name = name.trim(),
                    phone = phone.trim(),
                    address = address.trim()
                )
            )
            onCreated(partyId.toInt())
        }
    }

    fun addEntry(
        partyId: Int,
        type: String,
        amount: Double,
        note: String,
        onCreated: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entryId = repo.addBusinessEntry(
                BusinessEntryEntity(
                    partyId = partyId,
                    type = type,
                    amount = amount,
                    note = note.trim(),
                    createdAt = now
                )
            )
            repo.updateBusinessPartyTimestamp(partyId, now)
            onCreated(entryId.toInt())
        }
    }

    fun settleBalance(partyId: Int, balance: Double) {
        if (balance == 0.0) return
        val type = if (balance > 0) "got" else "gave"
        addEntry(
            partyId = partyId,
            type = type,
            amount = kotlin.math.abs(balance),
            note = "Settlement"
        )
    }

    fun deleteEntry(entryId: Int) {
        viewModelScope.launch {
            repo.deleteBusinessEntryById(entryId)
        }
    }

    fun deleteParty(partyId: Int) {
        viewModelScope.launch {
            repo.deleteBusinessPartyById(partyId)
        }
    }

    fun saveAcceptedCustomerRequest(
        partyName: String,
        partyPhone: String,
        type: String,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch {
            val currentParties = repo.getAllBusinessParties().first()
            val party = currentParties.firstOrNull {
                it.name.equals(partyName, ignoreCase = true) &&
                    (partyPhone.isBlank() || it.phone == partyPhone)
            }

            val partyId = if (party == null) {
                repo.addBusinessParty(
                    BusinessPartyEntity(
                        name = partyName.trim().ifBlank { "Customer" },
                        phone = partyPhone.trim(),
                        address = ""
                    )
                ).toInt()
            } else {
                party.id
            }

            addEntry(
                partyId = partyId,
                type = type,
                amount = amount,
                note = if (note.isBlank()) "Accepted request" else note
            )
        }
    }

    fun observeParty(partyId: Int): Flow<BusinessPartyEntity?> =
        repo.observeBusinessPartyById(partyId)

    fun observeEntries(partyId: Int): Flow<List<BusinessEntryEntity>> =
        repo.getBusinessEntriesForParty(partyId)
}
