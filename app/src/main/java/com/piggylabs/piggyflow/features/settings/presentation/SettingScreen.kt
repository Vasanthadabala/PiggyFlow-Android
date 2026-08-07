package com.piggylabs.piggyflow.features.settings.presentation

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
import com.piggylabs.piggyflow.features.auth.data.firebaseAuthWithGoogle
import com.piggylabs.piggyflow.core.navigation.About
import com.piggylabs.piggyflow.core.navigation.BusinessHome
import com.piggylabs.piggyflow.core.navigation.Home
import com.piggylabs.piggyflow.core.navigation.Profile
import com.piggylabs.piggyflow.core.navigation.SignIn
import com.piggylabs.piggyflow.core.navigation.BottomBar
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import coil3.compose.SubcomposeAsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import com.piggylabs.piggyflow.core.di.preferencesManager

// Shared constants and sync/backup/account-lifecycle helpers (clearLocalAppData,
// deleteAccount, syncLocalDataToFirebase, restoreOrSyncAfterLogin, etc.) live in
// SettingsActions.kt in this same package so ProfileScreen.kt (personal mode) can
// reuse them without duplicating this logic.

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

    val storedPrefsManager = remember(context) { preferencesManager(context) }
    val storedPrefs = remember(context) { storedPrefsManager.snapshotBlocking() }
    val userName = storedPrefs.userName
    var accountType by remember { mutableStateOf(storedPrefs.accountType) }
    val lastSyncedAt = storedPrefs.lastSyncedAt

    var isSignedIn by remember {
        mutableStateOf(
            isSignedInForAccountType(
                context = context,
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
                    saveSignedInUidForAccountType(context, accountType, uid)
                    storedPrefsManager.setUidAsync(uid)
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
                        context = context,
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
                            storedPrefsManager.clearSignInStateAsync()
                            isSignedIn = false
                            Toast.makeText(context, "Logged out. Offline mode is still available.", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            storedPrefsManager.clearSignInStateAsync()
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
                                        storedPrefsManager.clearSignInStateAsync()
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
                                            storedPrefsManager.clearSignInStateAsync()
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
                    storedPrefsManager.setAccountTypeAsync(targetAccountType)
                    val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
                    val targetModeSignedIn = isSignedInForAccountType(
                        context = context,
                        accountType = targetAccountType,
                        firebaseUid = firebaseUid
                    )
                    // isSignedInForAccountType is only true for a non-blank uid.
                    if (targetModeSignedIn && firebaseUid != null) {
                        storedPrefsManager.setUidAsync(firebaseUid)
                    } else {
                        storedPrefsManager.removeUidAsync()
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
                    storedPrefsManager.clearClearedTrackerNotificationsAsync()
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

