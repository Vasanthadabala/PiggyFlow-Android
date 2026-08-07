package com.piggylabs.piggyflow.features.tracker.domain.repository

/** Resolves a subscription's brand logo. */
interface BrandLogoRepository {

    /**
     * Best-guess logo URL derived from the name alone, with no network call, so a new
     * subscription can be saved with artwork immediately. Blank when the brand is unknown.
     */
    fun guessLogoUrl(companyName: String): String

    /**
     * Verified logo URL, probing candidates over the network.
     * @return null when nothing reachable was found.
     */
    suspend fun resolveLogoUrl(companyName: String): String?
}
