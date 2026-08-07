package com.piggylabs.piggyflow.core.di

import android.content.Context
import com.piggylabs.piggyflow.core.domain.repository.BackupRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Escape hatch for the backup/sync helpers, which are plain top-level functions rather
 * than Hilt-managed classes. Prefer constructor injection; this exists so those call
 * sites can reach the domain layer without going back to Room directly.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface BackupEntryPoint {
    fun backupRepository(): BackupRepository
}

private fun entryPoint(context: Context): BackupEntryPoint =
    EntryPointAccessors.fromApplication(context.applicationContext, BackupEntryPoint::class.java)

fun backupRepository(context: Context): BackupRepository = entryPoint(context).backupRepository()
