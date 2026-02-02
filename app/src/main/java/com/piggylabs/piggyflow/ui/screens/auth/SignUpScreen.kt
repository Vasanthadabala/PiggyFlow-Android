package com.piggylabs.piggyflow.ui.screens.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
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
import com.piggylabs.piggyflow.R
import com.piggylabs.piggyflow.navigation.SignIn
import com.piggylabs.piggyflow.navigation.SignUp
import com.piggylabs.piggyflow.navigation.Sync
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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Sign Up",
            textAlign = TextAlign.Center,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = appColors().text
        )

        Column(
            modifier = Modifier.padding(4.dp)
        ) {

            Text(
                text = "User name",
                fontSize = 14.sp,
                color = appColors().text,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.Transparent),
                shape = RoundedCornerShape(24),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.LightGray,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = appColors().blue
                ),
                textStyle = TextStyle(
                    fontWeight = FontWeight.W500,
                    fontSize = 18.sp,
                    color = appColors().text
                )
            )
        }

        Column(
            modifier = Modifier.padding(4.dp)
        ) {

            Text(
                text = "Email",
                fontSize = 14.sp,
                color = appColors().text,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.Transparent),
                shape = RoundedCornerShape(24),
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
                textStyle = TextStyle(
                    fontWeight = FontWeight.W500,
                    fontSize = 18.sp,
                    color = appColors().text
                )
            )
        }

        Column(
            modifier = Modifier.padding(4.dp)
        ) {

            Text(
                text = "Password",
                fontSize = 14.sp,
                color = appColors().text,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.Transparent),
                shape = RoundedCornerShape(24),
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.LightGray,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = appColors().blue
                ),
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
                textStyle = TextStyle(
                    fontWeight = FontWeight.W500,
                    fontSize = 18.sp,
                    color = appColors().text
                )
            )
        }

        Column(
            modifier = Modifier.padding(4.dp)
        ) {

            Text(
                text = "Confirm password",
                fontSize = 14.sp,
                color = appColors().text,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Confirm password",
                        fontSize = 16.sp,
                        color = Color.Black.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.Transparent),
                shape = RoundedCornerShape(24),
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
                textStyle = TextStyle(
                    fontWeight = FontWeight.W500,
                    fontSize = 18.sp,
                    color = appColors().text
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.weight(1f))


        Text(
            text = "By tapping \"Agree and continue\" below, you agree to the Terms of Service and acknowledge that you have read the Privacy Policy.",
            fontSize = 14.sp,
            color = appColors().text,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                .imePadding()
                .padding(horizontal = 0.dp),
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

        Spacer(modifier = Modifier.height(16.dp))
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
    val accountType = sharedPref.getString("account_type", "personal") // default


    auth.createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { result ->

            val user = result.user ?: run {
                onError("User is null")
                return@addOnSuccessListener
            }

            val userData = hashMapOf(
                "uid" to user.uid,
                "userName" to userName,
                "email" to email,
                "accountType" to accountType,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("FIRESTORE", "Save failed", e)
                    onError(e.message ?: "Firestore write failed")
                }
        }
        .addOnFailureListener { e ->
            onError(e.message ?: "Signup failed")
        }
}