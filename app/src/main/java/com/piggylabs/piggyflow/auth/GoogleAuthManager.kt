package com.piggylabs.piggyflow.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

object GoogleAuthManager {

    // TODO: Replace with your actual Web Client ID from Google Cloud Console
    private const val WEB_CLIENT_ID = "668859011738-dgl1of7bbuc210hj8j4jt07j2f686ts0.apps.googleusercontent.com"
    private const val DRIVE_APPDATA_SCOPE =
        "https://www.googleapis.com/auth/drive.appdata"

    fun getSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
//            .requestIdToken(WEB_CLIENT_ID)
//            .requestServerAuthCode(WEB_CLIENT_ID, true)
            .requestScopes(Scope(DRIVE_APPDATA_SCOPE))
            .build()
    }

    fun getSignInClient(context: Context): GoogleSignInClient {
        return GoogleSignIn.getClient(context, getSignInOptions())
    }

    fun getSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun isSignedIn(context: Context): Boolean {
        val account = getSignedInAccount(context)
        return account != null && !account.isExpired
    }

    fun signOut(context: Context, onComplete: () -> Unit) {
        getSignInClient(context).signOut().addOnCompleteListener {
            onComplete()
        }
    }

    fun revokeAccess(context: Context, onComplete: () -> Unit) {
        getSignInClient(context).revokeAccess().addOnCompleteListener {
            onComplete()
        }
    }
}

