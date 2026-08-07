package com.piggylabs.piggyflow.features.business.domain.model

import com.piggylabs.piggyflow.core.domain.model.BusinessParty

/** A party plus its computed ledger position - what the business home list renders. */
data class BusinessPartySummary(
    val party: BusinessParty,
    val balance: Double,
    val lastEntryAt: Long?
)
