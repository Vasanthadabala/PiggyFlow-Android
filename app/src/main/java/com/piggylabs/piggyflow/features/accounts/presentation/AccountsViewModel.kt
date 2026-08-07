package com.piggylabs.piggyflow.features.accounts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.piggyflow.core.database.AppEvents
import com.piggylabs.piggyflow.core.domain.model.Account
import com.piggylabs.piggyflow.features.accounts.domain.usecase.AddAccountUseCase
import com.piggylabs.piggyflow.features.accounts.domain.usecase.DeleteAccountUseCase
import com.piggylabs.piggyflow.features.accounts.domain.usecase.ObserveAccountByIdUseCase
import com.piggylabs.piggyflow.features.accounts.domain.usecase.ObserveAccountsUseCase
import com.piggylabs.piggyflow.features.accounts.domain.usecase.UpdateAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.piggylabs.piggyflow.core.common.UiState

/** Snapshot of the accounts screen. */
data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    override val isLoading: Boolean = true,
    override val errorMessage: String? = null
) : UiState

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AccountsViewModel @Inject constructor(
    observeAccounts: ObserveAccountsUseCase,
    private val observeAccountByIdUseCase: ObserveAccountByIdUseCase,
    private val addAccountUseCase: AddAccountUseCase,
    private val updateAccountUseCase: UpdateAccountUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val restarts = MutableStateFlow(0)

    val uiState: StateFlow<AccountsUiState> = restarts
        .flatMapLatest { observeAccounts() }
        .map { AccountsUiState(accounts = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = AccountsUiState()
        )

    init {
        viewModelScope.launch {
            AppEvents.dbRecreated.collect { restarts.update { it + 1 } }
        }
    }

    fun observeAccountById(id: Int): Flow<Account?> = observeAccountByIdUseCase(id)

    fun addAccount(
        name: String,
        type: String,
        balance: Double,
        accountNumber: String,
        creditLimit: Double?,
        dueDay: Int?,
        colorArgb: Long
    ) = viewModelScope.launch {
        addAccountUseCase(
            Account(
                name = name,
                type = type,
                balance = balance,
                accountNumber = accountNumber,
                creditLimit = creditLimit,
                dueDay = dueDay,
                colorArgb = colorArgb
            )
        )
    }

    fun updateAccount(account: Account) = viewModelScope.launch {
        updateAccountUseCase(account)
    }

    fun deleteAccount(id: Int) = viewModelScope.launch {
        deleteAccountUseCase(id)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
