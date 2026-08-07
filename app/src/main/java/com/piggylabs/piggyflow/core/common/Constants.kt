package com.piggylabs.piggyflow.core.common

/**
 * Values that were previously repeated as string literals across screens. Keeping them
 * here means a key is spelled one way only.
 */
object Constants {

    /**
     * The old SharedPreferences file. Every preference lived here; DataStore migrates it
     * on first access, so the name must not change.
     */
    const val LEGACY_PREFS_NAME = "MY_PRE"

    /** DataStore file backing [com.piggylabs.piggyflow.core.datastore.PreferencesManager]. */
    const val DATASTORE_NAME = "piggyflow_preferences"

    object PrefKeys {
        const val IS_LOGGED_IN = "is_logged_in"
        const val UID = "uid"
        const val USER_NAME = "userName"
        const val ACCOUNT_TYPE = "account_type"
        const val LAST_SYNCED_AT = "last_synced_at"
        const val ACTIVE_DATA_UID = "active_data_uid"
        const val ACTIVE_DATA_ACCOUNT_TYPE = "active_data_account_type"

        /** Spelling kept from the SharedPreferences era so migrated values still resolve. */
        const val THEME_MODE = "app_theme_mode"
        const val CLEARED_TRACKER_NOTIFICATIONS = "cleared_tracker_notifications"

        /** Which Firebase uid is signed in for each account type, tracked separately. */
        const val PERSONAL_SIGNED_IN_UID = "personal_signed_in_uid"
        const val BUSINESS_SIGNED_IN_UID = "business_signed_in_uid"
    }

    object Firestore {
        const val USERS = "users"
        const val MODES = "modes"
    }

    object AccountTypes {
        const val PERSONAL = "personal"
        const val BUSINESS = "business"
    }

    /** Default value used whenever a display name is missing. */
    const val DEFAULT_USER_NAME = "User"
}
