package com.piggylabs.piggyflow.ui.screens.common.auth

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.piggylabs.piggyflow.R
import com.piggylabs.piggyflow.auth.firebaseAuthWithGoogle
import com.piggylabs.piggyflow.data.local.db.AppDataBase
import com.piggylabs.piggyflow.data.local.entity.BusinessEntryEntity
import com.piggylabs.piggyflow.data.local.entity.BusinessPartyEntity
import com.piggylabs.piggyflow.data.local.entity.ExpenseEntity
import com.piggylabs.piggyflow.data.local.entity.IncomeEntity
import com.piggylabs.piggyflow.data.local.entity.SubscriptionEntity
import com.piggylabs.piggyflow.data.local.entity.UserCategoryEntity
import com.piggylabs.piggyflow.navigation.SignIn
import com.piggylabs.piggyflow.navigation.SignUp
import com.piggylabs.piggyflow.navigation.getPrimaryRoute
import com.piggylabs.piggyflow.navigation.components.TopBar
import com.piggylabs.piggyflow.ui.theme.appColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val LOGIN_OPTIONS_TAG = "LoginOptionsFlow"
private const val ACTIVE_DATA_UID_KEY = "active_data_uid"
private const val ACTIVE_DATA_ACCOUNT_TYPE_KEY = "active_data_account_type"

@ExperimentalMaterial3Api
@Composable
fun LoginOptionsScreen(navController: NavHostController){
    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            LoginOptionsScreenComponent(navController = navController)
        }

    }
}

@Composable
fun LoginOptionsScreenComponent(navController: NavHostController) {
    val context = LocalContext.current

    //sharedPreferences
    val sharedPref = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    val accountType = sharedPref.getString("account_type", "personal")

    val imageRes = when (accountType) {
        "personal" -> R.drawable.account_type1
        "business" -> R.drawable.account_type2
        else -> R.drawable.account_type1
    }

    /* ---------------- SKIP BUTTON ANIMATION ---------------- */

    val infiniteTransition = rememberInfiniteTransition(label = "skip_anim")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 700,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow"
    )

    /* ---------------- Google Sign In---------------- */
    val webClientId = context.getString(R.string.default_web_client_id)

    val googleSignInClient = remember {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(webClientId)
                .build()
        )
    }

    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }

    val runRestoreThenNavigateHome: (String, String) -> Unit = { uid, mode ->
        Log.d(LOGIN_OPTIONS_TAG, "Firebase auth success uid=$uid mode=$mode; starting restoreOrSyncAfterLogin")
        restoreOrSyncAfterLogin(
            context = context,
            uid = uid,
            setSyncing = { isSyncing = it },
            onMessage = { message ->
                Log.d(LOGIN_OPTIONS_TAG, "Restore flow finished with message: $message")
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                navController.navigate(getPrimaryRoute(context)) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        )
    }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        Log.d(LOGIN_OPTIONS_TAG, "Google launcher completed: resultCode=${result.resultCode}, mode=login")
        Log.d("FIRESTORE", "Google launcher resultCode=${result.resultCode}")
        isGoogleSigningIn = false

        if (result.resultCode == Activity.RESULT_OK) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)

                Log.d("FIRESTORE", "Google account obtained")
                Log.d("FIRESTORE", "Google email=${account.email}")
                Log.d("FIRESTORE", "Google id=${account.id}")
                Log.d("FIRESTORE", "Google idToken=${account.idToken}")

                val idToken = account.idToken

                if (idToken.isNullOrEmpty()) {
                    Log.e(LOGIN_OPTIONS_TAG, "Google sign-in returned empty ID token")
                    Log.e("FIRESTORE", "❌ Google ID TOKEN is NULL")
                    Toast.makeText(context, "Google token missing", Toast.LENGTH_SHORT).show()
                    return@rememberLauncherForActivityResult
                }

                Log.d(LOGIN_OPTIONS_TAG, "Firebase auth request started (mode=login)")
                Log.d("FIRESTORE", "✅ Google ID TOKEN received, calling Firebase")

                firebaseAuthWithGoogle(
                    idToken = idToken,
                    context = context,
                    onSuccess = { uid, name ->
                        runRestoreThenNavigateHome(uid, "login")
                    },
                    onFailure = { error ->
                        Log.e(LOGIN_OPTIONS_TAG, "Firebase auth failed (mode=login): $error")
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )

            } catch (e: ApiException) {
                Log.e(
                    "FIRESTORE",
                    "❌ GoogleSignIn ApiException status=${e.statusCode} msg=${e.message}",
                    e
                )
                val message = when (e.statusCode) {
                    10 -> "Google Sign-In misconfigured (OAuth SHA / client). Please update Firebase config."
                    12501 -> "Google Sign-In cancelled."
                    else -> "Google Sign-In failed (code ${e.statusCode})"
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("FIRESTORE", "❌ Unexpected GoogleSignIn exception", e)
                Toast.makeText(context, "Unexpected Google Sign-In error", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.e("FIRESTORE", "❌ Google Sign-In cancelled by user")
            Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    val restoreGoogleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(LOGIN_OPTIONS_TAG, "Google launcher completed: resultCode=${result.resultCode}, mode=restore")
        isGoogleSigningIn = false

        if (result.resultCode != Activity.RESULT_OK) {
            Log.e(LOGIN_OPTIONS_TAG, "Google Sign-In cancelled during restore flow")
            Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken.isNullOrEmpty()) {
                Log.e(LOGIN_OPTIONS_TAG, "Google sign-in returned empty ID token (restore mode)")
                Toast.makeText(context, "Google token missing", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }

            Log.d(LOGIN_OPTIONS_TAG, "Firebase auth request started (mode=restore)")
            firebaseAuthWithGoogle(
                idToken = idToken,
                context = context,
                onSuccess = { uid, _ ->
                    runRestoreThenNavigateHome(uid, "restore")
                },
                onFailure = { error ->
                    Log.e(LOGIN_OPTIONS_TAG, "Firebase auth failed (mode=restore): $error")
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        } catch (e: ApiException) {
            Log.e(LOGIN_OPTIONS_TAG, "GoogleSignIn ApiException in restore flow status=${e.statusCode} msg=${e.message}", e)
            Toast.makeText(context, "Google Sign-In failed (code ${e.statusCode})", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(LOGIN_OPTIONS_TAG, "Unexpected GoogleSignIn exception in restore flow", e)
            Toast.makeText(context, "Unexpected Google Sign-In error", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
        )

        AuthCard(
            onLogin = {
                Log.d(LOGIN_OPTIONS_TAG, "Login button tapped")
                navController.navigate(SignIn.route)
            },
            onRegister = {
                Log.d(LOGIN_OPTIONS_TAG, "Register button tapped")
                navController.navigate(SignUp.route)
            },
            onGoogleLogin = {
                if (isGoogleSigningIn) return@AuthCard
                isGoogleSigningIn = true
                Log.d(LOGIN_OPTIONS_TAG, "Continue with Google tapped; launching Google sign-in")
                googleSignInClient.signOut().addOnCompleteListener {
                    googleLauncher.launch(googleSignInClient.signInIntent)
                }.addOnFailureListener {
                    Log.e(LOGIN_OPTIONS_TAG, "Pre-login Google signOut failed; continuing with sign-in", it)
                    Log.e("FIRESTORE", "Failed to clear old Google session", it)
                    googleLauncher.launch(googleSignInClient.signInIntent)
                }
            },
            onRestoreBackup = {
                if (isGoogleSigningIn || isSyncing) {
                    Log.w(LOGIN_OPTIONS_TAG, "Restore Backup tap ignored: isGoogleSigningIn=$isGoogleSigningIn isSyncing=$isSyncing")
                    return@AuthCard
                }
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid
                Log.d(LOGIN_OPTIONS_TAG, "Restore Backup tapped from LoginOptions. existingAuthUid=$currentUid. Starting Google re-auth restore flow")
                isGoogleSigningIn = true
                Log.d(LOGIN_OPTIONS_TAG, "Restore Backup tapped; launching Google sign-in for restore")
                googleSignInClient.signOut().addOnCompleteListener {
                    restoreGoogleLauncher.launch(googleSignInClient.signInIntent)
                }.addOnFailureListener {
                    Log.e(LOGIN_OPTIONS_TAG, "Pre-restore Google signOut failed; continuing with sign-in", it)
                    Log.e("FIRESTORE", "Failed to clear old Google session", it)
                    restoreGoogleLauncher.launch(googleSignInClient.signInIntent)
                }
            },
            isRestoring = isSyncing
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable {
                    editor.putBoolean("is_logged_in", true)
                    editor.apply()

                    navController.navigate(getPrimaryRoute(context)) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = appColors().green,
                contentColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skip",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowRight,
                    contentDescription = "Skip",
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            translationX = arrowOffset
                        }
                )
            }
        }
    }

}

@Composable
fun AuthCard(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onGoogleLogin: () -> Unit,
    onRestoreBackup: () -> Unit,
    isRestoring: Boolean
) {

    Card(
        shape = RoundedCornerShape(
            topStart = 32.dp,
            bottomStart = 32.dp,
            topEnd = 32.dp,
            bottomEnd = 32.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Welcome",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Login Button
            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors().green,
                    contentColor = Color.White
                ),
            ) {
                Text(
                    text = "Login",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            // Register Button
            OutlinedButton(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Register",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = appColors().text,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            // Google Button (Spotify style)
            OutlinedButton(
                onClick = onGoogleLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = appColors().text,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            if(isRestoring) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Restoring backup...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = appColors().text,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun syncLocalDataToFirebase(
    context: Context,
    uid: String,
    setSyncing: (Boolean) -> Unit,
    onMessage: (String) -> Unit
) {
    Log.d(LOGIN_OPTIONS_TAG, "syncLocalDataToFirebase started for uid=$uid")
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { setSyncing(true) }
    val db = AppDataBase.getDatabase(context)
    val firestore = FirebaseFirestore.getInstance()
    val userDoc = firestore.collection("users").document(uid)
    val accountType = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        .getString("account_type", "personal")
        ?.lowercase()
        ?: "personal"
    val modeDoc = userDoc.collection("modes").document(accountType)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val categories = db.userCategoryDao().getAllCategories().first()
            val expenses = db.expenseDao().getAllExpenses().first()
            val incomes = db.incomeDao().getAllIncome().first()
            val subscriptions = db.subscriptionDao().getAllSubscriptions().first()
            val businessParties = db.businessPartyDao().getAllBusinessParties().first()
            val businessEntries = db.businessEntryDao().getAllBusinessEntries().first()
            Log.d(LOGIN_OPTIONS_TAG, "Local data prepared for sync uid=$uid categories=${categories.size} expenses=${expenses.size} incomes=${incomes.size} subscriptions=${subscriptions.size} businessParties=${businessParties.size} businessEntries=${businessEntries.size}")

            val batch = firestore.batch()

            val syncMeta = mapOf(
                "updatedAt" to System.currentTimeMillis(),
                "categoryCount" to categories.size,
                "expenseCount" to expenses.size,
                "incomeCount" to incomes.size,
                "subscriptionCount" to subscriptions.size,
                "businessPartyCount" to businessParties.size,
                "businessEntryCount" to businessEntries.size
            )

            batch.set(
                modeDoc.collection("sync").document("meta"),
                syncMeta,
                SetOptions.merge()
            )

            categories.forEach { category ->
                val categoryData = mapOf(
                    "id" to category.id,
                    "name" to category.name,
                    "emoji" to category.emoji,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(
                    modeDoc.collection("categories").document(category.id.toString()),
                    categoryData,
                    SetOptions.merge()
                )
            }

            expenses.forEach { expense ->
                val expenseData = mapOf(
                    "id" to expense.id,
                    "categoryType" to expense.categoryType,
                    "amount" to expense.amount,
                    "note" to expense.note,
                    "date" to expense.date,
                    "categoryName" to expense.categoryName,
                    "categoryEmoji" to expense.categoryEmoji,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(
                    modeDoc.collection("expenses").document(expense.id.toString()),
                    expenseData,
                    SetOptions.merge()
                )
            }

            incomes.forEach { income ->
                val incomeData = mapOf(
                    "id" to income.id,
                    "categoryType" to income.categoryType,
                    "amount" to income.amount,
                    "note" to income.note,
                    "date" to income.date,
                    "categoryName" to income.categoryName,
                    "categoryEmoji" to income.categoryEmoji,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(
                    modeDoc.collection("incomes").document(income.id.toString()),
                    incomeData,
                    SetOptions.merge()
                )
            }

            subscriptions.forEach { subscription ->
                val subscriptionData = mapOf(
                    "id" to subscription.id,
                    "type" to subscription.type,
                    "name" to subscription.name,
                    "subType" to subscription.subType,
                    "amount" to subscription.amount,
                    "dueDate" to subscription.dueDate,
                    "logoUrl" to subscription.logoUrl,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(
                    modeDoc.collection("subscriptions").document(subscription.id.toString()),
                    subscriptionData,
                    SetOptions.merge()
                )
            }

            businessParties.forEach { party ->
                val partyData = mapOf(
                    "id" to party.id,
                    "name" to party.name,
                    "phone" to party.phone,
                    "address" to party.address,
                    "createdAt" to party.createdAt,
                    "updatedAt" to party.updatedAt
                )
                batch.set(
                    modeDoc.collection("businessParties").document(party.id.toString()),
                    partyData,
                    SetOptions.merge()
                )
            }

            businessEntries.forEach { entry ->
                val entryData = mapOf(
                    "id" to entry.id,
                    "partyId" to entry.partyId,
                    "type" to entry.type,
                    "amount" to entry.amount,
                    "note" to entry.note,
                    "createdAt" to entry.createdAt
                )
                batch.set(
                    modeDoc.collection("businessEntries").document(entry.id.toString()),
                    entryData,
                    SetOptions.merge()
                )
            }

            batch.commit()
                .addOnSuccessListener {
                    Log.d(LOGIN_OPTIONS_TAG, "Sync completed successfully for uid=$uid")
                    context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
                        .edit()
                        .putString(ACTIVE_DATA_UID_KEY, uid)
                        .putString(ACTIVE_DATA_ACCOUNT_TYPE_KEY, accountType)
                        .apply()
                    mainHandler.post {
                        setSyncing(false)
                        onMessage("Sync completed successfully")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(LOGIN_OPTIONS_TAG, "Sync failed for uid=$uid", e)
                    Log.e("FIRESTORE", "Sync failed", e)
                    val errorMessage = if (
                        e is FirebaseFirestoreException &&
                        e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                    ) {
                        "Sync blocked by Firestore rules. Allow users/{uid}/... for signed-in user."
                    } else {
                        e.message ?: "Sync failed"
                    }
                    mainHandler.post {
                        setSyncing(false)
                        onMessage(errorMessage)
                    }
                }
        } catch (e: Exception) {
            Log.e(LOGIN_OPTIONS_TAG, "Sync preparation failed for uid=$uid", e)
            Log.e("FIRESTORE", "Sync preparation failed", e)
            mainHandler.post {
                setSyncing(false)
                onMessage(e.message ?: "Sync failed")
            }
        }
    }
}

internal fun restoreOrSyncAfterLogin(
    context: Context,
    uid: String,
    setSyncing: (Boolean) -> Unit,
    onMessage: (String) -> Unit
) {
    Log.d(LOGIN_OPTIONS_TAG, "restoreOrSyncAfterLogin started for uid=$uid")
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { setSyncing(true) }

    val firestore = FirebaseFirestore.getInstance()
    val userDoc = firestore.collection("users").document(uid)
    val accountType = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        .getString("account_type", "personal")
        ?.lowercase()
        ?: "personal"
    val modeDoc = userDoc.collection("modes").document(accountType)

    prepareLocalDataForAccountSwitch(
        context = context,
        uid = uid,
        accountType = accountType,
        onSuccess = {
            modeDoc.collection("categories").get()
        .addOnSuccessListener { categorySnap ->
            modeDoc.collection("expenses").get()
                .addOnSuccessListener { expenseSnap ->
                    modeDoc.collection("incomes").get()
                        .addOnSuccessListener { incomeSnap ->
                            modeDoc.collection("subscriptions").get()
                                .addOnSuccessListener { subscriptionSnap ->
                                    modeDoc.collection("businessParties").get()
                                        .addOnSuccessListener { businessPartySnap ->
                                            modeDoc.collection("businessEntries").get()
                                                .addOnSuccessListener { businessEntrySnap ->
                            Log.d(LOGIN_OPTIONS_TAG, "Remote backup snapshot uid=$uid categories=${categorySnap.size()} expenses=${expenseSnap.size()} incomes=${incomeSnap.size()} subscriptions=${subscriptionSnap.size()} businessParties=${businessPartySnap.size()} businessEntries=${businessEntrySnap.size()}")
                            val hasBackupData =
                                categorySnap.documents.isNotEmpty() ||
                                    expenseSnap.documents.isNotEmpty() ||
                                    incomeSnap.documents.isNotEmpty() ||
                                    subscriptionSnap.documents.isNotEmpty() ||
                                    businessPartySnap.documents.isNotEmpty() ||
                                    businessEntrySnap.documents.isNotEmpty()

                            if (!hasBackupData) {
                                Log.d(LOGIN_OPTIONS_TAG, "No remote backup found for uid=$uid; falling back to sync")
                                mainHandler.post { setSyncing(false) }
                                syncLocalDataToFirebase(context, uid, setSyncing, onMessage)
                                return@addOnSuccessListener
                            }

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val db = AppDataBase.getDatabase(context)

                                    val categories = categorySnap.documents.map { doc ->
                                        UserCategoryEntity(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            name = doc.getString("name").orEmpty(),
                                            emoji = doc.getString("emoji").orEmpty()
                                        )
                                    }

                                    val expenses = expenseSnap.documents.map { doc ->
                                        ExpenseEntity(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            categoryType = doc.getString("categoryType").orEmpty(),
                                            amount = (doc.getDouble("amount")
                                                ?: doc.getLong("amount")?.toDouble()
                                                ?: 0.0),
                                            note = doc.getString("note").orEmpty(),
                                            date = doc.getString("date").orEmpty(),
                                            categoryName = doc.getString("categoryName").orEmpty(),
                                            categoryEmoji = doc.getString("categoryEmoji").orEmpty()
                                        )
                                    }

                                    val incomes = incomeSnap.documents.map { doc ->
                                        IncomeEntity(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            categoryType = doc.getString("categoryType").orEmpty(),
                                            amount = (doc.getDouble("amount")
                                                ?: doc.getLong("amount")?.toDouble()
                                                ?: 0.0),
                                            note = doc.getString("note").orEmpty(),
                                            date = doc.getString("date").orEmpty(),
                                            categoryName = doc.getString("categoryName").orEmpty(),
                                            categoryEmoji = doc.getString("categoryEmoji").orEmpty()
                                        )
                                    }

                                    val subscriptions = subscriptionSnap.documents.map { doc ->
                                        SubscriptionEntity(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            type = doc.getString("type").orEmpty(),
                                            name = doc.getString("name").orEmpty(),
                                            subType = doc.getString("subType").orEmpty(),
                                            amount = (doc.getDouble("amount") ?: doc.getLong("amount")?.toDouble() ?: 0.0),
                                            dueDate = doc.getString("dueDate").orEmpty(),
                                            logoUrl = doc.getString("logoUrl").orEmpty()
                                        )
                                    }

                                    val businessParties = businessPartySnap.documents.map { doc ->
                                        BusinessPartyEntity(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            name = doc.getString("name").orEmpty(),
                                            phone = doc.getString("phone").orEmpty(),
                                            address = doc.getString("address").orEmpty(),
                                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                                        )
                                    }

                                    val businessEntries = businessEntrySnap.documents.map { doc ->
                                        BusinessEntryEntity(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            partyId = doc.getLong("partyId")?.toInt() ?: 0,
                                            type = doc.getString("type").orEmpty(),
                                            amount = (doc.getDouble("amount") ?: doc.getLong("amount")?.toDouble() ?: 0.0),
                                            note = doc.getString("note").orEmpty(),
                                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                        )
                                    }

                                    val invalidCategoryIds = categories.count { it.id == 0 }
                                    val invalidExpenseIds = expenses.count { it.id == 0 }
                                    val invalidIncomeIds = incomes.count { it.id == 0 }
                                    Log.d(
                                        LOGIN_OPTIONS_TAG,
                                        "Parsed remote backup uid=$uid categories=${categories.size} expenses=${expenses.size} incomes=${incomes.size} invalidIds(c/e/i)=($invalidCategoryIds/$invalidExpenseIds/$invalidIncomeIds)"
                                    )

                                    val localCategories = db.userCategoryDao().getAllCategories().first()
                                    val localExpenses = db.expenseDao().getAllExpenses().first()
                                    val localIncomes = db.incomeDao().getAllIncome().first()
                                    val localSubscriptions = db.subscriptionDao().getAllSubscriptions().first()
                                    val localBusinessParties = db.businessPartyDao().getAllBusinessParties().first()
                                    val localBusinessEntries = db.businessEntryDao().getAllBusinessEntries().first()
                                    Log.d(
                                        LOGIN_OPTIONS_TAG,
                                        "Local snapshot before restore uid=$uid categories=${localCategories.size} expenses=${localExpenses.size} incomes=${localIncomes.size}"
                                    )

                                    val localCategoryIds = localCategories.map { it.id }.toSet()
                                    val localExpenseIds = localExpenses.map { it.id }.toSet()
                                    val localIncomeIds = localIncomes.map { it.id }.toSet()
                                    val localSubscriptionIds = localSubscriptions.map { it.id }.toSet()
                                    val localBusinessPartyIds = localBusinessParties.map { it.id }.toSet()
                                    val localBusinessEntryIds = localBusinessEntries.map { it.id }.toSet()

                                    val categoriesToInsert = categories.filter { it.id !in localCategoryIds }
                                    val expensesToInsert = expenses.filter { it.id !in localExpenseIds }
                                    val incomesToInsert = incomes.filter { it.id !in localIncomeIds }
                                    val subscriptionsToInsert = subscriptions.filter { it.id !in localSubscriptionIds }
                                    val businessPartiesToInsert = businessParties.filter { it.id !in localBusinessPartyIds }
                                    val businessEntriesToInsert = businessEntries.filter { it.id !in localBusinessEntryIds }
                                    Log.d(
                                        LOGIN_OPTIONS_TAG,
                                        "Restore diff uid=$uid newCategories=${categoriesToInsert.size}/${categories.size} newExpenses=${expensesToInsert.size}/${expenses.size} newIncomes=${incomesToInsert.size}/${incomes.size}"
                                    )

                                    if (categoriesToInsert.isNotEmpty()) {
                                        db.userCategoryDao().insertAllCategories(categoriesToInsert)
                                    }
                                    if (expensesToInsert.isNotEmpty()) {
                                        db.expenseDao().insertAllExpenses(expensesToInsert)
                                    }
                                    if (incomesToInsert.isNotEmpty()) {
                                        db.incomeDao().insertAllIncome(incomesToInsert)
                                    }
                                    subscriptionsToInsert.forEach { db.subscriptionDao().insertSubscription(it) }
                                    businessPartiesToInsert.forEach { db.businessPartyDao().insertBusinessParty(it) }
                                    businessEntriesToInsert.forEach { db.businessEntryDao().insertBusinessEntry(it) }
                                    if (
                                        categories.isNotEmpty() || expenses.isNotEmpty() || incomes.isNotEmpty()
                                    ) {
                                        if (
                                            categoriesToInsert.isEmpty() &&
                                            expensesToInsert.isEmpty() &&
                                            incomesToInsert.isEmpty()
                                        ) {
                                            Log.w(
                                                LOGIN_OPTIONS_TAG,
                                                "Restore inserted 0 rows for uid=$uid because all remote items already exist locally (ID-based dedupe)"
                                            )
                                        }
                                    }
                                    Log.d(LOGIN_OPTIONS_TAG, "Backup merge completed uid=$uid insertedCategories=${categoriesToInsert.size} insertedExpenses=${expensesToInsert.size} insertedIncomes=${incomesToInsert.size}")
                                    context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
                                        .edit()
                                        .putString(ACTIVE_DATA_UID_KEY, uid)
                                        .putString(ACTIVE_DATA_ACCOUNT_TYPE_KEY, accountType)
                                        .apply()

                                    mainHandler.post {
                                        setSyncing(false)
                                        onMessage(
                                            "Backup merged: +${categoriesToInsert.size} categories, +${expensesToInsert.size} expenses, +${incomesToInsert.size} incomes, +${subscriptionsToInsert.size} tracker items, +${businessPartiesToInsert.size} parties, +${businessEntriesToInsert.size} ledger entries"
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e(LOGIN_OPTIONS_TAG, "Restore merge failed for uid=$uid", e)
                                    Log.e("FIRESTORE", "Restore failed", e)
                                    mainHandler.post {
                                        setSyncing(false)
                                        onMessage(e.message ?: "Restore failed")
                                    }
                                }
                            }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(LOGIN_OPTIONS_TAG, "Restore business entries fetch failed for uid=$uid", e)
                                                    mainHandler.post {
                                                        setSyncing(false)
                                                        onMessage(e.message ?: "Restore failed")
                                                    }
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(LOGIN_OPTIONS_TAG, "Restore business parties fetch failed for uid=$uid", e)
                                            mainHandler.post {
                                                setSyncing(false)
                                                onMessage(e.message ?: "Restore failed")
                                            }
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(LOGIN_OPTIONS_TAG, "Restore subscriptions fetch failed for uid=$uid", e)
                                    mainHandler.post {
                                        setSyncing(false)
                                        onMessage(e.message ?: "Restore failed")
                                    }
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e(LOGIN_OPTIONS_TAG, "Restore incomes fetch failed for uid=$uid", e)
                            Log.e("FIRESTORE", "Restore incomes fetch failed", e)
                            mainHandler.post {
                                setSyncing(false)
                                onMessage(e.message ?: "Restore failed")
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(LOGIN_OPTIONS_TAG, "Restore expenses fetch failed for uid=$uid", e)
                    Log.e("FIRESTORE", "Restore expenses fetch failed", e)
                    mainHandler.post {
                        setSyncing(false)
                        onMessage(e.message ?: "Restore failed")
                    }
                }
        }
        .addOnFailureListener { e ->
            Log.e(LOGIN_OPTIONS_TAG, "Restore categories fetch failed for uid=$uid", e)
            Log.e("FIRESTORE", "Restore categories fetch failed", e)
            val errorMessage = if (
                e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            ) {
                "Restore blocked by Firestore rules. Allow users/{uid}/... for signed-in user."
            } else {
                e.message ?: "Restore failed"
            }
            mainHandler.post {
                setSyncing(false)
                onMessage(errorMessage)
            }
        }},
        onError = { error ->
            mainHandler.post {
                setSyncing(false)
                onMessage(error)
            }
        }
    )
}

private fun prepareLocalDataForAccountSwitch(
    context: Context,
    uid: String,
    accountType: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val prefs = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val previousUid = prefs.getString(ACTIVE_DATA_UID_KEY, null)
    val previousType = prefs.getString(ACTIVE_DATA_ACCOUNT_TYPE_KEY, null)
    val shouldResetLocal = previousUid != null &&
        (previousUid != uid || !previousType.equals(accountType, ignoreCase = true))

    if (!shouldResetLocal) {
        onSuccess()
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            clearAllLocalData(AppDataBase.getDatabase(context))
            onSuccess()
        } catch (e: Exception) {
            Log.e(LOGIN_OPTIONS_TAG, "Failed clearing local data on account switch", e)
            onError(e.message ?: "Failed to prepare local data for account switch")
        }
    }
}

private suspend fun clearAllLocalData(db: AppDataBase) {
    db.incomeDao().clearAllIncome()
    db.expenseDao().clearAllExpenses()
    db.userCategoryDao().clearAllCategories()

    val subscriptions = db.subscriptionDao().getAllSubscriptions().first()
    subscriptions.forEach { db.subscriptionDao().deleteSubscriptionById(it.id) }

    val businessEntries = db.businessEntryDao().getAllBusinessEntries().first()
    businessEntries.forEach { db.businessEntryDao().deleteBusinessEntryById(it.id) }

    val businessParties = db.businessPartyDao().getAllBusinessParties().first()
    businessParties.forEach { db.businessPartyDao().deleteBusinessPartyById(it.id) }
}
