package com.piggylabs.piggyflow.features.scanbill.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.piggylabs.piggyflow.core.navigation.AddExpense
import com.piggylabs.piggyflow.core.navigation.Transactions

/**
 * Capture screen for the Scan Bill flow. The viewfinder is a design placeholder —
 * there is no camera dependency in the project yet, so nothing is previewed or
 * captured; the frame, controls and tips are the finished UI around that gap.
 */

private val ViewfinderBackground = Color(0xFF23282A)
private val ControlBackground = Color(0xCC1B1F21)

private data class TipData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color
)

private val TIPS = listOf(
    TipData(
        title = "Good Lighting",
        description = "Use natural light for best results",
        icon = Icons.Default.WbSunny,
        iconTint = Color(0xFFF5A623),
        iconBackground = Color(0xFFFDF1DC)
    ),
    TipData(
        title = "Flat Surface",
        description = "Place bill on a flat surface",
        icon = Icons.Default.Description,
        iconTint = Color(0xFF15803D),
        iconBackground = Color(0xFFE3F2E7)
    ),
    TipData(
        title = "Avoid Blur",
        description = "Hold steady while capturing",
        icon = Icons.Default.CenterFocusStrong,
        iconTint = Color(0xFF3B82F6),
        iconBackground = Color(0xFFE4EEFD)
    ),
    TipData(
        title = "Fit in Frame",
        description = "Make sure the entire bill is inside",
        icon = Icons.Default.Crop,
        iconTint = Color(0xFF15803D),
        iconBackground = Color(0xFFE3F2E7)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanBillScreen(navController: NavHostController) {
    val colors = appColors()
    var isMultiMode by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.width(58.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.textMuted.copy(alpha = 0.2f), CircleShape)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.text,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scan Bill",
                        fontSize = 21.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Secure & Private",
                            fontSize = 13.sp,
                            lineHeight = 15.sp,
                            color = colors.textMuted
                        )
                    }
                }

                Column(
                    modifier = Modifier.width(58.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.textMuted.copy(alpha = 0.2f), CircleShape)
                            .clickable { navController.navigate(Transactions.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Scan history",
                            tint = colors.text,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "History",
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.text
                    )
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CaptureSideButton(
                    icon = Icons.Default.CloudUpload,
                    label = "Upload Bill",
                    onClick = { navController.navigate(AddExpense.route) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(12.dp))

                // Shutter: visual only until a camera pipeline exists.
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(colors.accent.copy(alpha = 0.18f))
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(colors.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture bill",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                CaptureSideButton(
                    icon = Icons.Default.Edit,
                    label = "Enter Manually",
                    onClick = { navController.navigate(AddExpense.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. VIEWFINDER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .background(ViewfinderBackground)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 14.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ControlBackground)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Align bill within the frame",
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Corner brackets marking the capture area.
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        // Insets reserve the hint pill above and the mode toggle below.
                        .padding(top = 50.dp, bottom = 58.dp)
                        .width(224.dp)
                        .height(218.dp)
                ) {
                    FrameCorner(Alignment.TopStart)
                    FrameCorner(Alignment.TopEnd)
                    FrameCorner(Alignment.BottomStart)
                    FrameCorner(Alignment.BottomEnd)

                    Text(
                        text = "Camera preview",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ViewfinderControl(icon = Icons.Default.FlashOn, label = "Flash")
                    ViewfinderControl(label = "Auto", lettermark = "A")
                    ViewfinderControl(icon = Icons.Default.PhotoLibrary, label = "Gallery")
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .padding(4.dp)
                ) {
                    Row {
                        CaptureModeChip(
                            label = "Single",
                            selected = !isMultiMode,
                            onClick = { isMultiMode = false }
                        )
                        CaptureModeChip(
                            label = "Multi",
                            selected = isMultiMode,
                            onClick = { isMultiMode = true }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 60.dp)
                ) {
                    ViewfinderControl(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        label = "Help"
                    )
                }
            }

            // 2. AI SUMMARY + TIPS
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.accentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.onAccentSoft,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI will extract details",
                                fontSize = 16.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "We'll scan and extract merchant, items, amount and date for you.",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = colors.textMuted
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Tips for best results",
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TIPS.forEach { tip ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(140.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 6.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(tip.iconBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tip.icon,
                                        contentDescription = null,
                                        tint = tip.iconTint,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = tip.title,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.text,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = tip.description,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp,
                                    color = colors.textMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/** One L-shaped corner of the capture frame, anchored to [corner]. */
@Composable
private fun BoxScope.FrameCorner(corner: Alignment) {
    val isTop = corner == Alignment.TopStart || corner == Alignment.TopEnd
    val isStart = corner == Alignment.TopStart || corner == Alignment.BottomStart
    val thickness = 3.dp
    val arm = 34.dp

    Box(modifier = Modifier.align(corner)) {
        Box(
            modifier = Modifier
                .align(if (isTop) Alignment.TopCenter else Alignment.BottomCenter)
                .width(arm)
                .height(thickness)
                .background(Color.White, RoundedCornerShape(2.dp))
        )
        Box(
            modifier = Modifier
                .align(if (isStart) Alignment.CenterStart else Alignment.CenterEnd)
                .width(thickness)
                .height(arm)
                .background(Color.White, RoundedCornerShape(2.dp))
        )
    }
}

/** Dark round control on the viewfinder: an icon (or letter) above its label. */
@Composable
private fun ViewfinderControl(
    label: String,
    icon: ImageVector? = null,
    lettermark: String? = null
) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(ControlBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )

                lettermark != null -> Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lettermark,
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CaptureModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = appColors()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 26.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) colors.accent else Color(0xFF4B5563),
            fontSize = 14.sp,
            lineHeight = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun CaptureSideButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = colors.textMuted.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.text,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
                maxLines = 1
            )
        }
    }
}
