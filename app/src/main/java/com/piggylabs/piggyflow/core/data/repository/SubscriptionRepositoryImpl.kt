package com.piggylabs.piggyflow.core.data.repository

import com.piggylabs.piggyflow.core.database.dao.SubscriptionDao
import com.piggylabs.piggyflow.core.database.mapper.toDomain
import com.piggylabs.piggyflow.core.database.mapper.toEntity
import com.piggylabs.piggyflow.core.di.IoDispatcher
import com.piggylabs.piggyflow.core.domain.model.Subscription
import com.piggylabs.piggyflow.core.domain.repository.SubscriptionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider

class SubscriptionRepositoryImpl @Inject constructor(
    private val daoProvider: Provider<SubscriptionDao>,
    @IoDispatcher private val io: CoroutineDispatcher
) : SubscriptionRepository {

    /** Resolved per call so a restored database swap is picked up. */
    private val dao get() = daoProvider.get()

    override fun observeSubscriptions(): Flow<List<Subscription>> =
        dao.getAllSubscriptions().map { list -> list.map { it.toDomain() } }.flowOn(io)

    override suspend fun addSubscription(subscription: Subscription): Long = withContext(io) {
        dao.insertSubscription(subscription.toEntity())
    }

    override suspend fun updateSubscription(subscription: Subscription) = withContext(io) {
        dao.updateSubscription(
            id = subscription.id,
            type = subscription.type,
            name = subscription.name,
            subType = subscription.subType,
            amount = subscription.amount,
            dueDate = subscription.dueDate,
            logoUrl = subscription.logoUrl
        )
    }

    override suspend fun updateSubscriptionLogo(id: Int, logoUrl: String) = withContext(io) {
        dao.updateSubscriptionLogo(id, logoUrl)
    }

    override suspend fun deleteSubscription(id: Int) = withContext(io) {
        dao.deleteSubscriptionById(id)
    }
}
