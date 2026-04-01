package com.piggylabs.piggyflow.ui.screens.common.settings

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.piggylabs.piggyflow.R
import com.piggylabs.piggyflow.auth.firebaseAuthWithGoogle
import com.piggylabs.piggyflow.data.local.db.AppDataBase
import com.piggylabs.piggyflow.data.local.db.AppEvents
import com.piggylabs.piggyflow.data.local.db.closeDatabase
import com.piggylabs.piggyflow.data.local.db.reopenDatabase
import com.piggylabs.piggyflow.data.local.entity.BusinessEntryEntity
import com.piggylabs.piggyflow.data.local.entity.BusinessPartyEntity
import com.piggylabs.piggyflow.data.local.entity.ExpenseEntity
import com.piggylabs.piggyflow.data.local.entity.IncomeEntity
import com.piggylabs.piggyflow.data.local.entity.SubscriptionEntity
import com.piggylabs.piggyflow.data.local.entity.UserCategoryEntity
import com.piggylabs.piggyflow.navigation.About
import com.piggylabs.piggyflow.navigation.BusinessHome
import com.piggylabs.piggyflow.navigation.Home
import com.piggylabs.piggyflow.navigation.Profile
import com.piggylabs.piggyflow.navigation.SignIn
import com.piggylabs.piggyflow.navigation.components.BottomBar
import com.piggylabs.piggyflow.ui.theme.appColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import coil3.compose.SubcomposeAsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

private const val SETTINGS_TAG = "SettingsFlow"
private const val SETTINGS_PREF = "MY_PRE"
private const val CLEARED_TRACKER_NOTIFICATION_KEY = "cleared_tracker_notifications"
private const val LAST_SYNCED_AT_KEY = "last_synced_at"
private const val ACTIVE_DATA_UID_KEY = "active_data_uid"
private const val ACTIVE_DATA_ACCOUNT_TYPE_KEY = "active_data_account_type"
private const val PERSONAL_SIGNED_IN_UID_KEY = "personal_signed_in_uid"
private const val BUSINESS_SIGNED_IN_UID_KEY = "business_signed_in_uid"

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
    val isDark = isSystemInDarkTheme()

    val sharedPreferences = context.getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE)
    val userName = sharedPreferences.getString("userName","Guest")
    var accountType by remember {
        mutableStateOf(sharedPreferences.getString("account_type", "personal").orEmpty())
    }
    val lastSyncedAt = sharedPreferences.getLong(LAST_SYNCED_AT_KEY, 0L)

    var isSignedIn by remember {
        mutableStateOf(
            isSignedInForAccountType(
                prefs = sharedPreferences,
                accountType = accountType,
                firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
            )
        )
    }
    val email = if (isSignedIn) {
        FirebaseAuth.getInstance().currentUser?.email ?: "Not connected"
    } else {
        "Not connected"
    }
    val profilePhotoUrl = if (isSignedIn) {
        FirebaseAuth.getInstance().currentUser?.photoUrl?.toString().orEmpty()
    } else {
        ""
    }
    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showReauthDialog by remember { mutableStateOf(false) }
    var reauthPassword by remember { mutableStateOf("") }
    var isReauthing by remember { mutableStateOf(false) }

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

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(SETTINGS_TAG, "Google launcher completed: resultCode=${result.resultCode}")
        isGoogleSigningIn = false

        if (result.resultCode != Activity.RESULT_OK) {
            Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken.isNullOrEmpty()) {
                Toast.makeText(context, "Google token missing", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }

            firebaseAuthWithGoogle(
                idToken = idToken,
                context = context,
                onSuccess = { uid, _ ->
                    saveSignedInUidForAccountType(sharedPreferences, accountType, uid)
                    sharedPreferences.edit().putString("uid", uid).apply()
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
                    isSignedIn = isSignedInForAccountType(
                        prefs = sharedPreferences,
                        accountType = accountType,
                        firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
                    )
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            )
        } catch (e: ApiException) {
            val message = when (e.statusCode) {
                10 -> "Google Sign-In misconfigured. Update Firebase config."
                12501 -> "Google Sign-In cancelled."
                else -> "Google Sign-In failed (code ${e.statusCode})"
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Unexpected Google Sign-In error", Toast.LENGTH_LONG).show()
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear local data?") },
            text = { Text("This removes categories, income, expenses, tracker items, and business data from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDataDialog = false
                        clearLocalAppData(context)
                        Toast.makeText(context, "Local data cleared", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Clear", color = appColors().red) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            shape = RoundedCornerShape(12.dp),
            containerColor = if (isDark) Color.Black else Color.White,
            title = { Text("Logout?") },
            text = { Text("You will be signed out from the synced account on this device, but you can continue using the app offline.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        if (isGoogleSigningIn || isSyncing) return@TextButton
                        FirebaseAuth.getInstance().signOut()
                        googleSignInClient.signOut().addOnCompleteListener {
                            sharedPreferences.edit()
                                .remove("uid")
                                .remove(PERSONAL_SIGNED_IN_UID_KEY)
                                .remove(BUSINESS_SIGNED_IN_UID_KEY)
                                .remove(LAST_SYNCED_AT_KEY)
                                .apply()
                            isSignedIn = false
                            Toast.makeText(context, "Logged out. Offline mode is still available.", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            sharedPreferences.edit()
                                .remove("uid")
                                .remove(PERSONAL_SIGNED_IN_UID_KEY)
                                .remove(BUSINESS_SIGNED_IN_UID_KEY)
                                .remove(LAST_SYNCED_AT_KEY)
                                .apply()
                            isSignedIn = false
                            Toast.makeText(context, "Logged out. Offline mode is still available.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Logout", color = appColors().red) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete account?") },
            containerColor = if (isDark) Color.Black else Color.White,
            text = {
                Text("This will permanently remove your synced account and cloud backup data. For email accounts, your password will be required before deletion.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val requiresPasswordReauth = currentUser?.email != null &&
                            currentUser.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }

                        if (requiresPasswordReauth) {
                            reauthPassword = ""
                            showReauthDialog = true
                        } else {
                            deleteAccount(
                                context = context,
                                onProgress = { syncing -> isSyncing = syncing },
                                onComplete = { message, deleted ->
                                    if (deleted) {
                                        sharedPreferences.edit()
                                            .remove("uid")
                                            .remove(PERSONAL_SIGNED_IN_UID_KEY)
                                            .remove(BUSINESS_SIGNED_IN_UID_KEY)
                                            .remove(LAST_SYNCED_AT_KEY)
                                            .apply()
                                        isSignedIn = false
                                    }
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                ) { Text("Delete", color = appColors().red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showReauthDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isReauthing) {
                    showReauthDialog = false
                    reauthPassword = ""
                }
            },
            containerColor = if (isDark) Color.Black else Color.White,
            title = { Text("Confirm password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter your account password to confirm deletion.")
                    OutlinedTextField(
                        value = reauthPassword,
                        onValueChange = { reauthPassword = it },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        placeholder = { Text("Password") },
                        shape = RoundedCornerShape(20),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val user = FirebaseAuth.getInstance().currentUser
                        val userEmail = user?.email

                        if (user == null || userEmail.isNullOrBlank()) {
                            showReauthDialog = false
                            Toast.makeText(context, "No signed-in account found", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        if (reauthPassword.isBlank()) {
                            Toast.makeText(context, "Enter your password", Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }

                        isReauthing = true
                        val credential = EmailAuthProvider.getCredential(userEmail, reauthPassword)
                        user.reauthenticate(credential)
                            .addOnSuccessListener {
                                isReauthing = false
                                showReauthDialog = false
                                reauthPassword = ""
                                deleteAccount(
                                    context = context,
                                    onProgress = { syncing -> isSyncing = syncing },
                                    onComplete = { message, deleted ->
                                        if (deleted) {
                                            sharedPreferences.edit()
                                                .remove("uid")
                                                .remove(PERSONAL_SIGNED_IN_UID_KEY)
                                                .remove(BUSINESS_SIGNED_IN_UID_KEY)
                                                .remove(LAST_SYNCED_AT_KEY)
                                                .apply()
                                            isSignedIn = false
                                        }
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                            .addOnFailureListener { e ->
                                isReauthing = false
                                Toast.makeText(
                                    context,
                                    e.message ?: "Password verification failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    },
                    enabled = !isReauthing
                ) {
                    Text(
                        text = if (isReauthing) "Checking..." else "Continue",
                        color = appColors().red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isReauthing) {
                            showReauthDialog = false
                            reauthPassword = ""
                        }
                    }
                ) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF050505), Color(0xFF121212), Color(0xFF1A1A1A))
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSignedIn && profilePhotoUrl.isNotBlank()) {
                                    SubcomposeAsyncImage(
                                        model = profilePhotoUrl,
                                        contentDescription = "Profile picture",
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            Text(
                                                text = userName?.trim()?.firstOrNull()?.uppercase() ?: "P",
                                                color = Color.White,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        error = {
                                            Text(
                                                text = userName?.trim()?.firstOrNull()?.uppercase() ?: "P",
                                                color = Color.White,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    )
                                } else {
                                    Text(
                                        text = userName?.trim()?.firstOrNull()?.uppercase() ?: "P",
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            SettingsPill(
                                label = accountType.replaceFirstChar { it.uppercase() }
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Settings",
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$userName • $email",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = formatLastSynced(lastSyncedAt),
                                color = Color.White.copy(alpha = 0.74f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        item { SettingsSectionTitle("Account") }

        item {
            SettingsActionCard(
                title = "Open Profile",
                subtitle = "Manage your display name and account details",
                icon = Icons.Default.Person,
                onClick = { navController.navigate(Profile.route) { launchSingleTop = true } }
            )
        }

        item {
            val targetAccountType = if (accountType.equals("business", ignoreCase = true)) {
                "personal"
            } else {
                "business"
            }
            val targetRoute = if (targetAccountType == "business") {
                BusinessHome.route
            } else {
                Home.route
            }

            SettingsActionCard(
                title = "Switch to ${targetAccountType.replaceFirstChar { it.uppercase() }}",
                subtitle = "Change app flow and bottom navigation to ${targetAccountType.replaceFirstChar { it.lowercase() }} mode",
                icon = Icons.Default.SwapHoriz,
                onClick = {
                    sharedPreferences.edit()
                        .putString("account_type", targetAccountType)
                        .apply()
                    val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
                    val targetModeSignedIn = isSignedInForAccountType(
                        prefs = sharedPreferences,
                        accountType = targetAccountType,
                        firebaseUid = firebaseUid
                    )
                    if (targetModeSignedIn) {
                        sharedPreferences.edit().putString("uid", firebaseUid).apply()
                    } else {
                        sharedPreferences.edit().remove("uid").apply()
                    }
                    accountType = targetAccountType
                    isSignedIn = targetModeSignedIn
                    Toast.makeText(
                        context,
                        "Switched to ${targetAccountType.replaceFirstChar { it.uppercase() }} mode",
                        Toast.LENGTH_SHORT
                    ).show()

                    navController.navigate(targetRoute) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }

        item {
            if (!isSignedIn) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsActionCard(
                        title = "Connect Google Account",
                        subtitle = "Sign in for ${accountType.replaceFirstChar { it.uppercase() }} mode backup/restore",
                        iconPainter = painterResource(id = R.drawable.google),
                        onClick = {
                            if (isGoogleSigningIn) return@SettingsActionCard
                            isGoogleSigningIn = true
                            googleSignInClient.signOut()
                                .addOnCompleteListener { googleLauncher.launch(googleSignInClient.signInIntent) }
                                .addOnFailureListener { googleLauncher.launch(googleSignInClient.signInIntent) }
                        }
                    )

//                    SettingsActionCard(
//                        title = "Sign in with Email",
//                        subtitle = "Use your existing email and password account",
//                        icon = Icons.Default.Person,
//                        onClick = {
//                            navController.navigate(SignIn.route) { launchSingleTop = true }
//                        }
//                    )
                }
            } else {
                SettingsActionCard(
                    title = if (isSyncing) "Syncing backup..." else "Sync Now",
                    subtitle = "Upload local categories, income, and expenses to cloud backup",
                    icon = Icons.Default.CloudDone,
                    progress = if (isSyncing) 1f else null,
                    onClick = {
                        if (!isSignedIn) {
                            Toast.makeText(
                                context,
                                "Sign in to ${accountType.replaceFirstChar { it.uppercase() }} mode first",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@SettingsActionCard
                        }
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid.isNullOrEmpty()) {
                            Toast.makeText(context, "Please sign in first", Toast.LENGTH_SHORT).show()
                            return@SettingsActionCard
                        }
                        syncLocalDataToFirebase(
                            context = context,
                            uid = uid,
                            setSyncing = { isSyncing = it },
                            onMessage = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
                        )
                    }
                )
            }
        }

        if (isSignedIn) {
            item {
                SettingsActionCard(
                    title = "Logout",
                    subtitle = "Disconnect this device from your synced Google account",
                    icon = Icons.Default.Logout,
                    titleColor = appColors().red,
                    onClick = { showLogoutDialog = true }
                )
            }

            item {
                SettingsActionCard(
                    title = "Delete Account",
                    subtitle = "Permanently remove your synced account and backup data",
                    icon = Icons.Default.DeleteForever,
                    titleColor = appColors().red,
                    onClick = { showDeleteAccountDialog = true }
                )
            }
        }

        item { SettingsSectionTitle("Data") }

        item {
            SettingsActionCard(
                title = "Clear Notification History",
                subtitle = "Reset cleared reminders so hidden tracker alerts appear again",
                icon = Icons.Default.NotificationsNone,
                onClick = {
                    sharedPreferences.edit().remove(CLEARED_TRACKER_NOTIFICATION_KEY).apply()
                    Toast.makeText(context, "Notification history cleared", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            SettingsActionCard(
                title = "Clear Local Data",
                subtitle = "Remove all on-device categories, transactions, tracker and business records",
                icon = Icons.Default.DeleteSweep,
                titleColor = appColors().red,
                onClick = { showClearDataDialog = true }
            )
        }

        item { SettingsSectionTitle("App") }

        item {
            SettingsActionCard(
                title = "Privacy & Security",
                subtitle = "Your data stays local unless you explicitly sync with Google",
                icon = Icons.Default.Security,
                onClick = {
                    Toast.makeText(context, "Local-first app. Google sync is optional.", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            SettingsSectionTitle("Support")
        }

        item {
            SettingsActionCard(
                title = "About PiggyFlow",
                subtitle = "Version info and app overview",
                icon = Icons.Default.Info,
                onClick = {
                    navController.navigate(About.route) { launchSingleTop = true }
                }
            )
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = appColors().text
    )
}

@Composable
private fun SettingsPill(label: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    titleColor: Color = appColors().text,
    progress: Float? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(appColors().green.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        iconPainter != null -> Image(
                            painter = iconPainter,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        icon != null -> Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = appColors().green,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                            color = titleColor
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    if (progress != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = appColors().green,
                            trackColor = appColors().background
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun clearLocalAppData(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            closeDatabase(context)
            context.deleteDatabase("app_database")
            reopenDatabase(context)
            AppEvents.tryEmitDbRecreated()
        } catch (e: Exception) {
            Log.e(SETTINGS_TAG, "Failed to clear local data", e)
        }
    }
}

private fun deleteAccount(
    context: Context,
    onProgress: (Boolean) -> Unit,
    onComplete: (message: String, deleted: Boolean) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        onComplete("No signed-in account found", false)
        return
    }

    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { onProgress(true) }

    val firestore = FirebaseFirestore.getInstance()
    val userDoc = firestore.collection("users").document(user.uid)
    val accountType = context.getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE)
        .getString("account_type", "personal")
        ?.lowercase()
        ?: "personal"
    val modeDoc = userDoc.collection("modes").document(accountType)

    fun finish(message: String, deleted: Boolean) {
        mainHandler.post {
            onProgress(false)
            if (deleted) {
                clearLocalAppData(context)
            }
            onComplete(message, deleted)
        }
    }

    fun deleteAuthUser(successMessage: String) {
        user.delete()
            .addOnSuccessListener {
                FirebaseAuth.getInstance().signOut()
                finish(successMessage, true)
            }
            .addOnFailureListener { e ->
                Log.e(SETTINGS_TAG, "Firebase auth delete failed", e)
                val message = if ((e.message ?: "").contains("recent", ignoreCase = true)) {
                    "For security, please log in again and then delete your account."
                } else {
                    e.message ?: "Account deletion failed"
                }
                finish(message, false)
            }
    }

    userDoc.get()
        .addOnSuccessListener { doc ->
            val allTypes = (doc.get("accountTypes") as? List<*>)
                ?.mapNotNull { it as? String }
                ?.map { it.lowercase() }
                ?.toMutableSet()
                ?: mutableSetOf()
            doc.getString("accountType")?.lowercase()?.let(allTypes::add)

            val remainingTypes = allTypes.filter { it != accountType }

            deleteModeBackupData(
                modeDoc = modeDoc,
                onSuccess = {
                    if (remainingTypes.isEmpty()) {
                        userDoc.delete()
                            .addOnSuccessListener {
                                deleteAuthUser("Account deleted successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e(SETTINGS_TAG, "Failed to delete main user doc", e)
                                finish(e.message ?: "Failed to delete account data", false)
                            }
                    } else {
                        val updates = mapOf(
                            "accountTypes" to remainingTypes,
                            "accountType" to remainingTypes.first(),
                            "userNames.$accountType" to FieldValue.delete(),
                            "updatedAt" to System.currentTimeMillis()
                        )

                        userDoc.set(updates, SetOptions.merge())
                            .addOnSuccessListener {
                                FirebaseAuth.getInstance().signOut()
                                finish(
                                    "${accountType.replaceFirstChar { it.uppercase() }} account deleted. Other account type is still available.",
                                    true
                                )
                            }
                            .addOnFailureListener { e ->
                                Log.e(SETTINGS_TAG, "Failed updating remaining account types", e)
                                finish(e.message ?: "Failed to update account data", false)
                            }
                    }
                },
                onFailure = { error ->
                    finish(error, false)
                }
            )
        }
        .addOnFailureListener { e ->
            Log.e(SETTINGS_TAG, "Failed reading user profile before delete", e)
            finish(e.message ?: "Failed to delete account", false)
        }
}

private fun deleteModeBackupData(
    modeDoc: DocumentReference,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val collections = listOf("categories", "expenses", "incomes", "subscriptions", "businessParties", "businessEntries", "sync")
    val pending = AtomicInteger(collections.size)
    var failed = false

    fun markDone() {
        if (pending.decrementAndGet() == 0 && !failed) {
            modeDoc.delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e ->
                    Log.e(SETTINGS_TAG, "Failed to delete mode doc", e)
                    onFailure(e.message ?: "Failed to delete account data")
                }
        }
    }

    collections.forEach { collectionName ->
        modeDoc.collection(collectionName).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    markDone()
                    return@addOnSuccessListener
                }

                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener { markDone() }
                    .addOnFailureListener { e ->
                        failed = true
                        Log.e(SETTINGS_TAG, "Failed deleting subcollection=$collectionName", e)
                        onFailure(e.message ?: "Failed to delete account data")
                    }
            }
            .addOnFailureListener { e ->
                failed = true
                Log.e(SETTINGS_TAG, "Failed reading subcollection=$collectionName", e)
                onFailure(e.message ?: "Failed to delete account data")
            }
    }
}

private fun syncLocalDataToFirebase(
    context: Context,
    uid: String,
    setSyncing: (Boolean) -> Unit,
    onMessage: (String) -> Unit
) {
    Log.d(SETTINGS_TAG, "syncLocalDataToFirebase started for uid=$uid")
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
            Log.d(SETTINGS_TAG, "Local data prepared for sync uid=$uid categories=${categories.size} expenses=${expenses.size} incomes=${incomes.size} subscriptions=${subscriptions.size} businessParties=${businessParties.size} businessEntries=${businessEntries.size}")

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
                    context.getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE)
                        .edit()
                        .putLong(LAST_SYNCED_AT_KEY, System.currentTimeMillis())
                        .putString(ACTIVE_DATA_UID_KEY, uid)
                        .putString(ACTIVE_DATA_ACCOUNT_TYPE_KEY, accountType)
                        .apply()
                    mainHandler.post {
                        setSyncing(false)
                        onMessage("Sync completed successfully")
                    }
                }
                .addOnFailureListener { e ->
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
    Log.d(SETTINGS_TAG, "restoreOrSyncAfterLogin started for uid=$uid")
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
                            val hasBackupData =
                                categorySnap.documents.isNotEmpty() ||
                                    expenseSnap.documents.isNotEmpty() ||
                                    incomeSnap.documents.isNotEmpty() ||
                                    subscriptionSnap.documents.isNotEmpty() ||
                                    businessPartySnap.documents.isNotEmpty() ||
                                    businessEntrySnap.documents.isNotEmpty()

                            if (!hasBackupData) {
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

                                    val localCategories = db.userCategoryDao().getAllCategories().first()
                                    val localExpenses = db.expenseDao().getAllExpenses().first()
                                    val localIncomes = db.incomeDao().getAllIncome().first()
                                    val localSubscriptions = db.subscriptionDao().getAllSubscriptions().first()
                                    val localBusinessParties = db.businessPartyDao().getAllBusinessParties().first()
                                    val localBusinessEntries = db.businessEntryDao().getAllBusinessEntries().first()

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

                                    if (categoriesToInsert.isNotEmpty()) db.userCategoryDao().insertAllCategories(categoriesToInsert)
                                    if (expensesToInsert.isNotEmpty()) db.expenseDao().insertAllExpenses(expensesToInsert)
                                    if (incomesToInsert.isNotEmpty()) db.incomeDao().insertAllIncome(incomesToInsert)
                                    subscriptionsToInsert.forEach { db.subscriptionDao().insertSubscription(it) }
                                    businessPartiesToInsert.forEach { db.businessPartyDao().insertBusinessParty(it) }
                                    businessEntriesToInsert.forEach { db.businessEntryDao().insertBusinessEntry(it) }
                                    context.getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE)
                                        .edit()
                                        .putLong(LAST_SYNCED_AT_KEY, System.currentTimeMillis())
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
                                    mainHandler.post {
                                        setSyncing(false)
                                        onMessage(e.message ?: "Restore failed")
                                    }
                                }
                            }
                                                }
                                                .addOnFailureListener { e ->
                                                    mainHandler.post {
                                                        setSyncing(false)
                                                        onMessage(e.message ?: "Restore failed")
                                                    }
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            mainHandler.post {
                                                setSyncing(false)
                                                onMessage(e.message ?: "Restore failed")
                                            }
                                        }
                                }
                                .addOnFailureListener { e ->
                                    mainHandler.post {
                                        setSyncing(false)
                                        onMessage(e.message ?: "Restore failed")
                                    }
                                }
                        }
                        .addOnFailureListener { e ->
                            mainHandler.post {
                                setSyncing(false)
                                onMessage(e.message ?: "Restore failed")
                            }
                        }
                }
                .addOnFailureListener { e ->
                    mainHandler.post {
                        setSyncing(false)
                        onMessage(e.message ?: "Restore failed")
                    }
                }
        }
        .addOnFailureListener { e ->
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
    val prefs = context.getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE)
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
            Log.e(SETTINGS_TAG, "Failed clearing local data on account switch", e)
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

private fun formatLastSynced(timestamp: Long): String {
    if (timestamp <= 0L) return "Last synced: Not synced yet"
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return "Last synced: ${formatter.format(Date(timestamp))}"
}

private fun signedInUidKeyForAccountType(accountType: String): String {
    return if (accountType.equals("business", ignoreCase = true)) {
        BUSINESS_SIGNED_IN_UID_KEY
    } else {
        PERSONAL_SIGNED_IN_UID_KEY
    }
}

private fun saveSignedInUidForAccountType(
    prefs: android.content.SharedPreferences,
    accountType: String,
    uid: String
) {
    prefs.edit()
        .putString(signedInUidKeyForAccountType(accountType), uid)
        .apply()
}

private fun isSignedInForAccountType(
    prefs: android.content.SharedPreferences,
    accountType: String,
    firebaseUid: String?
): Boolean {
    if (firebaseUid.isNullOrBlank()) return false
    val storedModeUid = prefs.getString(signedInUidKeyForAccountType(accountType), null)
    return !storedModeUid.isNullOrBlank() && storedModeUid == firebaseUid
}
