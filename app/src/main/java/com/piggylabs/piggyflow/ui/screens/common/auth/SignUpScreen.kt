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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.piggylabs.piggyflow.navigation.SignIn
import com.piggylabs.piggyflow.navigation.SignUp
import com.piggylabs.piggyflow.navigation.components.TopBar
import com.piggylabs.piggyflow.ui.theme.appColors

@ExperimentalMaterial3Api
@Composable
fun SignUpScreen(navController: NavHostController){
    var step by rememberSaveable { mutableIntStateOf(1) }

    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            SignUpScreenComponent(navController = navController, step = step, onStepChange = { step = it })
        }
    }
}

@Composable
fun SignUpScreenComponent(navController: NavHostController, step: Int, onStepChange: (Int) -> Unit) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create your account",
            textAlign = TextAlign.Center,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = appColors().text
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Set up your PiggyFlow account to start tracking with the right account type.",
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
                SignUpField(
                    label = "User name",
                    value = userName,
                    onValueChange = { userName = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                SignUpField(
                    label = "Email",
                    value = email,
                    onValueChange = { email = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                SignUpField(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingContent = {
                        Text(
                            text = if (passwordVisible) "HIDE" else "SHOW",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    passwordVisible = !passwordVisible
                                }
                        )
                    }
                )

                SignUpField(
                    label = "Confirm password",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "By tapping \"Agree and continue\" below, you agree to the Terms of Service and acknowledge that you have read the Privacy Policy.",
            fontSize = 14.sp,
            color = appColors().text,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        /*Continue Button */
        Button(
            onClick = {
                when {
                    userName.isEmpty() -> {
                        Toast.makeText(context, "Provide Username", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    email.isEmpty() -> {
                        Toast.makeText(context, "Provide Email", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    password.length < 8 -> {
                        Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    password != confirmPassword -> {
                        Toast.makeText(context, "Passwords not matching", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                }

                isLoading = true

                signUpWithEmail(
                    context = context,
                    userName = userName,
                    email = email,
                    password = password,
                    onSuccess = {
                        isLoading = false
                        Toast.makeText(context, "Account created successfully. Please sign in.", Toast.LENGTH_LONG).show()

                        navController.navigate(SignIn.route){
                            popUpTo(SignUp.route){
                                inclusive = true
                            }
                        }
                    },
                    onError = { error ->
                        isLoading = false
                        Log.e("SIGNUP", error)
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                )


            },
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 2.dp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors().green,
                contentColor = Color.White
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                // Show a loading spinner when the button is disabled
                CircularProgressIndicator(
                    color = appColors().text,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Agree and continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account?",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = appColors().text
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Log In",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    navController.navigate(SignIn.route)
                },
                color = Color(0xff3f8efc)
            )
        }
    }
}

@Composable
private fun SignUpField(
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
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color.Transparent),
            shape = RoundedCornerShape(24.dp),
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
            trailingIcon = trailingContent,
            textStyle = TextStyle(
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                color = appColors().text
            )
        )
    }
}

fun signUpWithEmail(
    context: Context,
    userName:String,
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val sharedPref = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val accountType = sharedPref.getString("account_type", "personal")?.lowercase() ?: "personal"


    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { result ->

            val user = result.user ?: run {
                onError("User is null")
                return@addOnSuccessListener
            }

            val userData = hashMapOf(
                "uid" to user.uid,
                "userName" to userName,
                "userNames" to mapOf(accountType to userName),
                "email" to email,
                "accountType" to accountType,
                "accountTypes" to listOf(accountType),
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    auth.signOut()
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("FIRESTORE", "Save failed", e)
                    onError(e.message ?: "Firestore write failed")
                }
        }
        .addOnFailureListener { e ->
            if (e is FirebaseAuthUserCollisionException) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { signInResult ->
                        val existingUser = signInResult.user ?: run {
                            onError("User session error")
                            return@addOnSuccessListener
                        }

                        firestore.collection("users")
                            .document(existingUser.uid)
                            .get()
                            .addOnSuccessListener { doc ->
                                val existingTypes = (doc.get("accountTypes") as? List<*>)
                                    ?.mapNotNull { it as? String }
                                    ?.map { it.lowercase() }
                                    ?.toMutableSet()
                                    ?: mutableSetOf()

                                doc.getString("accountType")
                                    ?.lowercase()
                                    ?.let(existingTypes::add)

                                existingTypes.add(accountType)

                                val updates = hashMapOf<String, Any>(
                                    "uid" to existingUser.uid,
                                    "email" to (existingUser.email ?: email),
                                    "accountType" to accountType,
                                    "accountTypes" to existingTypes.toList(),
                                    "userNames.$accountType" to userName,
                                    "updatedAt" to System.currentTimeMillis()
                                )
                                if (doc.getString("userName").isNullOrBlank()) {
                                    updates["userName"] = userName
                                }

                                firestore.collection("users")
                                    .document(existingUser.uid)
                                    .set(updates, SetOptions.merge())
                                    .addOnSuccessListener {
                                        auth.signOut()
                                        onSuccess()
                                    }
                                    .addOnFailureListener { updateError ->
                                        onError(updateError.message ?: "Failed to enable account type")
                                    }
                            }
                            .addOnFailureListener { fetchError ->
                                onError(fetchError.message ?: "Failed to fetch user profile")
                            }
                    }
                    .addOnFailureListener {
                        onError("Email already in use. Log in with your password to add this account type.")
                    }
            } else {
                onError(e.message ?: "Signup failed")
            }
        }
}
