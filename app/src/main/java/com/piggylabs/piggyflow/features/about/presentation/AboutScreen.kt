package com.piggylabs.piggyflow.features.about.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.R
import com.piggylabs.piggyflow.core.navigation.TopBar
import com.piggylabs.piggyflow.core.designsystem.theme.appColors

@ExperimentalMaterial3Api
@Composable
fun AboutScreen(navController: NavHostController){
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName.orEmpty()

    Scaffold(
        topBar = { TopBar(name = "About PiggyFlow", navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ){
            AboutScreenComponent(versionName = versionName)
        }
    }
}

@Composable
fun AboutScreenComponent(versionName: String){
    val context = LocalContext.current
    val colors = appColors()
    var infoMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // HERO
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_image),
                    contentDescription = "PiggyFlow logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "PiggyFlow", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.text)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Scan. Track. Save. Grow.", fontSize = 14.sp, color = colors.textMuted)
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.Surface(shape = RoundedCornerShape(14.dp), color = colors.accentSoft) {
                    Text(
                        text = "Version $versionName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onAccentSoft,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // BLURB
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier.size(40.dp).background(colors.accentSoft, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = colors.onAccentSoft, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Your Finance, Your Control", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.text)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PiggyFlow helps you take control of your money with smart tracking, AI-powered insights and beautiful reports.",
                            fontSize = 13.sp,
                            color = colors.textMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // WHAT'S INSIDE
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "What's Inside", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.text)
                    Spacer(modifier = Modifier.height(14.dp))
                    FeatureRow(icon = Icons.Default.AutoAwesome, title = "Quick Bill Entry", subtitle = "Jump straight into adding an expense from a bill — automatic AI extraction is on the roadmap.")
                    Spacer(modifier = Modifier.height(14.dp))
                    FeatureRow(icon = Icons.Default.Description, title = "Smart Reports", subtitle = "Detailed insights and beautiful reports to understand your money better.")
                    Spacer(modifier = Modifier.height(14.dp))
                    FeatureRow(icon = Icons.Default.TrackChanges, title = "Goals & Budgets", subtitle = "Set goals, create budgets and achieve your financial milestones.")
                    Spacer(modifier = Modifier.height(14.dp))
                    FeatureRow(icon = Icons.Default.CloudDone, title = "Cloud Sync", subtitle = "Secure backup and sync across all your devices.")
                }
            }
        }

        // MISSION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.accentSoft),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Built with ❤️ for better financial habits", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.onAccentSoft)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "We're on a mission to make personal finance simple, clear and empowering for everyone.",
                        fontSize = 13.sp,
                        color = colors.onAccentSoft,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // LINKS
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                LinkRow(
                    icon = Icons.Default.CardGiftcard,
                    label = "What's New",
                    onClick = {
                        infoMessage = "Version $versionName brings the redesigned Home, Expenses, Tracker and Reports screens, plus a refreshed Profile."
                    }
                )
                LinkRow(
                    icon = Icons.Default.StarRate,
                    label = "Rate PiggyFlow",
                    onClick = {
                        val packageName = context.packageName
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                        } catch (e: ActivityNotFoundException) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                        }
                    }
                )
                LinkRow(
                    icon = Icons.Default.Shield,
                    label = "Privacy Policy",
                    onClick = { infoMessage = "A hosted privacy policy page is coming soon." }
                )
                LinkRow(
                    icon = Icons.Default.Gavel,
                    label = "Terms of Service",
                    onClick = { infoMessage = "Hosted terms of service are coming soon." }
                )
            }
        }

        if (infoMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceMuted)
                ) {
                    Text(
                        text = infoMessage.orEmpty(),
                        modifier = Modifier.padding(14.dp),
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "© 2026 PiggyFlow\nAll rights reserved.",
                fontSize = 12.sp,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, subtitle: String) {
    val colors = appColors()
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(38.dp).background(colors.accentSoft, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.onAccentSoft, modifier = Modifier.size(19.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
            Text(text = subtitle, fontSize = 12.sp, color = colors.textMuted, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun LinkRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = appColors()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.text)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
