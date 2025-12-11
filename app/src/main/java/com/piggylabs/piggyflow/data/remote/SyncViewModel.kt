package com.piggylabs.piggyflow.data.remote

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.piggylabs.piggyflow.auth.GoogleAuthManager
import com.piggylabs.piggyflow.data.local.db.AppEvents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}

data class BackupState(
    val exists: Boolean = false,
    val size: String = "",
    val lastModified: String = ""
)

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    private val tag = "SyncViewModel"

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _backupState = MutableStateFlow(BackupState())
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private var driveService: DriveServiceHelper? = null
    private var currentAccount: GoogleSignInAccount? = null

    // Callback to close database before operations
    var onDatabaseClose: (suspend () -> Unit)? = null
    var onDatabaseReopen: (suspend () -> Unit)? = null

    init {
        checkSignInStatus()
    }

    private fun checkSignInStatus() {
        val account = GoogleAuthManager.getSignedInAccount(getApplication())
        _isSignedIn.value = account != null

        if (account != null) {
            setAccount(account)
        }
    }

    fun setAccount(account: GoogleSignInAccount) {
        currentAccount = account
        val email = account.email

        if (email != null) {
            driveService = DriveServiceHelper(getApplication(), email)
            _isSignedIn.value = true

            Log.d(tag, "Account set: $email")

            // Load backup info
            loadBackupInfo()
        } else {
            Log.e(tag, "Account email is null")
            _isSignedIn.value = false
        }
    }

    fun loadBackupInfo() {
        val service = driveService ?: return

        viewModelScope.launch {
            try {
                service.getBackupInfo()
                    .onSuccess { info ->
                        if (info != null) {
                            _backupState.value = BackupState(
                                exists = true,
                                size = formatFileSize(info.size),
                                lastModified = formatDate(info.modifiedTime)
                            )
                        } else {
                            _backupState.value = BackupState(exists = false)
                        }
                    }
                    .onFailure {
                        _backupState.value = BackupState(exists = false)
                    }
            } catch (e: Exception) {
                Log.e(tag, "Error loading backup info", e)
            }
        }
    }

    fun backup() {
        val service = driveService
        if (service == null) {
            _syncState.value = SyncState.Error("Not signed in. Please sign in first.")
            return
        }

        _syncState.value = SyncState.Loading

        viewModelScope.launch {
            try {
                // Close database before backup
                onDatabaseClose?.invoke()

                service.uploadDatabase(getApplication())
                    .onSuccess { fileId ->
                        _syncState.value = SyncState.Success("Backup completed successfully!")
                        Log.d(tag, "Backup completed: $fileId")

                        // Reload backup info
                        loadBackupInfo()

                        // Reopen database (recreate Room instance)
                        onDatabaseReopen?.invoke()

                        //Emit global reload event so other ViewModels refresh
                        AppEvents.tryEmitDbRecreated()
                    }
                    .onFailure { exception ->
                        _syncState.value = SyncState.Error("Backup failed: ${exception.message}")
                        Log.e(tag, "Backup failed", exception)

                        // Reopen database even on failure
                        onDatabaseReopen?.invoke()
                    }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error("Backup error: ${e.message}")
                Log.e(tag, "Backup error", e)

                // Reopen database
                onDatabaseReopen?.invoke()
            }
        }
    }

    fun restore() {
        val service = driveService
        if (service == null) {
            _syncState.value = SyncState.Error("Not signed in. Please sign in first.")
            return
        }

        _syncState.value = SyncState.Loading

        viewModelScope.launch {
            try {
                // Close database before restore
                onDatabaseClose?.invoke()

                //Download & replace DB file
                service.downloadDatabase(getApplication())
                    .onSuccess {

                        // Reopen database (recreate Room instance)
                        onDatabaseReopen?.invoke()

                        //Emit global reload event so other ViewModels refresh
                        AppEvents.tryEmitDbRecreated()

                        //Notify UI
                        _syncState.value = SyncState.Success("Restore completed!")
                        Log.d(tag, "Restore completed")

                        // Note: Don't reopen database here - app needs to restart
                    }
                    .onFailure { exception ->
                        _syncState.value = SyncState.Error("Restore failed: ${exception.message}")
                        Log.e(tag, "Restore failed", exception)

                        // Reopen database on failure
                        onDatabaseReopen?.invoke()
                    }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error("Restore error: ${e.message}")
                Log.e(tag, "Restore error", e)

                // Reopen database
                onDatabaseReopen?.invoke()
            }
        }
    }

    fun deleteBackup() {
        val service = driveService
        if (service == null) {
            _syncState.value = SyncState.Error("Not signed in. Please sign in first.")
            return
        }

        _syncState.value = SyncState.Loading

        viewModelScope.launch {
            try {
                service.deleteBackup()
                    .onSuccess {
                        _syncState.value = SyncState.Success("Backup deleted successfully!")
                        Log.d(tag, "Backup deleted")

                        // Update backup state
                        loadBackupInfo()
                    }
                    .onFailure { exception ->
                        _syncState.value = SyncState.Error("Delete failed: ${exception.message}")
                        Log.e(tag, "Delete failed", exception)
                    }
            } catch (e: Exception) {
                _syncState.value = SyncState.Error("Delete error: ${e.message}")
                Log.e(tag, "Delete error", e)
            }
        }
    }

    fun signOut() {
        GoogleAuthManager.signOut(getApplication()) {
            _isSignedIn.value = false
            driveService = null
            currentAccount = null
            _backupState.value = BackupState(exists = false)
            Log.d(tag, "Signed out")
        }
    }

    fun resetState() {
        _syncState.value = SyncState.Idle
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onCleared() {
        super.onCleared()
        driveService = null
        currentAccount = null
    }
}
