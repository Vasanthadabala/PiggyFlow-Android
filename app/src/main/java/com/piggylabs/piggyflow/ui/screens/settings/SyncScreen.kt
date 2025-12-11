package com.piggylabs.piggyflow.ui.screens.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestorePage
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.piggylabs.piggyflow.auth.GoogleAuthManager
import com.piggylabs.piggyflow.data.remote.SyncState
import com.piggylabs.piggyflow.data.remote.SyncViewModel
import com.piggylabs.piggyflow.data.local.db.closeDatabase
import com.piggylabs.piggyflow.data.local.db.reopenDatabase
import com.piggylabs.piggyflow.ui.theme.appColors
import kotlinx.coroutines.delay

@ExperimentalMaterial3Api
@Composable
fun SyncScreen(navController: NavHostController, viewModel: SyncViewModel){
    val isSignedIn by viewModel.isSignedIn.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Backup & Sync",
                    fontSize = 18.sp,
                    color = appColors().green
                ) },
                navigationIcon = {
                    IconButton(onClick = {navController.popBackStack()}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = appColors().green
                        )
                    }
                },
                actions = {
                    if (isSignedIn) {
                        IconButton(onClick = { viewModel.loadBackupInfo() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            SyncScreenComponent(viewModel)
        }

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreenComponent(
    viewModel: SyncViewModel
) {
    val context = LocalContext.current
    val syncState by viewModel.syncState.collectAsState()
    val backupState by viewModel.backupState.collectAsState()
    val isSignedIn by viewModel.isSignedIn.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.setAccount(account)
                signInError = null
            } catch (e: ApiException) {
                signInError = when (e.statusCode) {
                    12501 -> "Sign in cancelled"
                    12500 -> "Sign in failed. Please try again."
                    else -> "Error: ${e.message}"
                }
            }
        } else {
            signInError = "Sign in cancelled"
        }
    }

    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            viewModel.loadBackupInfo()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onDatabaseClose = { closeDatabase(context) }
        viewModel.onDatabaseReopen = { reopenDatabase(context) }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Banner
        when (syncState) {
            is SyncState.Loading -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = appColors().container
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = appColors().text
                        )
                        Text(
                            "Processing...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = appColors().text
                        )
                    }
                }
            }

            is SyncState.Success -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = appColors().container
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = appColors().green
                        )
                        Text(
                            (syncState as SyncState.Success).message,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = appColors().text
                        )
                    }
                }
                LaunchedEffect(Unit) {
                    delay(3000)
                    viewModel.resetState()
                }
            }

            is SyncState.Error -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = appColors().container
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = appColors().red
                        )
                        Text(
                            (syncState as SyncState.Error).message,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = appColors().text
                        )
                    }
                }
                LaunchedEffect(Unit) {
                    delay(3000)
                    viewModel.resetState()
                }
            }

            else -> {
                // Idle - no banner
            }
        }

        // Sign In Error
        signInError?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = appColors().container
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = appColors().red
                    )
                    Text(
                        error,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = appColors().text
                    )
                }
            }
            LaunchedEffect(Unit) {
                delay(3000)
                signInError = null
            }
        }

        if (!isSignedIn) {
            // Not Signed In View
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = appColors().container
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = appColors().green
                    )

                    Text(
                        "Backup Your Data",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = appColors().text
                    )

                    Text(
                        "Sign in with Google to securely backup your data to Google Drive",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val signInIntent =
                                GoogleAuthManager.getSignInClient(context).signInIntent
                            signInLauncher.launch(signInIntent)
                        },
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 1.dp,
                            pressedElevation = 5.dp,
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors().green,
                            contentColor = Color.White
                        ),
                    ) {
                        Icon(
                            Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Sign in with Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Benefits Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = appColors().container
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Why backup?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().text
                    )

                    BenefitItem(
                        icon = Icons.Default.Security,
                        text = "Secure encrypted storage"
                    )
                    BenefitItem(
                        icon = Icons.Default.Sync,
                        text = "Sync across devices"
                    )
                    BenefitItem(
                        icon = Icons.Default.RestorePage,
                        text = "Easy restore if needed"
                    )
                    BenefitItem(
                        icon = Icons.Default.Lock,
                        text = "Only you can access your data"
                    )
                }
            }
        } else {
            // Signed In View

            // Backup Info Card (if backup exists)
            if (backupState.exists) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = appColors().container
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = appColors().green,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                "Backup Available",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = appColors().text
                            )
                        }

                        Divider(
                            color = Color.LightGray,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 0.dp, horizontal = 0.dp)
                        )

                        InfoRow(label = "Size", value = backupState.size)
                        InfoRow(label = "Last Modified", value = backupState.lastModified)
                    }
                }
            } else {
                // No Backup Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = appColors().container
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = appColors().text,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                "No Backup Found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = appColors().text
                            )
                            Text(
                                "Create your first backup to secure your data",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Backup Actions Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = appColors().container
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Backup Actions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500,
                        color = appColors().text
                    )

                    // Create/Update Backup Button
                    Button(
                        onClick = { viewModel.backup() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = syncState !is SyncState.Loading
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (backupState.exists) "Update Backup" else "Create Backup",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    // Restore Button (only if backup exists)
                    if (backupState.exists) {
                        Button(
                            onClick = { showRestoreDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = syncState !is SyncState.Loading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(
                                width = 0.5.dp,
                                color = appColors().text.copy(alpha = 0.8f)
                            )
                        ) {
                            Icon(
                                Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = appColors().text,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Restore from Backup",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = appColors().text
                            )
                        }

                        // Delete Backup Button
                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = syncState !is SyncState.Loading,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = appColors().red
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp,
                            ),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(
                                width = 0.5.dp,
                                color = appColors().red.copy(alpha = 0.8f)
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Delete Backup",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Account Actions Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = appColors().container
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.dp, horizontal = 8.dp)
                ) {
                    TextButton(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = appColors().red
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "Sign Out",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors().red
                        )
                    }
                }
            }

            // Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = appColors().container
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = appColors().green
                        )
                        Text(
                            "About Backup",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors().text
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoText("• Stored securely in Google Drive's app folder")
                        InfoText("• Only you can access this data")
                        InfoText("• Encrypted by Google automatically")
                        InfoText("• App will restart after restore")
                        InfoText("• Backup includes all your app data")
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            containerColor = appColors().background,
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = appColors().red,
                    modifier = Modifier
                        .size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Backup?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )
            },
            text = {
                Text(
                    text = "This will permanently delete your backup from Google Drive. This action cannot be undone.\n\nAre you sure you want to continue?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBackup()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().red
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().green
                    )
                }
            }
        )
    }

    // Restore Confirmation Dialog
    if (showRestoreDialog) {
        AlertDialog(
            containerColor = appColors().background,
            onDismissRequest = { showRestoreDialog = false },
            icon = {
                Icon(
                    Icons.Default.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp),
                    tint = appColors().green
                )
            },
            title = {
                Text(
                    text = "Restore from Backup?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = appColors().text
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This will replace your current data with the backup.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = appColors().text
                    )
                    Text(
                        text = "Are you sure you want to continue?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = appColors().text
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restore()
                        showRestoreDialog = false
                    }
                ) {
                    Text(
                        text = "Restore",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().green
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().red
                    )
                }
            }
        )
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            containerColor = appColors().background,
            onDismissRequest = { showSignOutDialog = false },
            icon = {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = appColors().text
                )
            },
            title = {
                Text(
                    "Sign Out?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W500,
                    color = appColors().text
                )
            },
            text = {
                Text(
                    "You will need to sign in again to create or restore backups.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.signOut()
                        showSignOutDialog = false
                    }
                ) {
                    Text(
                        text = "Sign Out",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500,
                        color = appColors().red,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W500,
                        color = appColors().red
                    )
                }
            }
        )
    }
}

@Composable
private fun BenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = appColors().green,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text,
            fontSize = 14.sp,
            color = appColors().text,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = appColors().text
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = appColors().text
        )
    }
}

@Composable
private fun InfoText(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = appColors().text
    )
}