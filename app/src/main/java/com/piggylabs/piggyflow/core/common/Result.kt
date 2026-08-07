package com.piggylabs.piggyflow.core.common

/**
 * Outcome of an operation that can fail for reasons the UI must react to
 * (sync, backup, restore, seeding). Kept separate from [kotlin.Result] so the
 * failure carries a message the presentation layer can show as-is.
 */
sealed interface Result<out T> {

    data class Success<T>(val data: T) : Result<T>

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : Result<Nothing>

    val isSuccess: Boolean get() = this is Success

    /** The value on success, or null when this is a [Failure]. */
    fun getOrNull(): T? = (this as? Success)?.data
}

/** Runs [block], converting any thrown exception into a [Result.Failure]. */
inline fun <T> runCatchingResult(
    fallbackMessage: String,
    block: () -> T
): Result<T> = try {
    Result.Success(block())
} catch (e: Exception) {
    Result.Failure(e.message ?: fallbackMessage, e)
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onFailure(action: (Result.Failure) -> Unit): Result<T> {
    if (this is Result.Failure) action(this)
    return this
}
