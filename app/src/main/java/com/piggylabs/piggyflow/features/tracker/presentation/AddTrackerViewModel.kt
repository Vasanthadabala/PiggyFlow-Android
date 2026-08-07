package com.piggylabs.piggyflow.features.tracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.piggyflow.core.domain.model.BudgetGoal
import com.piggylabs.piggyflow.core.domain.model.Subscription
import com.piggylabs.piggyflow.features.budget.domain.usecase.AddBudgetGoalUseCase
import com.piggylabs.piggyflow.features.tracker.domain.repository.BrandLogoRepository
import com.piggylabs.piggyflow.features.tracker.domain.usecase.AddSubscriptionUseCase
import com.piggylabs.piggyflow.features.tracker.domain.usecase.UpdateSubscriptionLogoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Write side of the Add Tracker screen.
 *
 * Only two of the four tracker types have somewhere to go today: subscriptions and
 * EMIs share [Subscription], and budgets map onto [BudgetGoal]. Goal trackers have no
 * entity yet, so the screen collects them but cannot save them.
 */
@HiltViewModel
class AddTrackerViewModel @Inject constructor(
    private val addSubscriptionUseCase: AddSubscriptionUseCase,
    private val updateSubscriptionLogoUseCase: UpdateSubscriptionLogoUseCase,
    private val addBudgetGoalUseCase: AddBudgetGoalUseCase,
    private val brandLogos: BrandLogoRepository
) : ViewModel() {

    /**
     * @param type either "subscription" or "emi" — the same values the tracker list filters on.
     * @param subType billing cycle, e.g. "monthly".
     * @param dueDate ISO-8601, since the tracker list parses it with `LocalDate.parse`.
     */
    fun addSubscription(
        type: String,
        name: String,
        subType: String,
        amount: Double,
        dueDate: String
    ) = viewModelScope.launch {
        // Save the name-derived guess first so the row has artwork immediately, then
        // upgrade to a verified logo once the network probe returns.
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

    fun addBudgetGoal(
        categoryName: String,
        categoryEmoji: String,
        priority: String,
        monthlyLimit: Double
    ) = viewModelScope.launch {
        addBudgetGoalUseCase(
            BudgetGoal(
                categoryName = categoryName,
                categoryEmoji = categoryEmoji,
                priority = priority,
                monthlyLimit = monthlyLimit
            )
        )
    }
}
