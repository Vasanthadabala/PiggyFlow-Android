package com.piggylabs.piggyflow.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.House
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.ViewKanban
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavigationItem(
    val title: String,
    val icon: ImageVector,
    val route:String
)

private val personalBottomBarItems = listOf(
    BottomNavigationItem(
        title = "Home",
        icon = Icons.Rounded.House,
        route = Home.route
    ),
    BottomNavigationItem(
        title = "Tracker",
        icon = Icons.Rounded.ViewKanban,
        route = Tracker.route
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

private val businessBottomBarItems = listOf(
    BottomNavigationItem(
        title = "Business",
        icon = Icons.Rounded.Storefront,
        route = BusinessHome.route
    ),
    BottomNavigationItem(
        title = "Tracker",
        icon = Icons.Rounded.ViewKanban,
        route = Tracker.route
    ),
    BottomNavigationItem(
        title = "Settings",
        icon = Icons.Rounded.Settings,
        route = Settings.route
    ),
)

fun getBottomBarItems(accountType: String?): List<BottomNavigationItem> {
    return if (accountType == "business") businessBottomBarItems else personalBottomBarItems
}
