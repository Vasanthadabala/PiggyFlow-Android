package com.piggylabs.piggyflow.features.business.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.piggyflow.core.database.AppEvents
import com.piggylabs.piggyflow.core.domain.model.BusinessEntry
import com.piggylabs.piggyflow.core.domain.model.BusinessParty
import com.piggylabs.piggyflow.features.business.domain.model.BusinessPartySummary
import com.piggylabs.piggyflow.features.business.domain.usecase.AddEntryUseCase
import com.piggylabs.piggyflow.features.business.domain.usecase.AddPartyUseCase
import com.piggylabs.piggyflow.features.business.domain.usecase.DeleteEntryUseCase
import com.piggylabs.piggyflow.features.business.domain.usecase.DeletePartyUseCase
import com.piggylabs.piggyflow.features.business.domain.usecase.ObservePartyEntriesUseCase
import com.piggylabs.piggyflow.features.business.domain.usecase.ObservePartySummariesUseCase
import com.piggylabs.piggyflow.features.business.domain.usecase.ObservePartyUseCase
import com.piggylabs.piggyflow.features.business.domain.usecase.SaveAcceptedCustomerRequestUseCase
import com.piggylabs.piggyflow.features.business.domain.usecase.SettleBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BusinessLedgerViewModel @Inject constructor(
    observePartySummaries: ObservePartySummariesUseCase,
    private val observePartyUseCase: ObservePartyUseCase,
    private val observePartyEntriesUseCase: ObservePartyEntriesUseCase,
    private val addPartyUseCase: AddPartyUseCase,
    private val addEntryUseCase: AddEntryUseCase,
    private val settleBalanceUseCase: SettleBalanceUseCase,
    private val saveAcceptedCustomerRequestUseCase: SaveAcceptedCustomerRequestUseCase,
    private val deleteEntryUseCase: DeleteEntryUseCase,
    private val deletePartyUseCase: DeletePartyUseCase
) : ViewModel() {

    private val restarts = MutableStateFlow(0)

    val partySummaries: Flow<List<BusinessPartySummary>> =
        restarts.flatMapLatest { observePartySummaries() }

    init {
        viewModelScope.launch {
            AppEvents.dbRecreated.collect { restarts.update { it + 1 } }
        }
    }

    fun observeParty(partyId: Int): Flow<BusinessParty?> = observePartyUseCase(partyId)

    fun observeEntries(partyId: Int): Flow<List<BusinessEntry>> =
        observePartyEntriesUseCase(partyId)

    fun addParty(
        name: String,
        phone: String,
        address: String,
        onCreated: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            onCreated(addPartyUseCase(name, phone, address))
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
            onCreated(addEntryUseCase(partyId, type, amount, note))
        }
    }

    fun settleBalance(partyId: Int, balance: Double) {
        viewModelScope.launch { settleBalanceUseCase(partyId, balance) }
    }

    fun saveAcceptedCustomerRequest(
        partyName: String,
        partyPhone: String,
        type: String,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch {
            saveAcceptedCustomerRequestUseCase(partyName, partyPhone, type, amount, note)
        }
    }

    fun deleteEntry(entryId: Int) {
        viewModelScope.launch { deleteEntryUseCase(entryId) }
    }

    fun deleteParty(partyId: Int) {
        viewModelScope.launch { deletePartyUseCase(partyId) }
    }
}
