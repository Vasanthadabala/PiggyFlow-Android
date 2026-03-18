package com.piggylabs.piggyflow.ui.screens.common.tracker.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.piggyflow.data.local.db.AppEvents
import com.piggylabs.piggyflow.data.local.entity.SubscriptionEntity
import com.piggylabs.piggyflow.data.local.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "TrackerBrandfetch"
        private const val BRANDFETCH_CLIENT_ID = "1idb3QiwFiyjBHINpgC"
    }

    private var repo = UserRepository(application)

    var subscriptions by mutableStateOf<List<SubscriptionEntity>>(emptyList())
        private set

    init {
        observeSubscriptions()

        viewModelScope.launch {
            AppEvents.dbRecreated.collect {
                onDbRecreated()
            }
        }
    }

    private fun onDbRecreated() {
        viewModelScope.launch {
            subscriptions = emptyList()
            repo = UserRepository(getApplication())
            observeSubscriptions()
        }
    }

    private fun observeSubscriptions() {
        viewModelScope.launch {
            repo.getAllSubscriptions().collect { list ->
                subscriptions = list
                Log.d(TAG, "DB observe: total subscriptions=${list.size}")
                list.forEach { item ->
                    Log.d(
                        TAG,
                        "DB row -> id=${item.id}, type=${item.type}, name=${item.name}, subType=${item.subType}, amount=${item.amount}, dueDate=${item.dueDate}, logoUrl=${item.logoUrl}"
                    )
                }
            }
        }
    }

    fun addSubscription(
        type: String,
        name: String,
        subType: String,
        amount: Double,
        dueDate: String
    ) = viewModelScope.launch {
        Log.d(TAG, "addSubscription: type=$type, name=$name, subType=$subType, amount=$amount, dueDate=$dueDate")
        val initialLogoUrl = buildPrimaryBrandfetchUrl(name)

        val insertedId = repo.addSubscription(
            SubscriptionEntity(
                type = type,
                name = name,
                subType = subType,
                amount = amount,
                dueDate = dueDate,
                logoUrl = initialLogoUrl
            )
        )
        Log.d(TAG, "Inserted subscription with logoUrl=$initialLogoUrl, id=$insertedId")

        if (insertedId > 0L && initialLogoUrl.isNotBlank()) {
            val logoUrl = fetchBrandfetchLogoUrl(name)
            if (logoUrl != null) {
                Log.d(TAG, "Brandfetch success. Updating subscription id=$insertedId with logoUrl=$logoUrl")
                repo.updateSubscriptionLogo(insertedId.toInt(), logoUrl)
            } else {
                Log.w(TAG, "Brandfetch failed. Will show default tracker icon for id=$insertedId")
            }
        } else {
            Log.d(TAG, "No known company match for '$name'. Using default tracker icon.")
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
        val initialLogoUrl = buildPrimaryBrandfetchUrl(name)
        repo.updateSubscription(
            SubscriptionEntity(
                id = id,
                type = type,
                name = name,
                subType = subType,
                amount = amount,
                dueDate = dueDate,
                logoUrl = initialLogoUrl
            )
        )

        if (initialLogoUrl.isNotBlank()) {
            fetchBrandfetchLogoUrl(name)?.let { logoUrl ->
                repo.updateSubscriptionLogo(id, logoUrl)
            }
        }
    }

    fun deleteSubscription(id: Int) = viewModelScope.launch {
        repo.deleteSubscriptionById(id)
    }

    private suspend fun fetchBrandfetchLogoUrl(companyName: String): String? = withContext(Dispatchers.IO) {
        val normalized = companyName.trim().lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (normalized.isBlank()) {
            Log.w(TAG, "fetchBrandfetchLogoUrl: blank company name")
            return@withContext null
        }
        Log.d(TAG, "Brandfetch lookup for normalized name='$normalized'")
        val mappedDomain = resolveKnownBrandDomain(normalized) ?: return@withContext null
        Log.d(TAG, "Known brand domain match=$mappedDomain")

        val urlCandidates = listOf(
            mappedDomain
        ).flatMap { domain ->
            listOf(
                "https://cdn.brandfetch.io/domain/$domain?c=$BRANDFETCH_CLIENT_ID",
                "https://cdn.brandfetch.io/$domain/icon?c=$BRANDFETCH_CLIENT_ID"
            )
        }
        Log.d(TAG, "URL candidates count=${urlCandidates.size}, clientIdSuffix=${BRANDFETCH_CLIENT_ID.takeLast(6)}")

        urlCandidates.firstOrNull { url ->
            isReachableImage(url)
        }
    }

    private fun buildPrimaryBrandfetchUrl(companyName: String): String {
        val normalized = companyName.trim().lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (normalized.isBlank()) return ""

        val mappedDomain = resolveKnownBrandDomain(normalized) ?: return ""
        return "https://cdn.brandfetch.io/domain/$mappedDomain?c=$BRANDFETCH_CLIENT_ID"
    }

    private fun resolveKnownBrandDomain(normalizedCompanyName: String): String? {
        val directDomainMap = mapOf(
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

        return directDomainMap.entries.firstOrNull { (key, _) ->
            normalizedCompanyName.contains(key)
        }?.value
    }

    private fun isReachableImage(url: String): Boolean {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.instanceFollowRedirects = true
            connection.connect()
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
    }
}
