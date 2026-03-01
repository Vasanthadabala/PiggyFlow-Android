package com.piggylabs.piggyflow.ui.screens.settings

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.piggylabs.piggyflow.data.local.entity.ExpenseEntity
import com.piggylabs.piggyflow.data.local.entity.IncomeEntity
import com.piggylabs.piggyflow.data.local.entity.UserCategoryEntity
import com.piggylabs.piggyflow.navigation.About
import com.piggylabs.piggyflow.navigation.components.BottomBar
import com.piggylabs.piggyflow.ui.theme.appColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingScreen(navController: NavHostController){

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) {  innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            SettingScreenComponent(navController = navController)
        }
    }

}

@Composable
fun SettingScreenComponent(navController: NavHostController){
    val context = LocalContext.current

    val sharedPreferences = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val userName = sharedPreferences.getString("userName","Guest")

    var isSignedIn by remember {
        mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
    }

    /* -------- Google Sign In Setup -------- */

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

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

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
                    Log.e("FIRESTORE", "❌ Google ID TOKEN is NULL")
                    Toast.makeText(context, "Google token missing", Toast.LENGTH_SHORT).show()
                    return@rememberLauncherForActivityResult
                }

                Log.d("FIRESTORE", "✅ Google ID TOKEN received, calling Firebase")

                firebaseAuthWithGoogle(
                    idToken = idToken,
                    context = context,
                    onSuccess = { uid, name ->
                        isSignedIn = true
                        restoreOrSyncAfterLogin(
                            context = context,
                            uid = uid,
                            setSyncing = { isSyncing = it },
                            onMessage = { message ->
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onFailure = { error ->
                        isSignedIn = false
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors().text
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Profile",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = appColors().text
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = appColors().container
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(appColors().green),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName?.trim()?.firstOrNull()?.uppercase() ?: "",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "$userName",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().text
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = appColors().text
            )

            if (!isSignedIn) {
                // -------- Google Sign In Card --------
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            if (isGoogleSigningIn) return@clickable
                            isGoogleSigningIn = true
                            googleSignInClient.signOut()
                                .addOnCompleteListener {
                                    googleLauncher.launch(googleSignInClient.signInIntent)
                                }
                                .addOnFailureListener {
                                    Log.e("FIRESTORE", "Failed to clear old Google session", it)
                                    googleLauncher.launch(googleSignInClient.signInIntent)
                                }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors().container),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "",
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Sign in with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors().text
                        )
                    }
                }
            } else {

                // -------- Sync Card --------
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            val uid = FirebaseAuth.getInstance().currentUser?.uid
                            if (uid.isNullOrEmpty()) {
                                Toast.makeText(context, "Please sign in first", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            syncLocalDataToFirebase(
                                context = context,
                                uid = uid,
                                setSyncing = { isSyncing = it },
                                onMessage = { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors().container),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = appColors().green,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isSyncing) "Syncing..." else "Sync Now",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = appColors().text
                            )
                            if (isSyncing) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = appColors().green,
                                    trackColor = appColors().container
                                )
                            }
                        }
                    }
                }

                // -------- Logout Card --------
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            if (isGoogleSigningIn || isSyncing) return@clickable
                            FirebaseAuth.getInstance().signOut()
                            googleSignInClient.signOut().addOnCompleteListener {
                                sharedPreferences.edit()
                                    .putBoolean("is_logged_in", false)
                                    .remove("uid")
                                    .remove("userName")
                                    .apply()
                                isSignedIn = false
                                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Log.e("FIRESTORE", "Google signOut failed on logout", it)
                                sharedPreferences.edit()
                                    .putBoolean("is_logged_in", false)
                                    .remove("uid")
                                    .remove("userName")
                                    .apply()
                                isSignedIn = false
                                Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors().container),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Logout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Red
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "About",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = appColors().text
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 4.dp)
                    .clickable{
                        navController.navigate(About.route){
                            launchSingleTop = true
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = appColors().container
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "About",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().text
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "",
                        tint = appColors().text,
                        modifier = Modifier
                            .rotate(180f)
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
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { setSyncing(true) }
    val db = AppDataBase.getDatabase(context)
    val firestore = FirebaseFirestore.getInstance()
    val userDoc = firestore.collection("users").document(uid)

    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            val categories = db.userCategoryDao().getAllCategories().first()
            val expenses = db.expenseDao().getAllExpenses().first()
            val incomes = db.incomeDao().getAllIncome().first()

            val batch = firestore.batch()

            val syncMeta = mapOf(
                "updatedAt" to System.currentTimeMillis(),
                "categoryCount" to categories.size,
                "expenseCount" to expenses.size,
                "incomeCount" to incomes.size
            )

            batch.set(
                userDoc.collection("sync").document("meta"),
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
                    userDoc.collection("categories").document(category.id.toString()),
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
                    userDoc.collection("expenses").document(expense.id.toString()),
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
                    userDoc.collection("incomes").document(income.id.toString()),
                    incomeData,
                    SetOptions.merge()
                )
            }

            batch.commit()
                .addOnSuccessListener {
                    mainHandler.post {
                        setSyncing(false)
                        onMessage("Sync completed successfully")
                    }
                }
                .addOnFailureListener { e ->
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
            Log.e("FIRESTORE", "Sync preparation failed", e)
            mainHandler.post {
                setSyncing(false)
                onMessage(e.message ?: "Sync failed")
            }
        }
    }
}

private fun restoreOrSyncAfterLogin(
    context: Context,
    uid: String,
    setSyncing: (Boolean) -> Unit,
    onMessage: (String) -> Unit
) {
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { setSyncing(true) }

    val firestore = FirebaseFirestore.getInstance()
    val userDoc = firestore.collection("users").document(uid)

    userDoc.collection("categories").get()
        .addOnSuccessListener { categorySnap ->
            userDoc.collection("expenses").get()
                .addOnSuccessListener { expenseSnap ->
                    userDoc.collection("incomes").get()
                        .addOnSuccessListener { incomeSnap ->
                            val hasBackupData =
                                categorySnap.documents.isNotEmpty() ||
                                    expenseSnap.documents.isNotEmpty() ||
                                    incomeSnap.documents.isNotEmpty()

                            if (!hasBackupData) {
                                mainHandler.post { setSyncing(false) }
                                syncLocalDataToFirebase(context, uid, setSyncing, onMessage)
                                return@addOnSuccessListener
                            }

                            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
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

                                    val localCategories = db.userCategoryDao().getAllCategories().first()
                                    val localExpenses = db.expenseDao().getAllExpenses().first()
                                    val localIncomes = db.incomeDao().getAllIncome().first()

                                    val localCategoryIds = localCategories.map { it.id }.toSet()
                                    val localExpenseIds = localExpenses.map { it.id }.toSet()
                                    val localIncomeIds = localIncomes.map { it.id }.toSet()

                                    val categoriesToInsert = categories.filter { it.id !in localCategoryIds }
                                    val expensesToInsert = expenses.filter { it.id !in localExpenseIds }
                                    val incomesToInsert = incomes.filter { it.id !in localIncomeIds }

                                    if (categoriesToInsert.isNotEmpty()) {
                                        db.userCategoryDao().insertAllCategories(categoriesToInsert)
                                    }
                                    if (expensesToInsert.isNotEmpty()) {
                                        db.expenseDao().insertAllExpenses(expensesToInsert)
                                    }
                                    if (incomesToInsert.isNotEmpty()) {
                                        db.incomeDao().insertAllIncome(incomesToInsert)
                                    }

                                    mainHandler.post {
                                        setSyncing(false)
                                        onMessage(
                                            "Backup merged: +${categoriesToInsert.size} categories, +${expensesToInsert.size} expenses, +${incomesToInsert.size} incomes"
                                        )
                                    }
                                } catch (e: Exception) {
                                    Log.e("FIRESTORE", "Restore failed", e)
                                    mainHandler.post {
                                        setSyncing(false)
                                        onMessage(e.message ?: "Restore failed")
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FIRESTORE", "Restore incomes fetch failed", e)
                            mainHandler.post {
                                setSyncing(false)
                                onMessage(e.message ?: "Restore failed")
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("FIRESTORE", "Restore expenses fetch failed", e)
                    mainHandler.post {
                        setSyncing(false)
                        onMessage(e.message ?: "Restore failed")
                    }
                }
        }
        .addOnFailureListener { e ->
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
        }
}
