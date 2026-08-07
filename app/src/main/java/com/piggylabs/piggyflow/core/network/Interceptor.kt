package com.piggylabs.piggyflow.core.network

import android.util.Log
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger

/** Routes Ktor's client logging into Logcat under a single tag. */
object NetworkLogger : Logger {
    private const val TAG = "PiggyFlowNetwork"

    override fun log(message: String) {
        Log.d(TAG, message)
    }
}

/** Log verbosity for the shared client - quiet in release, full traffic in debug. */
val defaultLogLevel: LogLevel
    get() = if (com.piggylabs.piggyflow.BuildConfig.DEBUG) LogLevel.HEADERS else LogLevel.NONE
