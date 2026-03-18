package com.piggylabs.piggyflow.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

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
            val accountType = sharedPref.getString("account_type", "personal")?.lowercase() ?: "personal"
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
                            "userNames" to mapOf(accountType to (user.displayName ?: "User")),
                            "accountType" to accountType,
                            "accountTypes" to listOf(accountType),
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
                        val existingTypes = (doc.get("accountTypes") as? List<*>)
                            ?.mapNotNull { it as? String }
                            ?.map { it.lowercase() }
                            ?.toMutableSet()
                            ?: mutableSetOf()
                        doc.getString("accountType")?.lowercase()?.let(existingTypes::add)
                        existingTypes.add(accountType)

                        val typedNames = doc.get("userNames") as? Map<*, *>
                        val scopedName = typedNames?.get(accountType) as? String
                        val resolvedName = scopedName
                            ?: user.displayName
                            ?: doc.getString("userName")
                            ?: "User"

                        val updates = mapOf(
                            "accountType" to accountType,
                            "accountTypes" to existingTypes.toList(),
                            "userNames.$accountType" to resolvedName,
                            "updatedAt" to System.currentTimeMillis()
                        )

                        ref.set(updates, SetOptions.merge())
                            .addOnSuccessListener {
                                editor.putBoolean("is_logged_in", true)
                                editor.putString("uid", user.uid)
                                editor.putString("userName", resolvedName)
                                editor.apply()

                                onSuccess(user.uid, resolvedName)
                            }
                            .addOnFailureListener { e ->
                                Log.e("FIRESTORE", "❌ Firestore account-type update failed", e)
                                onFailure("Failed to update account type")
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
