package com.piggylabs.piggyflow.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.piggylabs.piggyflow.navigation.Home

fun firebaseAuthWithGoogle(
    idToken: String,
    context: Context,
    onSuccess: (uid: String, userName: String?) -> Unit,
    onFailure: (String) -> Unit

) {
    val editor = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE).edit()

    Log.d("FIRESTORE", "firebaseAuthWithGoogle() called")
    Log.d("FIRESTORE", "Received idToken=${idToken.take(20)}...")

    val credential = GoogleAuthProvider.getCredential(idToken, null)

    Log.d("FIRESTORE", "Firebase credential created")

    FirebaseAuth.getInstance()
        .signInWithCredential(credential)
        .addOnSuccessListener { result ->

            Log.d("FIRESTORE", "✅ Firebase Auth SUCCESS")

            val user = result.user
            if (user == null) {
                Log.e("FIRESTORE", "❌ Firebase user is NULL after success")
                return@addOnSuccessListener
            }

            Log.d("FIRESTORE", "Firebase UID=${user.uid}")
            Log.d("FIRESTORE", "Firebase email=${user.email}")
            Log.d("FIRESTORE", "Firebase name=${user.displayName}")
            Log.d("FIRESTORE", "Providers=${user.providerData}")

            val sharedPref = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
            val accountType = sharedPref.getString("account_type", "personal")
            Log.d("FIRESTORE", "Local accountType=$accountType")

            val db = FirebaseFirestore.getInstance()
            val ref = db.collection("users").document(user.uid)

            Log.d("FIRESTORE", "Fetching Firestore document for UID=${user.uid}")

            ref.get()
                .addOnSuccessListener { doc ->

                    Log.d("FIRESTORE", "Firestore doc exists=${doc.exists()}")

                    if (!doc.exists()) {

                        Log.d("FIRESTORE", "Creating new Firestore user document")

                        val data = mapOf(
                            "uid" to user.uid,
                            "email" to user.email,
                            "userName" to (user.displayName ?: "User"),
                            "accountType" to accountType,
                            "createdAt" to System.currentTimeMillis()
                        )

                        ref.set(data)
                            .addOnSuccessListener {
                                Log.d("FIRESTORE", "✅ Firestore user CREATED")

                                editor.putBoolean("is_logged_in", true)
                                editor.putString("uid", user.uid)
                                editor.putString("userName", user.displayName ?: "User")
                                editor.apply()

                                onSuccess(user.uid, user.displayName)
                            }
                            .addOnFailureListener { e ->
                                Log.e("FIRESTORE", "❌ Firestore create FAILED", e)
                                onFailure("Failed to create user")
                            }

                    } else {

                        val accountTypeFromDb = doc.getString("accountType")
                        Log.d("FIRESTORE", "Firestore accountType=$accountTypeFromDb")

                        if (accountTypeFromDb == null) {
                            Log.d("FIRESTORE", "Updating missing accountType in Firestore")
                            ref.update("accountType", accountType)
                        }

                        if (accountTypeFromDb == null || accountTypeFromDb == accountType) {
                            Log.d("FIRESTORE", "✅ Account type matched, login allowed")
                            editor.putBoolean("is_logged_in", true)
                            editor.putString("uid", user.uid)
                            editor.putString("userName", user.displayName ?: "User")
                            editor.apply()

                            onSuccess(user.uid, user.displayName)
                        } else {
                            Log.e("FIRESTORE", "❌ Account type mismatch, login blocked")
                            Toast.makeText(context, "Wrong Account Type", Toast.LENGTH_LONG).show()
                            FirebaseAuth.getInstance().signOut()
                            onFailure("Wrong Account Type")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FIRESTORE", "❌ Firestore GET failed", e)
                    onFailure("Failed to fetch profile")
                }
        }
        .addOnFailureListener { e ->
            Log.e("FIRESTORE", "❌ Firebase Auth FAILED", e)
            Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
            onFailure(e.message ?: "Google sign-in failed")
        }
}