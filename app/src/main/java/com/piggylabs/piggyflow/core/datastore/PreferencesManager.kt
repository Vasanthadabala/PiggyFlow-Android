package com.piggylabs.piggyflow.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.piggylabs.piggyflow.core.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * All persisted user preferences, in one place.
 *
 * Replaces the `getSharedPreferences("MY_PRE", ...)` calls that used to be scattered
 * across auth, settings, onboarding, navigation and theming. The existing
 * SharedPreferences file is migrated on first access, so users stay signed in and keep
 * their account type and theme across the upgrade.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, Constants.LEGACY_PREFS_NAME))
    }
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store = context.dataStore

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey(Constants.PrefKeys.IS_LOGGED_IN)
        val UID = stringPreferencesKey(Constants.PrefKeys.UID)
        val USER_NAME = stringPreferencesKey(Constants.PrefKeys.USER_NAME)
        val ACCOUNT_TYPE = stringPreferencesKey(Constants.PrefKeys.ACCOUNT_TYPE)
        val THEME_MODE = stringPreferencesKey(Constants.PrefKeys.THEME_MODE)
        val LAST_SYNCED_AT = longPreferencesKey(Constants.PrefKeys.LAST_SYNCED_AT)
        val ACTIVE_DATA_UID = stringPreferencesKey(Constants.PrefKeys.ACTIVE_DATA_UID)
        val ACTIVE_DATA_ACCOUNT_TYPE =
            stringPreferencesKey(Constants.PrefKeys.ACTIVE_DATA_ACCOUNT_TYPE)
        val CLEARED_TRACKER_NOTIFICATIONS =
            stringSetPreferencesKey(Constants.PrefKeys.CLEARED_TRACKER_NOTIFICATIONS)
        val PERSONAL_SIGNED_IN_UID =
            stringPreferencesKey(Constants.PrefKeys.PERSONAL_SIGNED_IN_UID)
        val BUSINESS_SIGNED_IN_UID =
            stringPreferencesKey(Constants.PrefKeys.BUSINESS_SIGNED_IN_UID)

        /** Account type decides which signed-in uid slot applies. */
        fun signedInUidFor(accountType: String) =
            if (accountType.equals(Constants.AccountTypes.BUSINESS, ignoreCase = true)) {
                BUSINESS_SIGNED_IN_UID
            } else {
                PERSONAL_SIGNED_IN_UID
            }
    }

    /** Writes issued from callbacks, matching SharedPreferences `apply()` semantics. */
    private val writeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** A corrupt store should read as empty rather than crash the app on launch. */
    private val preferences: Flow<Preferences> = store.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    val isLoggedIn: Flow<Boolean> = preferences.map { it[Keys.IS_LOGGED_IN] ?: false }
    val uid: Flow<String?> = preferences.map { it[Keys.UID] }
    val userName: Flow<String> = preferences.map {
        it[Keys.USER_NAME] ?: Constants.DEFAULT_USER_NAME
    }
    val accountType: Flow<String> = preferences.map {
        it[Keys.ACCOUNT_TYPE] ?: Constants.AccountTypes.PERSONAL
    }
    val themeMode: Flow<String?> = preferences.map { it[Keys.THEME_MODE] }
    val lastSyncedAt: Flow<Long> = preferences.map { it[Keys.LAST_SYNCED_AT] ?: 0L }
    val activeDataUid: Flow<String?> = preferences.map { it[Keys.ACTIVE_DATA_UID] }
    val activeDataAccountType: Flow<String?> =
        preferences.map { it[Keys.ACTIVE_DATA_ACCOUNT_TYPE] }
    val clearedTrackerNotifications: Flow<Set<String>> =
        preferences.map { it[Keys.CLEARED_TRACKER_NOTIFICATIONS] ?: emptySet() }

    suspend fun setLoggedIn(value: Boolean) = edit { it[Keys.IS_LOGGED_IN] = value }

    suspend fun setUid(value: String) = edit { it[Keys.UID] = value }

    suspend fun setUserName(value: String) = edit { it[Keys.USER_NAME] = value }

    suspend fun setAccountType(value: String) = edit { it[Keys.ACCOUNT_TYPE] = value }

    suspend fun setThemeMode(value: String) = edit { it[Keys.THEME_MODE] = value }

    suspend fun setLastSyncedAt(value: Long) = edit { it[Keys.LAST_SYNCED_AT] = value }

    /** Records which account's data currently populates the local database. */
    suspend fun setActiveData(uid: String, accountType: String) = edit {
        it[Keys.ACTIVE_DATA_UID] = uid
        it[Keys.ACTIVE_DATA_ACCOUNT_TYPE] = accountType
    }

    /** Marks the session signed in and stores who it belongs to, in one write. */
    suspend fun setSignedIn(uid: String, userName: String) = edit {
        it[Keys.IS_LOGGED_IN] = true
        it[Keys.UID] = uid
        it[Keys.USER_NAME] = userName
    }

    suspend fun clearSession() = edit {
        it.remove(Keys.IS_LOGGED_IN)
        it.remove(Keys.UID)
        it.remove(Keys.USER_NAME)
    }

    suspend fun setClearedTrackerNotifications(keys: Set<String>) = edit {
        it[Keys.CLEARED_TRACKER_NOTIFICATIONS] = keys
    }

    private suspend fun edit(block: (MutablePreferences) -> Unit) {
        store.edit(block)
    }

    // ---- fire-and-forget variants -------------------------------------------------
    // Firebase callbacks and Compose click handlers are not coroutine scopes. These
    // return immediately and persist in the background, exactly as `apply()` did.

    fun setSignedInAsync(uid: String, userName: String) {
        writeScope.launch { setSignedIn(uid, userName) }
    }

    fun setLoggedInAsync(value: Boolean) {
        writeScope.launch { setLoggedIn(value) }
    }

    fun setUserNameAsync(value: String) {
        writeScope.launch { setUserName(value) }
    }

    fun setAccountTypeAsync(value: String) {
        writeScope.launch { setAccountType(value) }
    }

    fun setLastSyncedAtAsync(value: Long) {
        writeScope.launch { setLastSyncedAt(value) }
    }

    fun setActiveDataAsync(uid: String, accountType: String) {
        writeScope.launch { setActiveData(uid, accountType) }
    }

    fun setThemeModeAsync(value: String) {
        writeScope.launch { setThemeMode(value) }
    }

    fun setClearedTrackerNotificationsAsync(keys: Set<String>) {
        writeScope.launch { setClearedTrackerNotifications(keys) }
    }

    fun setUidAsync(value: String) {
        writeScope.launch { setUid(value) }
    }

    fun removeUidAsync() {
        writeScope.launch { edit { it.remove(Keys.UID) } }
    }

    fun clearClearedTrackerNotificationsAsync() {
        writeScope.launch { edit { it.remove(Keys.CLEARED_TRACKER_NOTIFICATIONS) } }
    }

    /** Remembers that [uid] is the account signed in while in [accountType] mode. */
    fun setSignedInUidForAccountTypeAsync(accountType: String, uid: String) {
        writeScope.launch { edit { it[Keys.signedInUidFor(accountType)] = uid } }
    }

    /** True when [firebaseUid] is the account already signed in for [accountType]. */
    fun isSignedInForAccountType(accountType: String, firebaseUid: String?): Boolean {
        if (firebaseUid.isNullOrBlank()) return false
        val stored = runBlocking { preferences.first()[Keys.signedInUidFor(accountType)] }
        return !stored.isNullOrBlank() && stored == firebaseUid
    }

    /** Signing out: drops the session uid, both mode slots, and the sync timestamp. */
    fun clearSignInStateAsync() {
        writeScope.launch {
            edit {
                it.remove(Keys.UID)
                it.remove(Keys.PERSONAL_SIGNED_IN_UID)
                it.remove(Keys.BUSINESS_SIGNED_IN_UID)
                it.remove(Keys.LAST_SYNCED_AT)
            }
        }
    }

    /**
     * Synchronous snapshot for the few callers that must answer before the first frame -
     * the navigation start destination and the initial theme. Everything else should
     * collect the flows above.
     */
    fun snapshotBlocking(): Snapshot = runBlocking {
        val prefs = preferences.first()
        Snapshot(
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            uid = prefs[Keys.UID].orEmpty(),
            userName = prefs[Keys.USER_NAME] ?: Constants.DEFAULT_USER_NAME,
            accountType = prefs[Keys.ACCOUNT_TYPE] ?: Constants.AccountTypes.PERSONAL,
            themeMode = prefs[Keys.THEME_MODE],
            lastSyncedAt = prefs[Keys.LAST_SYNCED_AT] ?: 0L,
            activeDataUid = prefs[Keys.ACTIVE_DATA_UID],
            activeDataAccountType = prefs[Keys.ACTIVE_DATA_ACCOUNT_TYPE],
            clearedTrackerNotifications = prefs[Keys.CLEARED_TRACKER_NOTIFICATIONS] ?: emptySet()
        )
    }

    data class Snapshot(
        val isLoggedIn: Boolean,
        val uid: String,
        val userName: String,
        val accountType: String,
        val themeMode: String?,
        val lastSyncedAt: Long,
        val activeDataUid: String?,
        val activeDataAccountType: String?,
        val clearedTrackerNotifications: Set<String>
    )
}
