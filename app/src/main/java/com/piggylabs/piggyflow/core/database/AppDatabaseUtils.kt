package com.piggylabs.piggyflow.core.database

import android.content.Context

fun closeDatabase(context: Context) {
    try {
        val db = AppDatabase.getExistingInstance()
        db?.close()
        AppDatabase.clearInstance()
    } catch (_: Exception) {}
}

fun reopenDatabase(context: Context) {
    try {
        AppDatabase.getDatabase(context) // fresh instance recreated
    } catch (_: Exception) {}
}
