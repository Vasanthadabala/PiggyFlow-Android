package com.piggylabs.piggyflow.features.tracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.piggyflow.core.database.AppEvents
import com.piggylabs.piggyflow.core.domain.model.Subscription
import com.piggylabs.piggyflow.features.tracker.domain.repository.BrandLogoRepository
import com.piggylabs.piggyflow.features.tracker.domain.usecase.AddSubscriptionUseCase
import com.piggylabs.piggyflow.features.tracker.domain.usecase.DeleteSubscriptionUseCase
import com.piggylabs.piggyflow.features.tracker.domain.usecase.ObserveSubscriptionsUseCase
import com.piggylabs.piggyflow.features.tracker.domain.usecase.UpdateSubscriptionLogoUseCase
import com.piggylabs.piggyflow.features.tracker.domain.usecase.UpdateSubscriptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

/** Snapshot of the subscription tracker screen. */
data class TrackerUiState(
    val subscriptions: List<Subscription> = emptyList(),
    override val isLoading: Boolean = true,
    override val errorMessage: String? = null
) : UiState

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrackerViewModel @Inject constructor(
    observeSubscriptions: ObserveSubscriptionsUseCase,
    private val addSubscriptionUseCase: AddSubscriptionUseCase,
    private val updateSubscriptionUseCase: UpdateSubscriptionUseCase,
    private val updateSubscriptionLogoUseCase: UpdateSubscriptionLogoUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase,
    private val brandLogos: BrandLogoRepository
) : ViewModel() {

    private val restarts = MutableStateFlow(0)

    val uiState: StateFlow<TrackerUiState> = restarts
        .flatMapLatest { observeSubscriptions() }
        .map { TrackerUiState(subscriptions = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = TrackerUiState()
        )

    init {
        viewModelScope.launch {
            AppEvents.dbRecreated.collect { restarts.update { it + 1 } }
        }
    }

    fun addSubscription(
        type: String,
        name: String,
        subType: String,
        amount: Double,
        dueDate: String
    ) = viewModelScope.launch {
        // Save with the name-derived guess first so the row has artwork immediately,
        // then upgrade to a verified logo once the network probe comes back.
        val guessedLogo = brandLogos.guessLogoUrl(name)
        val insertedId = addSubscriptionUseCase(
            Subscription(
                type = type,
                name = name,
                subType = subType,
                amount = amount,
                dueDate = dueDate,
                logoUrl = guessedLogo
            )
        )

        if (insertedId > 0L && guessedLogo.isNotBlank()) {
            brandLogos.resolveLogoUrl(name)?.let { resolved ->
                updateSubscriptionLogoUseCase(insertedId.toInt(), resolved)
            }
        }
    }

    fun updateSubscription(
        id: Int,
        type: String,
        name: String,
        subType: String,
        amount: Double,
        dueDate: String
    ) = viewModelScope.launch {
        val guessedLogo = brandLogos.guessLogoUrl(name)
        updateSubscriptionUseCase(
            Subscription(
                id = id,
                type = type,
                name = name,
                subType = subType,
                amount = amount,
                dueDate = dueDate,
                logoUrl = guessedLogo
            )
        )

        if (guessedLogo.isNotBlank()) {
            brandLogos.resolveLogoUrl(name)?.let { resolved ->
                updateSubscriptionLogoUseCase(id, resolved)
            }
        }
    }

    fun deleteSubscription(id: Int) = viewModelScope.launch {
        deleteSubscriptionUseCase(id)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
