package com.piggylabs.piggyflow.ui.screens.common.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.piggylabs.piggyflow.navigation.components.TopBar
import com.piggylabs.piggyflow.ui.theme.appColors

@ExperimentalMaterial3Api
@Composable
fun AboutScreen(navController: NavHostController){
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName.orEmpty()
    val versionCode = packageInfo.longVersionCode

    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            appColors().green.copy(alpha = 0.12f),
                            appColors().background,
                            appColors().background
                        )
                    )
                )
                .padding(innerPadding)
        ){
            AboutScreenComponent(versionName = versionName, versionCode = versionCode)
        }
    }
}

@Composable
fun AboutScreenComponent(
    versionName: String,
    versionCode: Long
){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = appColors().container
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.onboarding_image),
                        contentDescription = "PiggyFlow logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(92.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text ="PiggyFlow",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors().text
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text ="Track smarter. Spend better.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = appColors().container)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Build Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors().text
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LabelValueRow(label = "Version", value = versionName.ifBlank { "N/A" })
                    Spacer(modifier = Modifier.height(6.dp))
                    LabelValueRow(label = "Developer", value = "Vasanth")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = appColors().container)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "What PiggyFlow Helps With",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors().text
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FeatureRow(iconTint = appColors().green, icon = { Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null) }, text = "Tracker alerts to stay on top of repayments")
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureRow(iconTint = appColors().green, icon = { Icon(imageVector = Icons.Default.CloudDone, contentDescription = null) }, text = "Optional Google backup and sync when you need it")
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureRow(iconTint = appColors().green, icon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) }, text = "Local-first design with user-controlled data sync")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Your data stays on your device unless you explicitly enable sync.",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text ="Copyright 2026 Vasanth",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun LabelValueRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = appColors().text,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FeatureRow(
    iconTint: Color,
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(18.dp)) {
                icon()
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = appColors().text
        )
    }
}
