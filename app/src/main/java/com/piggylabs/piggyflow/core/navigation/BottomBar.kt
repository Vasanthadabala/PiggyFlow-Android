package com.piggylabs.piggyflow.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.navigation.AddExpense
import com.piggylabs.piggyflow.core.navigation.AddIncome
import com.piggylabs.piggyflow.core.navigation.AddOptions
import com.piggylabs.piggyflow.core.navigation.getAccountType
import com.piggylabs.piggyflow.core.navigation.getBottomBarItems
import com.piggylabs.piggyflow.core.designsystem.theme.appColors

/**
 * Personal mode shows 4 real destinations plus an inline Add action in the middle
 * (Home, Expenses, [Add], Tracker, Reports), matching the mockups. The Add slot
 * isn't a nav destination: it opens [AddOptions], and swaps to a close icon that pops
 * back when any of the add screens is already the current one.
 */
@Composable
fun BottomBar(navController: NavHostController){
    val context = LocalContext.current
    val bottomBarItems = getBottomBarItems(getAccountType(context))
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
    val selectedItemIndex = bottomBarItems.indexOfFirst { it.route == currentRoute }
    val isAddDataRoute = currentRoute == AddOptions.route ||
            currentRoute == AddIncome.route ||
            currentRoute == AddExpense.route
    val centerIndex = bottomBarItems.size / 2

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = appColors().background
        )
    ) {
        NavigationBar(
            tonalElevation = 0.dp,
            containerColor = appColors().background
        ) {
            bottomBarItems.forEachIndexed { index, item ->
                if (index == centerIndex && bottomBarItems.size > 3) {
                    AddNavigationItem(
                        isActive = isAddDataRoute,
                        onClick = {
                            if (isAddDataRoute) {
                                navController.popBackStack()
                            } else {
                                navController.navigate(AddOptions.route) { launchSingleTop = true }
                            }
                        }
                    )
                }

                NavigationBarItem(
                    selected = selectedItemIndex == index,
                    onClick = {
                        navController.navigate(item.route){
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.W500
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = ""
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color(0xFF38b000).copy(alpha = 0.25f),
                        selectedIconColor = appColors().text,
                        unselectedIconColor = Color.DarkGray,
                        selectedTextColor = appColors().text,
                        unselectedTextColor = Color.DarkGray,

                    ),
                    alwaysShowLabel = true
                )
            }
        }
    }
}

@Composable
private fun RowScope.AddNavigationItem(isActive: Boolean, onClick: () -> Unit) {
    NavigationBarItem(
        selected = false,
        onClick = onClick,
        icon = {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(appColors().accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (isActive) "Close" else "Add",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        label = null,
        alwaysShowLabel = false,
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent
        )
    )
}
