package com.piggylabs.piggyflow.data.local.db

import android.content.Context

fun closeDatabase(context: Context) {
    try {
        val db = AppDataBase.getExistingInstance()
        db?.close()
        AppDataBase.clearInstance()
    } catch (_: Exception) {}
}

fun reopenDatabase(context: Context) {
    try {
        AppDataBase.getDatabase(context) // fresh instance recreated
    } catch (_: Exception) {}
}
