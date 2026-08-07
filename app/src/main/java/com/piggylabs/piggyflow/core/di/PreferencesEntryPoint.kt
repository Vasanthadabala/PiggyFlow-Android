package com.piggylabs.piggyflow.core.di

import android.content.Context
import com.piggylabs.piggyflow.core.datastore.PreferencesManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Reaches [PreferencesManager] from composables and top-level helpers that Hilt does not
 * construct. Prefer constructor injection in ViewModels; this is for the auth, settings
 * and navigation helpers that are still plain functions.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface PreferencesEntryPoint {
    fun preferencesManager(): PreferencesManager
}

fun preferencesManager(context: Context): PreferencesManager =
    EntryPointAccessors
        .fromApplication(context.applicationContext, PreferencesEntryPoint::class.java)
        .preferencesManager()
