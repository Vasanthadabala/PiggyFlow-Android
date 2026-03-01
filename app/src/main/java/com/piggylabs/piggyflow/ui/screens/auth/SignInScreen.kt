package com.piggylabs.piggyflow.ui.screens.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.piggylabs.piggyflow.navigation.Home
import com.piggylabs.piggyflow.navigation.SignUp
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





    Box {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Log In",
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = appColors().text
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {

                Column {

                    Text(
                        text = "EMAIL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors().text
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.LightGray,
                            unfocusedIndicatorColor = Color.LightGray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = appColors().blue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(Color.Transparent),
                        shape = RoundedCornerShape(24),
                    )

                }

                Spacer(modifier = Modifier.height(16.dp))

                Column {

                    Text(
                        text = "PASSWORD",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = appColors().text
                        ),
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.LightGray,
                            unfocusedIndicatorColor = Color.LightGray,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = appColors().blue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(Color.Transparent),
                        shape = RoundedCornerShape(24),

                        trailingIcon = {
                            Text(
                                text = if (passwordVisible) "HIDE" else "SHOW",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clickable {
                                        passwordVisible = !passwordVisible
                                    }
                            )
                        },
                    )
                }

            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Forgot your password?",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable {
                        navController.navigate(Forgot.route)
                    },
                color = Color(0xff3f8efc)
            )

            Spacer(modifier = Modifier.height(32.dp))

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

                                    val userNameFromDb = profile["userName"] as? String
                                    val emailFromDb = profile["email"] as? String
                                    val accountTypeFromDb = profile["accountType"] as? String

                                    Log.d("PROFILE", "UID: $uid")
                                    Log.d("PROFILE", "Name: $userNameFromDb")
                                    Log.d("PROFILE", "Email: $emailFromDb")
                                    Log.d("PROFILE", "accountType: $accountTypeFromDb")

                                    // Optional: save locally
                                    editor.putBoolean("is_logged_in", true)
                                    editor.putString("uid", uid)
                                    editor.putString("userName", userNameFromDb)
                                    editor.apply()

                                    if (accountType == accountTypeFromDb) {
                                        navController.navigate(Home.route) {
                                            popUpTo(navController.graph.id) { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Wrong Account Type",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
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
                        text = "Log In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "Create account",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            navController.navigate(Forgot.route)
                        },
                    color = appColors().text
                )

                Spacer(modifier = Modifier.width(12.dp))


                Text(
                    text = "SignUp",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            navController.navigate(SignUp.route)
                        },
                    color = Color(0xff3f8efc)
                )
            }
        }
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