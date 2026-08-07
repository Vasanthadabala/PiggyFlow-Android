package com.piggylabs.piggyflow.core.common

/**
 * Contract every screen state implements, so loading and error handling look the same
 * across features. Feature states stay plain data classes - they just guarantee these
 * two fields exist.
 */
interface UiState {
    val isLoading: Boolean
    val errorMessage: String?
}
