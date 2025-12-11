package com.piggylabs.piggyflow.ui.navigation

interface Destinations{
    val route: String
}

object OnBoarding: Destinations{
    override val route = "OnBoarding"
}

object Home: Destinations{
    override val route = "Home"
}

object AddData: Destinations{
    override val route = "AddData"
}

object ListDataDetails: Destinations{
    override val route = "ListDataDetails"
    const val type = "type"
    const val listID = "listID"
}

object Stats: Destinations{
    override val route = "Stats"
}

object Settings: Destinations{
    override val route = "Settings"
}

object Profile: Destinations{
    override val route = "Profile"
}

object Sync: Destinations{
    override val route = "Sync"
}

object About: Destinations{
    override val route = "About"
}



object Notification: Destinations{
    override val route = "Notification"
}