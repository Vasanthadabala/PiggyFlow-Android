package com.piggylabs.piggyflow.ui.screens.common.profile

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.piggylabs.piggyflow.navigation.components.TopBar
import com.piggylabs.piggyflow.ui.theme.appColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.vector.ImageVector

@ExperimentalMaterial3Api
@Composable
fun ProfileScreen(navController: NavHostController){
    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            ProfileScreenComponent(navController = navController)
        }
    }
}

@Composable
fun ProfileScreenComponent(navController: NavHostController){
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val sharedPreferences = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val savedUserName = sharedPreferences.getString("userName", "Guest").orEmpty()
    val accountType = sharedPreferences.getString("account_type", "personal").orEmpty()
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val email = firebaseUser?.email ?: "Not linked"

    var currentUserName by remember { mutableStateOf(savedUserName) }
    var name by remember { mutableStateOf(savedUserName) }
    var editMode by remember { mutableStateOf(false) }

    val accountLabel = accountType.replaceFirstChar { it.uppercase() }
    val memberSinceLabel = if (firebaseUser != null) "Synced account" else "Local account"

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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(78.dp)
                                    .background(Color.White.copy(alpha = 0.16f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUserName.trim().firstOrNull()?.uppercase() ?: "P",
                                    color = Color.White,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            ProfileChip(label = accountLabel)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = currentUserName,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = email,
                                color = Color.White.copy(alpha = 0.86f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = memberSinceLabel,
                                color = Color.White.copy(alpha = 0.72f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Account",
                    value = accountLabel
                )
                ProfileMetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Status",
                    value = if (firebaseUser != null) "Connected" else "Offline"
                )
            }
        }

        item {
            Text(
                text = "Account Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors().text
            )
        }

        item {
            EditableDetailCard(
                title = "Display Name",
                value = currentUserName,
                draftValue = name,
                icon = Icons.Default.Person,
                isEditing = editMode,
                onValueChange = { name = it },
                onEditToggle = {
                    name = currentUserName
                    editMode = true
                },
                onCancel = {
                    name = currentUserName
                    editMode = false
                    keyboardController?.hide()
                },
                onSave = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Provide username", Toast.LENGTH_SHORT).show()
                    } else {
                        currentUserName = name.trim()
                        editor.putString("userName", currentUserName)
                        editor.apply()
                        editMode = false
                        keyboardController?.hide()
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        item {
            DetailCard(
                title = "Email",
                value = email,
                icon = Icons.Default.Badge
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun ProfileChip(label: String) {
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
private fun ProfileMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors().text
            )
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    value: String,
    icon: ImageVector,
    trailingLabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(18.dp),
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
                        .size(40.dp)
                        .background(appColors().green.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = appColors().green,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = value,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().text
                    )
                }
            }

            if (trailingLabel != null) {
                Text(
                    text = trailingLabel,
                    color = appColors().green,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(appColors().green.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = appColors().green,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        if (!isEditing) {
                            Text(
                                text = value,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = appColors().text
                            )
                        }
                    }
                }

                if (isEditing) {
                    Row {
                        IconButton(onClick = onCancel) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = Color.Gray
                            )
                        }
                        IconButton(onClick = onSave) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save",
                                tint = appColors().green
                            )
                        }
                    }
                } else {
                    IconButton(onClick = onEditToggle) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit display name",
                            tint = appColors().green
                        )
                    }
                }
            }

            if (isEditing) {
                OutlinedTextField(
                    value = draftValue,
                    singleLine = true,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Your Name",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W500,
                                color = Color.Gray,
                                textAlign = TextAlign.Start
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = appColors().green,
                        unfocusedIndicatorColor = Color.Gray,
                        focusedContainerColor = appColors().background,
                        unfocusedContainerColor = appColors().background,
                        cursorColor = appColors().text
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSave() }),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp,
                        color = appColors().text
                    )
                )
            }
        }
    }
}
