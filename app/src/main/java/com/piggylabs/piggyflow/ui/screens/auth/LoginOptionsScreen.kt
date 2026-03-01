package com.piggylabs.piggyflow.ui.screens.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.piggylabs.piggyflow.R
import com.piggylabs.piggyflow.auth.firebaseAuthWithGoogle
import com.piggylabs.piggyflow.navigation.Home
import com.piggylabs.piggyflow.navigation.SignIn
import com.piggylabs.piggyflow.navigation.SignUp
import com.piggylabs.piggyflow.navigation.components.TopBar
import com.piggylabs.piggyflow.ui.theme.appColors

@ExperimentalMaterial3Api
@Composable
fun LoginOptionsScreen(navController: NavHostController){
    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            LoginOptionsScreenComponent(navController = navController)
        }

    }
}

@Composable
fun LoginOptionsScreenComponent(navController: NavHostController) {
    val context = LocalContext.current

    //sharedPreferences
    val sharedPref = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()
    val accountType = sharedPref.getString("account_type", "personal")

    val imageRes = when (accountType) {
        "personal" -> R.drawable.account_type1
        "business" -> R.drawable.account_type2
        else -> R.drawable.account_type1
    }

    /* ---------------- SKIP BUTTON ANIMATION ---------------- */

    val infiniteTransition = rememberInfiniteTransition(label = "skip_anim")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 700,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow"
    )

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

                        navController.navigate(Home.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    onFailure = { error ->
                        FirebaseAuth.getInstance().signOut()
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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
        )

        AuthCard(
            onLogin = {
                navController.navigate(SignIn.route)
            },
            onRegister = {
                navController.navigate(SignUp.route)
            },
            onGoogleLogin = {
                if (isGoogleSigningIn) return@AuthCard
                isGoogleSigningIn = true
                googleSignInClient.signOut().addOnCompleteListener {
                    googleLauncher.launch(googleSignInClient.signInIntent)
                }.addOnFailureListener {
                    Log.e("FIRESTORE", "Failed to clear old Google session", it)
                    googleLauncher.launch(googleSignInClient.signInIntent)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable {
                    editor.putBoolean("is_logged_in", true)
                    editor.apply()

                    navController.navigate(Home.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = appColors().green,
                contentColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skip",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardDoubleArrowRight,
                    contentDescription = "Skip",
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            translationX = arrowOffset
                        }
                )
            }
        }
    }

}

@Composable
fun AuthCard(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onGoogleLogin: () -> Unit
) {

    Card(
        shape = RoundedCornerShape(
            topStart = 32.dp,
            bottomStart = 32.dp,
            topEnd = 32.dp,
            bottomEnd = 32.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Welcome",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // Login Button
            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors().green,
                    contentColor = Color.White
                ),
            ) {
                Text(
                    text = "Login",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            // Register Button
            OutlinedButton(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Register",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = appColors().text,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            // Google Button (Spotify style)
            OutlinedButton(
                onClick = onGoogleLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = appColors().text,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
