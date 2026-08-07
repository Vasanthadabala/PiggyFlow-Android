package com.piggylabs.piggyflow.features.profile.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.SubcomposeAsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.piggylabs.piggyflow.R
import com.piggylabs.piggyflow.features.auth.data.firebaseAuthWithGoogle
import com.piggylabs.piggyflow.core.navigation.About
import com.piggylabs.piggyflow.core.navigation.Appearance
import com.piggylabs.piggyflow.core.navigation.BusinessHome
import com.piggylabs.piggyflow.core.navigation.Home
import com.piggylabs.piggyflow.core.navigation.InfoScreen
import com.piggylabs.piggyflow.core.navigation.TopBar
import com.piggylabs.piggyflow.features.settings.presentation.clearLocalAppData
import com.piggylabs.piggyflow.features.settings.presentation.deleteAccount
import com.piggylabs.piggyflow.features.settings.presentation.formatLastSynced
import com.piggylabs.piggyflow.features.settings.presentation.isSignedInForAccountType
import com.piggylabs.piggyflow.features.settings.presentation.restoreOrSyncAfterLogin
import com.piggylabs.piggyflow.features.settings.presentation.saveSignedInUidForAccountType
import com.piggylabs.piggyflow.features.settings.presentation.syncLocalDataToFirebase
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.google.firebase.auth.EmailAuthProvider
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.piggylabs.piggyflow.core.di.preferencesManager

@ExperimentalMaterial3Api
@Composable
fun ProfileScreen(navController: NavHostController, viewModel: HomeViewModel){
    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            ProfileScreenComponent(navController = navController, viewModel = viewModel)
        }
    }
}

@Composable
fun ProfileScreenComponent(navController: NavHostController, viewModel: HomeViewModel){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val colors = appColors()

    val prefs = preferencesManager(context)
    val storedPrefs = remember(context) { prefs.snapshotBlocking() }
    val savedUserName = storedPrefs.userName
    var accountType by remember {
        mutableStateOf(storedPrefs.accountType)
    }
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
    val email = if (isSignedIn) FirebaseAuth.getInstance().currentUser?.email ?: "Not connected" else "Not connected"
    val profilePhotoUrl = if (isSignedIn) FirebaseAuth.getInstance().currentUser?.photoUrl?.toString().orEmpty() else ""

    var currentUserName by remember { mutableStateOf(savedUserName) }
    var name by remember { mutableStateOf(savedUserName) }
    var editMode by remember { mutableStateOf(false) }

    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var isSeedingSampleData by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showReauthDialog by remember { mutableStateOf(false) }
    var reauthPassword by remember { mutableStateOf("") }
    var isReauthing by remember { mutableStateOf(false) }

    val accountLabel = accountType.replaceFirstChar { it.uppercase() }

    // Account Overview — same lifetime aggregates the Home screen shows.
    val totalIncome = remember(uiState.income) { uiState.income.sumOf { it.amount } }
    val totalExpense = remember(uiState.expenses) { uiState.expenses.sumOf { it.amount } }
    val netBalance = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100) else 0.0

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
                    prefs.setUidAsync(uid)
                    isSignedIn = true
                    restoreOrSyncAfterLogin(
                        context = context,
                        uid = uid,
                        setSyncing = { isSyncing = it },
                        onMessage = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
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
                TextButton(onClick = {
                    showClearDataDialog = false
                    clearLocalAppData(context)
                    Toast.makeText(context, "Local data cleared", Toast.LENGTH_SHORT).show()
                }) { Text("Clear", color = colors.negative) }
            },
            dismissButton = { TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") } }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout?") },
            text = { Text("You will be signed out from the synced account on this device, but you can continue using the app offline.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    if (isGoogleSigningIn || isSyncing) return@TextButton
                    FirebaseAuth.getInstance().signOut()
                    googleSignInClient.signOut().addOnCompleteListener {
                        prefs.clearSignInStateAsync()
                        isSignedIn = false
                        Toast.makeText(context, "Logged out. Offline mode is still available.", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        prefs.clearSignInStateAsync()
                        isSignedIn = false
                        Toast.makeText(context, "Logged out. Offline mode is still available.", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Logout", color = colors.negative) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete account?") },
            text = { Text("This will permanently remove your synced account and cloud backup data. For email accounts, your password will be required before deletion.") },
            confirmButton = {
                TextButton(onClick = {
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
                                    prefs.clearSignInStateAsync()
                                    isSignedIn = false
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }) { Text("Delete", color = colors.negative) }
            },
            dismissButton = { TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") } }
        )
    }

    if (showReauthDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isReauthing) { showReauthDialog = false; reauthPassword = "" }
            },
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
                                            prefs.clearSignInStateAsync()
                                            isSignedIn = false
                                        }
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                            .addOnFailureListener { e ->
                                isReauthing = false
                                Toast.makeText(context, e.message ?: "Password verification failed", Toast.LENGTH_LONG).show()
                            }
                    },
                    enabled = !isReauthing
                ) { Text(if (isReauthing) "Checking..." else "Continue", color = colors.negative) }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (!isReauthing) { showReauthDialog = false; reauthPassword = "" }
                }) { Text("Cancel") }
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

        // HERO
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(64.dp).background(colors.accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSignedIn && profilePhotoUrl.isNotBlank()) {
                            SubcomposeAsyncImage(
                                model = profilePhotoUrl,
                                contentDescription = "Profile picture",
                                modifier = Modifier.size(64.dp).background(colors.accent, CircleShape),
                                contentScale = ContentScale.Crop,
                                loading = { InitialAvatarText(currentUserName) },
                                error = { InitialAvatarText(currentUserName) }
                            )
                        } else {
                            InitialAvatarText(currentUserName)
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = currentUserName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.text)
                        Text(text = email, fontSize = 13.sp, color = colors.textMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.Surface(shape = RoundedCornerShape(10.dp), color = colors.accentSoft) {
                            Text(
                                text = accountLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.onAccentSoft,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // ACCOUNT OVERVIEW
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Account Overview", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.text)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        OverviewStat(label = "Net Balance", value = "₹%,.0f".format(netBalance), color = colors.text)
                        OverviewStat(label = "Income", value = "₹%,.0f".format(totalIncome), color = colors.positive)
                        OverviewStat(label = "Expenses", value = "₹%,.0f".format(totalExpense), color = colors.negative)
                        OverviewStat(label = "Savings", value = "%.0f%%".format(savingsRate), color = colors.accent)
                    }
                }
            }
        }

        item { SectionTitle("Account") }

        item {
            EditableDetailCard(
                title = "Personal Information",
                value = currentUserName,
                draftValue = name,
                icon = Icons.Default.Person,
                isEditing = editMode,
                onValueChange = { name = it },
                onEditToggle = { name = currentUserName; editMode = true },
                onCancel = { name = currentUserName; editMode = false; keyboardController?.hide() },
                onSave = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Provide username", Toast.LENGTH_SHORT).show()
                    } else {
                        currentUserName = name.trim()
                        prefs.setUserNameAsync(currentUserName)
                        editMode = false
                        keyboardController?.hide()
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        item {
            ProfileRow(
                title = "Security",
                subtitle = "Password, biometric and security settings",
                icon = Icons.Default.Lock,
                onClick = { navController.navigate(InfoScreen.forTopic(InfoTopic.SECURITY)) { launchSingleTop = true } }
            )
        }

        item {
            ProfileRow(
                title = "Cloud Sync",
                subtitle = formatLastSynced(lastSyncedAt),
                icon = Icons.Default.CloudDone,
                trailingLabel = if (isSignedIn) "Active" else null,
                progress = if (isSyncing) 1f else null,
                onClick = {
                    if (isSyncing) return@ProfileRow
                    if (!isSignedIn) {
                        if (isGoogleSigningIn) return@ProfileRow
                        isGoogleSigningIn = true
                        googleSignInClient.signOut()
                            .addOnCompleteListener { googleLauncher.launch(googleSignInClient.signInIntent) }
                            .addOnFailureListener { googleLauncher.launch(googleSignInClient.signInIntent) }
                    } else {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid.isNullOrEmpty()) {
                            Toast.makeText(context, "Please sign in first", Toast.LENGTH_SHORT).show()
                            return@ProfileRow
                        }
                        syncLocalDataToFirebase(
                            context = context,
                            uid = uid,
                            setSyncing = { isSyncing = it },
                            onMessage = { message -> Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
                        )
                    }
                }
            )
        }

        item {
            ProfileRow(
                title = "Payment Methods",
                subtitle = "Manage your cards and accounts",
                icon = Icons.Default.CreditCard,
                onClick = { navController.navigate(InfoScreen.forTopic(InfoTopic.PAYMENT_METHODS)) { launchSingleTop = true } }
            )
        }

        item {
            ProfileRow(
                title = "Premium Membership",
                subtitle = "Manage your subscription",
                icon = Icons.Default.WorkspacePremium,
                onClick = { navController.navigate(InfoScreen.forTopic(InfoTopic.PREMIUM)) { launchSingleTop = true } }
            )
        }

        item {
            val targetAccountType = if (accountType.equals("business", ignoreCase = true)) "personal" else "business"
            val targetRoute = if (targetAccountType == "business") BusinessHome.route else Home.route
            ProfileRow(
                title = "Switch to ${targetAccountType.replaceFirstChar { it.uppercase() }}",
                subtitle = "Change app flow and bottom navigation",
                icon = Icons.Default.SwapHoriz,
                onClick = {
                    prefs.setAccountTypeAsync(targetAccountType)
                    val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid
                    val targetModeSignedIn = isSignedInForAccountType(context, targetAccountType, firebaseUid)
                    // isSignedInForAccountType is only true for a non-blank uid.
                    if (targetModeSignedIn && firebaseUid != null) {
                        prefs.setUidAsync(firebaseUid)
                    } else {
                        prefs.removeUidAsync()
                    }
                    accountType = targetAccountType
                    isSignedIn = targetModeSignedIn
                    Toast.makeText(context, "Switched to ${targetAccountType.replaceFirstChar { it.uppercase() }} mode", Toast.LENGTH_SHORT).show()
                    navController.navigate(targetRoute) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }

        item { SectionTitle("Preferences") }

        item {
            ProfileRow(
                title = "Notifications",
                subtitle = "Clear cleared reminders so hidden tracker alerts reappear",
                icon = Icons.Default.NotificationsNone,
                onClick = {
                    prefs.clearClearedTrackerNotificationsAsync()
                    Toast.makeText(context, "Notification history cleared", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            ProfileRow(
                title = "Appearance",
                subtitle = "Choose theme and app appearance",
                icon = Icons.Default.Palette,
                onClick = { navController.navigate(Appearance.route) { launchSingleTop = true } }
            )
        }

        item {
            ProfileRow(
                title = "Currency",
                subtitle = "Select your preferred currency",
                icon = Icons.Default.Payments,
                trailingLabel = "INR (₹)",
                onClick = { navController.navigate(InfoScreen.forTopic(InfoTopic.CURRENCY)) { launchSingleTop = true } }
            )
        }

        item {
            ProfileRow(
                title = "Language",
                subtitle = "Choose your preferred language",
                icon = Icons.Default.Language,
                trailingLabel = "English",
                onClick = { navController.navigate(InfoScreen.forTopic(InfoTopic.LANGUAGE)) { launchSingleTop = true } }
            )
        }

        item { SectionTitle("Support") }

        item {
            ProfileRow(
                title = "Help Center",
                subtitle = "FAQs and support articles",
                icon = Icons.Default.HelpOutline,
                onClick = { navController.navigate(InfoScreen.forTopic(InfoTopic.HELP_CENTER)) { launchSingleTop = true } }
            )
        }

        item {
            ProfileRow(
                title = "Contact Support",
                subtitle = "Get in touch with our support team",
                icon = Icons.Default.ContactSupport,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("vasanthadabala@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "PiggyFlow support")
                    }
                    runCatching { context.startActivity(intent) }
                        .onFailure { Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show() }
                }
            )
        }

        item {
            ProfileRow(
                title = "About PiggyFlow",
                subtitle = "Version info and app overview",
                icon = Icons.Default.Info,
                onClick = { navController.navigate(About.route) { launchSingleTop = true } }
            )
        }

        item {
            ProfileRow(
                title = if (isSeedingSampleData) "Loading sample data..." else "Load Sample Data",
                subtitle = "Fill accounts, transactions and tracker with demo data to try the app",
                icon = Icons.Default.Dataset,
                onClick = {
                    if (isSeedingSampleData) return@ProfileRow
                    isSeedingSampleData = true
                    profileViewModel.seedSampleData { added ->
                        isSeedingSampleData = false
                        Toast.makeText(
                            context,
                            if (added) "Sample data loaded" else "Couldn't load sample data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        item {
            ProfileRow(
                title = "Clear Local Data",
                subtitle = "Remove all on-device categories, transactions, tracker and business records",
                icon = Icons.Default.DeleteSweep,
                titleColor = colors.negative,
                onClick = { showClearDataDialog = true }
            )
        }

        if (isSignedIn) {
            item {
                ProfileRow(
                    title = "Delete Account",
                    subtitle = "Permanently remove your synced account and backup data",
                    icon = Icons.Default.DeleteForever,
                    titleColor = colors.negative,
                    onClick = { showDeleteAccountDialog = true }
                )
            }
        }

        item {
            androidx.compose.material3.Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = colors.negativeSoft,
                    contentColor = colors.negative
                )
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Logout", fontWeight = FontWeight.SemiBold)
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun InitialAvatarText(name: String) {
    Text(
        text = name.trim().firstOrNull()?.uppercase() ?: "P",
        color = Color.White,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun OverviewStat(label: String, value: String, color: Color) {
    Column {
        Text(text = label, fontSize = 10.sp, color = appColors().textMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = appColors().textMuted)
}

@Composable
private fun ProfileRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    trailingLabel: String? = null,
    titleColor: Color = appColors().text,
    progress: Float? = null,
    onClick: () -> Unit
) {
    val colors = appColors()
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(38.dp).background(colors.accentSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = colors.onAccentSoft, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = titleColor)
                    Text(text = subtitle, fontSize = 11.sp, color = colors.textMuted)
                }
            }

            if (trailingLabel != null) {
                Text(text = trailingLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.accent)
                Spacer(modifier = Modifier.width(6.dp))
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun EditableDetailCard(
    title: String,
    value: String,
    draftValue: String,
    icon: ImageVector,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    onEditToggle: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val colors = appColors()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(38.dp).background(colors.accentSoft, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = colors.onAccentSoft, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.text)
                        if (!isEditing) {
                            Text(text = value, fontSize = 11.sp, color = colors.textMuted)
                        }
                    }
                }

                if (isEditing) {
                    Row {
                        IconButton(onClick = onCancel) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = colors.textMuted)
                        }
                        IconButton(onClick = onSave) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = colors.accent)
                        }
                    }
                } else {
                    IconButton(onClick = onEditToggle) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit display name", tint = colors.accent)
                    }
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = draftValue,
                    singleLine = true,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(text = "Your Name", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W500, color = colors.textMuted, textAlign = TextAlign.Start))
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = colors.accent,
                        unfocusedIndicatorColor = colors.textMuted,
                        focusedContainerColor = colors.background,
                        unfocusedContainerColor = colors.background,
                        cursorColor = colors.text
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSave() }),
                    textStyle = TextStyle(fontWeight = FontWeight.W500, fontSize = 16.sp, color = colors.text)
                )
            }
        }
    }
}
