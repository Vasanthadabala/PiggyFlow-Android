package com.piggylabs.piggyflow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.House
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavigationItem(
    val title: String,
    val icon: ImageVector,
    val route:String
)

val bottomBarItems = listOf(
    BottomNavigationItem(
        title = "Home",
        icon = Icons.Rounded.House,
        route = Home.route
    ),
    BottomNavigationItem(
        title = "Stats",
        icon = Icons.Rounded.Insights,
        route = Stats.route
    ),
    BottomNavigationItem(
        title = "Settings",
        icon = Icons.Rounded.Settings,
        route = Settings.route
    ),
)