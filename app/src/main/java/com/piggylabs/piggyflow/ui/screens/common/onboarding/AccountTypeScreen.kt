package com.piggylabs.piggyflow.ui.screens.common.onboarding

import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.R
import com.piggylabs.piggyflow.navigation.LoginOptions
import com.piggylabs.piggyflow.navigation.OnBoarding
import com.piggylabs.piggyflow.navigation.components.TopBar
import com.piggylabs.piggyflow.ui.theme.appColors

enum class AccountType {
    PERSONAL,
    BUSINESS
}


@ExperimentalMaterial3Api
@Composable
fun AccountTypeScreen(navController: NavHostController){
    Scaffold (
        topBar = { TopBar(name = "Back", navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ){
            AccountTypeScreenComponent(navController)
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun AccountTypeScreenComponent(navController: NavHostController){
    val context = LocalContext.current

    //sharedPreferences
    val sharedPref = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val editor = sharedPref.edit()

    var selectedAccountType by remember { mutableStateOf(AccountType.PERSONAL) }

    val imageRes = when (selectedAccountType) {
        AccountType.PERSONAL -> R.drawable.account_type1
        AccountType.BUSINESS -> R.drawable.account_type2
    }


    /* Pager Text Effect */
    val messages = listOf(
        "Personal account",
        "Business account"
    )


    val currentPage = when (selectedAccountType) {
        AccountType.PERSONAL -> 0
        AccountType.BUSINESS -> 1
    }

    val pagerState = rememberPagerState (initialPage = 0, pageCount = { messages.size })

    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose your account",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = appColors().text,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Pick the flow that matches how you want to manage money in PiggyFlow.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(300.dp)
                .background(
                    color = appColors().container,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
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
                    val isSelected = currentPage == index
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

        Column(modifier = Modifier.fillMaxWidth()) {
            AccountTypeCard(
                title = "Personal",
                subtitle = "Budget tracking, income and expenses",
                selected = selectedAccountType == AccountType.PERSONAL,
                onClick = {
                    selectedAccountType = AccountType.PERSONAL
                }
            )

            AccountTypeCard(
                title = "Business",
                subtitle = "Party ledger, balances, reminders and business flow",
                selected = selectedAccountType == AccountType.BUSINESS,
                onClick = {
                    selectedAccountType = AccountType.BUSINESS
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {

                    val accountTypeValue = when (selectedAccountType) {
                        AccountType.PERSONAL -> "personal"
                        AccountType.BUSINESS -> "business"
                    }

                    editor.putString("account_type", accountTypeValue)
                    editor.apply()

                    navController.navigate(LoginOptions.route) {
                        popUpTo(OnBoarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 1.dp,
                    pressedElevation = 4.dp
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors().green,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AccountTypeCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                appColors().green.copy(alpha = 0.1f)
            else
                appColors().container
        ),
        border = if (selected)
            BorderStroke(
                2.dp,
                appColors().green
            )
        else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Radio indicator
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) appColors().green else Color.LightGray
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = appColors().text
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
