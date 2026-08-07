package com.piggylabs.piggyflow.features.tracker.data

import android.util.Log
import com.piggylabs.piggyflow.core.di.IoDispatcher
import com.piggylabs.piggyflow.features.tracker.domain.repository.BrandLogoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/** Brandfetch-backed [BrandLogoRepository]. */
class BrandfetchLogoRepository @Inject constructor(
    @IoDispatcher private val io: CoroutineDispatcher
) : BrandLogoRepository {

    override fun guessLogoUrl(companyName: String): String {
        val domain = resolveKnownBrandDomain(normalize(companyName)) ?: return ""
        return "https://cdn.brandfetch.io/domain/$domain?c=$CLIENT_ID"
    }

    override suspend fun resolveLogoUrl(companyName: String): String? = withContext(io) {
        val normalized = normalize(companyName)
        if (normalized.isBlank()) {
            Log.w(TAG, "resolveLogoUrl: blank company name")
            return@withContext null
        }

        Log.d(TAG, "Brandfetch lookup for normalized name='$normalized'")
        val domain = resolveKnownBrandDomain(normalized) ?: return@withContext null
        Log.d(TAG, "Known brand domain match=$domain")

        listOf(
            "https://cdn.brandfetch.io/domain/$domain?c=$CLIENT_ID",
            "https://cdn.brandfetch.io/$domain/icon?c=$CLIENT_ID"
        ).firstOrNull { isReachableImage(it) }
    }

    private fun normalize(companyName: String): String =
        companyName.trim().lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun resolveKnownBrandDomain(normalizedCompanyName: String): String? =
        KNOWN_DOMAINS.entries
            .firstOrNull { (key, _) -> normalizedCompanyName.contains(key) }
            ?.value

    private fun isReachableImage(url: String): Boolean = try {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            instanceFollowRedirects = true
            connect()
        }
        val code = connection.responseCode
        val contentType = connection.contentType.orEmpty()
        connection.disconnect()
        val ok = code in 200..299 && contentType.startsWith("image")
        Log.d(TAG, "Brandfetch probe: url=$url code=$code contentType=$contentType ok=$ok")
        ok
    } catch (e: Exception) {
        Log.e(TAG, "Brandfetch probe failed for url=$url", e)
        false
    }

    private companion object {
        const val TAG = "TrackerBrandfetch"
        const val CLIENT_ID = "1idb3QiwFiyjBHINpgC"
        const val TIMEOUT_MS = 3000

        val KNOWN_DOMAINS = mapOf(
            "google" to "google.com",
            "youtube" to "youtube.com",
            "spotify" to "spotify.com",
            "netflix" to "netflix.com",
            "amazon prime" to "primevideo.com",
            "prime video" to "primevideo.com",
            "amazon" to "amazon.com",
            "chatgpt" to "openai.com",
            "openai" to "openai.com",
            "hotstar" to "hotstar.com",
            "jio" to "jio.com",
            "airtel" to "airtel.in",
            "vodafone" to "myvi.in",
            "vi" to "myvi.in",
            "adobe" to "adobe.com",
            "microsoft" to "microsoft.com",
            "apple" to "apple.com",
            "hdfc" to "hdfcbank.com",
            "icici" to "icicibank.com",
            "sbi" to "sbi.co.in",
            "axis" to "axisbank.com",
            "tesla" to "tesla.com",
            "toyota" to "toyota.com",
            "honda" to "honda.com",
            "hyundai" to "hyundai.com",
            "kia" to "kia.com",
            "mahindra" to "mahindra.com",
            "tata" to "tatamotors.com",
            "suzuki" to "suzuki.com"
        )
    }
}
