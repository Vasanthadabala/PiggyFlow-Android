package com.piggylabs.piggyflow.navigation

interface Destinations{
    val route: String
}


object OnBoarding: Destinations{
    override val route = "OnBoarding"
}
object AccountType: Destinations{
    override val route = "AccountType"
}

/* Auth */

object LoginOptions: Destinations{
    override val route = "LoginOptions"
}
object SignIn: Destinations{
    override val route = "SignIn"
}

object SignUp: Destinations{
    override val route = "SignUp"
}

object Forgot: Destinations{
    override val route = "Forgot"
}


object Home: Destinations{
    override val route = "Home"
}

object AddData: Destinations{
    override val route = "AddData"
}

object TransactionDetail: Destinations{
    override val route = "TransactionDetail"
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