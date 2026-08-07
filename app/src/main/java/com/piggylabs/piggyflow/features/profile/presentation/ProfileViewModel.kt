package com.piggylabs.piggyflow.features.profile.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.piggyflow.features.profile.domain.usecase.SeedSampleDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val seedSampleDataUseCase: SeedSampleDataUseCase
) : ViewModel() {

    /** @param onDone receives true when the demo dataset was written. */
    fun seedSampleData(onDone: (added: Boolean) -> Unit) {
        viewModelScope.launch {
            val added = try {
                seedSampleDataUseCase()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seed sample data", e)
                false
            }
            onDone(added)
        }
    }

    private companion object {
        const val TAG = "SampleData"
    }
}
