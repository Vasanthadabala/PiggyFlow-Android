package com.piggylabs.piggyflow.ui.screens.common.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.piggylabs.piggyflow.navigation.Forgot
import com.piggylabs.piggyflow.navigation.SignUp
import com.piggylabs.piggyflow.navigation.getPrimaryRoute
import com.piggylabs.piggyflow.navigation.components.TopBar
import com.piggylabs.piggyflow.ui.theme.appColors

@ExperimentalMaterial3Api
@Composable
fun SignInScreen(navController: NavHostController){
    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController) }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            SignInScreenComponent(navController = navController)
        }
    }
}

@Composable
fun SignInScreenComponent(navController: NavHostController){
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    //sharedPreferences
    val sharedPref = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val accountType = sharedPref.getString("account_type", "personal")
    val editor = sharedPref.edit()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome back",
            textAlign = TextAlign.Center,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = appColors().text
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Sign in to continue with your ${accountType ?: "personal"} flow.",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = appColors().container),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AuthField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                AuthField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    trailingContent = {
                        Text(
                            text = if (passwordVisible) "HIDE" else "SHOW",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { passwordVisible = !passwordVisible }
                        )
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Forgot your password?",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            navController.navigate(Forgot.route)
                        },
                        color = Color(0xff3f8efc)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

            /* Get Started Button */
        Button(
            onClick = {
                when {
                    email.isEmpty() -> {
                        Toast.makeText(context, "Provide Email", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    password.isEmpty() -> {
                        Toast.makeText(
                            context,
                            "Provide password",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                }

                isLoading = true

                loginWithEmail(
                    email = email,
                    password = password,
                    onSuccess = {

                        val user = FirebaseAuth.getInstance().currentUser
                        val uid = user?.uid

                        if (uid == null) {
                            isLoading = false
                            Toast.makeText(context, "User session error", Toast.LENGTH_SHORT)
                                .show()
                            return@loginWithEmail
                        }

                        fetchUserProfile(
                            uid = uid,
                            onSuccess = { profile ->
                                isLoading = false

                                Log.d("PROFILE", "UID: $uid")

                                val requestedType = (accountType ?: "personal").lowercase()
                                val registeredTypes = extractRegisteredAccountTypes(profile)
                                if (registeredTypes.contains(requestedType)) {
                                    val userNameFromDb = extractUserNameForType(
                                        profile = profile,
                                        requestedType = requestedType
                                    )
                                    Log.d("PROFILE", "Name[$requestedType]: $userNameFromDb")

                                    editor.putBoolean("is_logged_in", true)
                                    editor.putString("uid", uid)
                                    editor.putString("userName", userNameFromDb)
                                    editor.apply()

                                    restoreOrSyncAfterLogin(
                                        context = context,
                                        uid = uid,
                                        setSyncing = { syncing -> isLoading = syncing },
                                        onMessage = { message ->
                                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                    navController.navigate(getPrimaryRoute(context)) {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                } else {
                                    FirebaseAuth.getInstance().signOut()
                                    val accountLabel = requestedType.replaceFirstChar { it.uppercase() }
                                    val registeredLabel = registeredTypes.joinToString(", ") {
                                        it.replaceFirstChar { c -> c.uppercase() }
                                    }.ifBlank { "none" }
                                    Toast.makeText(
                                        context,
                                        "This email is not registered for $accountLabel account. Registered type: $registeredLabel.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            onError = { error ->
                                isLoading = false
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onError = { error ->
                        isLoading = false
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )
            },
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 2.dp,
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors().green,
                contentColor = Color.White
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = appColors().text,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Log In",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Create account",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = appColors().text
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Sign Up",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    navController.navigate(SignUp.route)
                },
                color = Color(0xff3f8efc)
            )
        }
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: (@Composable (() -> Unit))? = null
) {
    Column {
        Text(
            text = label.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = appColors().text
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = appColors().blue
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color.Transparent),
            shape = RoundedCornerShape(24.dp),
            trailingIcon = trailingContent
        )
    }
}

fun loginWithEmail(
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                onError(task.exception?.message ?: "Login failed")
            }
        }
}

fun fetchUserProfile(
    uid: String,
    onSuccess: (Map<String, Any>) -> Unit,
    onError: (String) -> Unit
) {
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .get()
        .addOnSuccessListener { doc ->
            if (doc.exists()) {
                onSuccess(doc.data ?: emptyMap())
            } else {
                onError("User profile not found")
            }
        }
        .addOnFailureListener { e ->
            onError(e.message ?: "Failed to fetch profile")
        }
}

private fun extractRegisteredAccountTypes(profile: Map<String, Any>): Set<String> {
    val legacyType = (profile["accountType"] as? String)?.lowercase()
    val existingTypes = (profile["accountTypes"] as? List<*>)
        ?.mapNotNull { it as? String }
        ?.map { it.lowercase() }
        ?.toMutableSet()
        ?: mutableSetOf()

    if (legacyType != null) {
        existingTypes.add(legacyType)
    }
    return existingTypes
}

private fun extractUserNameForType(
    profile: Map<String, Any>,
    requestedType: String
): String {
    val typedNames = profile["userNames"] as? Map<*, *>
    val typeSpecific = typedNames?.get(requestedType) as? String
    return typeSpecific ?: (profile["userName"] as? String ?: "")
}
