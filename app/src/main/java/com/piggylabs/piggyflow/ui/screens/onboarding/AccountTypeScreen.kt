package com.piggylabs.piggyflow.ui.screens.onboarding

import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.piggylabs.piggyflow.navigation.SignIn
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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(320.dp)
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
                    .size(280.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

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

        AccountTypeCard(
            title = "Personal",
            selected = selectedAccountType == AccountType.PERSONAL,
            onClick = {
                selectedAccountType = AccountType.PERSONAL
            }
        )

        AccountTypeCard(
            title = "Business",
            selected = selectedAccountType == AccountType.BUSINESS,
            onClick = {
                selectedAccountType = AccountType.BUSINESS
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

                val accountTypeValue = when (selectedAccountType) {
                    AccountType.PERSONAL -> "personal"
                    AccountType.BUSINESS -> "business"
                }

                editor.putString("account_type", accountTypeValue)
                editor.apply()

                navController.navigate(SignIn.route)
            },
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 1.dp,
                pressedElevation = 4.dp
            ),
            modifier = Modifier
                .padding(horizontal = 0.dp),
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
    }
}

@Composable
fun AccountTypeCard(
    title: String,
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
            androidx.compose.foundation.BorderStroke(
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = appColors().text
            )
        }
    }
}