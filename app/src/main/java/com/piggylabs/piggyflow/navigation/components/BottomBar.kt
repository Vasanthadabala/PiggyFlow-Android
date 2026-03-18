package com.piggylabs.piggyflow.navigation.components

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.navigation.getAccountType
import com.piggylabs.piggyflow.navigation.getBottomBarItems
import com.piggylabs.piggyflow.ui.theme.appColors

@Composable
fun BottomBar(navController: NavHostController){
    val context = LocalContext.current
    val bottomBarItems = getBottomBarItems(getAccountType(context))
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
    val selectedItemIndex = bottomBarItems.indexOfFirst { it.route == currentRoute }

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
