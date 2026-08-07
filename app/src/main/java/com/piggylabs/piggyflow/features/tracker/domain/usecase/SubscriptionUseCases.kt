package com.piggylabs.piggyflow.features.tracker.domain.usecase

import com.piggylabs.piggyflow.core.domain.model.Subscription
import com.piggylabs.piggyflow.core.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSubscriptionsUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    operator fun invoke(): Flow<List<Subscription>> = repository.observeSubscriptions()
}

class AddSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(subscription: Subscription): Long =
        repository.addSubscription(subscription)
}

class UpdateSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(subscription: Subscription) =
        repository.updateSubscription(subscription)
}

class UpdateSubscriptionLogoUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(id: Int, logoUrl: String) =
        repository.updateSubscriptionLogo(id, logoUrl)
}

class DeleteSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(id: Int) = repository.deleteSubscription(id)
}
