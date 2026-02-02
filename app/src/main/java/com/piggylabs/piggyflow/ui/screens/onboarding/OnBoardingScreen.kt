package com.piggylabs.piggyflow.ui.screens.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.R
import com.piggylabs.piggyflow.navigation.AccountType
import com.piggylabs.piggyflow.navigation.SignIn
import com.piggylabs.piggyflow.ui.theme.appColors
import kotlinx.coroutines.delay

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
                navController.navigate(AccountType.route)
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}