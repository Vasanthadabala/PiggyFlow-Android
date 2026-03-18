package com.piggylabs.piggyflow.navigation

import android.content.Context

fun getAccountType(context: Context): String {
    return context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        .getString("account_type", "personal")
        ?: "personal"
}

fun getPrimaryRoute(context: Context): String {
    return if (getAccountType(context) == "business") {
        BusinessHome.route
    } else {
        Home.route
    }
}
