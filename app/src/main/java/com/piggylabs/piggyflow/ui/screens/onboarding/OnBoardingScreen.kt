package com.piggylabs.piggyflow.ui.screens.onboarding

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.R
import com.piggylabs.piggyflow.navigation.Home
import com.piggylabs.piggyflow.ui.theme.appColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@Composable
fun OnBoardingScreen(navController: NavHostController){
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ){
            OnBoardingScreenComponent(navController)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun OnBoardingScreenComponent(navController: NavHostController){
    val context = LocalContext.current

    val sharedPreferences = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("") }

    var showUserNameBottomSheet by remember { mutableStateOf(false) }
    val userNameBottomSheetState  =  rememberModalBottomSheetState( skipPartiallyExpanded = true)

    var isLoading by remember { mutableStateOf(false) }


    /* Pager Text Effect */
    val messages = listOf(
        "Track your daily expenses easily",
        "Visualize your spending habits",
        "Save smarter & grow financially"
    )

    val pagerState = rememberPagerState { messages.size }

    /* Pager Text */
    LaunchedEffect(Unit) {
        while(true){
            delay(1000L)
            val nextPage = (pagerState.currentPage +  1) % messages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }



    /* Bottom Sheet */
    LaunchedEffect(userNameBottomSheetState.isVisible) {
        if (!userNameBottomSheetState.isVisible){
            showUserNameBottomSheet = false
        }
    }


    /* Wallet drop animation */
    val walletOffsetY = remember { Animatable(-400f) }

    LaunchedEffect(Unit) {

        delay(100L)

        // Wallet drop
        walletOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .size(300.dp)
                .graphicsLayer {
                    translationY = walletOffsetY.value
                }
                .background(
                    color = appColors().container,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding_image),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(250.dp)
                    .clip(shape = CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) { page ->
                Text(
                    text = messages[page],
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = appColors().text
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dots indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(messages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) appColors().green else Color.LightGray
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                showUserNameBottomSheet = true
            },
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 1.dp,
                pressedElevation = 4.dp
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors().green,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(),
                color = Color.White
            )
        }
    }

    if (showUserNameBottomSheet){
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    userNameBottomSheetState.hide()
                }
            },
            sheetState = userNameBottomSheetState,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            containerColor = appColors().background
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                            .clickable {
                                scope.launch {
                                    userNameBottomSheetState.hide()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(32.dp)
                            .size(280.dp)
                            .background(
                                color = appColors().container,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.onboarding_image),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(240.dp)
                                .clip(shape = CircleShape)
                        )
                    }

                    Column {

                        Text(
                            text = "Enter Your Name",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = appColors().text
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = userName,
                                singleLine = true,
                                onValueChange = { userName = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
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
                                shape = RoundedCornerShape(20),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = appColors().container,
                                    unfocusedContainerColor = appColors().container,
                                    cursorColor = Color.DarkGray
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done // Use Done for the last field
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                    }
                                ),
                                leadingIcon = {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color.Gray
                                        )
                                    }
                                },
                                textStyle = TextStyle(
                                    fontWeight = FontWeight.W500,
                                    fontSize = 16.sp,
                                    color = appColors().text
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if(userName.isEmpty()){
                                    Toast.makeText(context, "Provide User Name", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                scope.launch {
                                    isLoading = true

                                    try {
                                        editor.putString("userName", userName)
                                        editor.putBoolean("is_logged_in", true)
                                        editor.apply()

                                        delay(200L)

                                        navController.navigate(Home.route){
                                            popUpTo(navController.graph.id){
                                                inclusive = true
                                            }
                                        }
                                    }catch (e:Exception){
                                        Log.d("AccountSection", "Error: $e")
                                    }finally {
                                        delay(800L)
                                        isLoading = false
                                    }
                                }
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
                            enabled = !isLoading,
                        ) {
                            if (isLoading) {
                                // Show a loading spinner when the button is disabled
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "Lets Go",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.W500,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}