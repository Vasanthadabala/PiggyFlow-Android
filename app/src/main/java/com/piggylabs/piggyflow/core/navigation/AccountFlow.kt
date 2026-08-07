package com.piggylabs.piggyflow.core.navigation

import android.content.Context
import com.piggylabs.piggyflow.core.common.Constants
import com.piggylabs.piggyflow.core.di.preferencesManager

fun getAccountType(context: Context): String =
    preferencesManager(context).snapshotBlocking().accountType

fun getPrimaryRoute(context: Context): String {
    return if (getAccountType(context) == Constants.AccountTypes.BUSINESS) {
        BusinessHome.route
    } else {
        Home.route
    }
}
