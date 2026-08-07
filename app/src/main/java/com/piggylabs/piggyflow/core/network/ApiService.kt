package com.piggylabs.piggyflow.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Entry point for REST calls.
 *
 * PiggyFlow has no first-party backend today - Firebase Auth/Firestore and Google Drive
 * are reached through their own SDKs, and the only raw HTTP is the Brandfetch logo probe
 * in `features/tracker/data`. This wraps the shared [HttpClient] so that when a backend
 * is added, endpoints land here instead of being scattered across ViewModels.
 */
@Singleton
class ApiService @Inject constructor(
    private val client: HttpClient
) {
    /** Issues a GET against [url] using the shared, instrumented client. */
    suspend fun get(url: String): HttpResponse = client.get(url)
}
