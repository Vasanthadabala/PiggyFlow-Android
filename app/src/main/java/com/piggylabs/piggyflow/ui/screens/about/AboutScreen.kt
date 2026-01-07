package com.piggylabs.piggyflow.ui.screens.about


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.R
import com.piggylabs.piggyflow.navigation.components.TopBar
import com.piggylabs.piggyflow.ui.theme.appColors

@ExperimentalMaterial3Api
@Composable
fun AboutScreen(navController: NavHostController){
    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ){
            AboutScreenComponent()
        }
    }
}

@Composable
fun AboutScreenComponent(){
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = CircleShape
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding_image),
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(96.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text ="PiggyFlow",
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors().text
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text ="Smart Expense Tracker",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = appColors().text
        )
        Spacer(modifier = Modifier.height(16.dp))

        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text ="Developed by",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text ="Vasanth Adabala",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors().text
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text ="Copyright 2025 Vasanth",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray
        )


    }
}