package com.piggylabs.piggyflow.core.navigation

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

object BusinessHome: Destinations{
    override val route = "BusinessHome"
}

object BusinessPartyDetail: Destinations{
    override val route = "BusinessPartyDetail"
    const val partyId = "partyId"
}

object AddData: Destinations{
    override val route = "AddData"
    const val type = "type"

    /** Full route pattern; [type] is an optional query param so bare navigation
     * still works and defaults to "Expense". */
    val routePattern = "$route?$type={$type}"

    fun forType(type: String) = "$route?${AddData.type}=$type"
}

/** Dedicated income entry form. */
object AddIncome: Destinations{
    override val route = "AddIncome"
}

/** Dedicated expense entry form. */
object AddExpense: Destinations{
    override val route = "AddExpense"
}

object AddOptions: Destinations{
    override val route = "AddOptions"
}

object TransactionDetail: Destinations{
    override val route = "TransactionDetail"
    const val type = "type"
    const val listID = "listID"
}

object Stats: Destinations{
    override val route = "Stats"
}

object Tracker: Destinations{
    override val route = "Tracker"
}

object BudgetGoals: Destinations{
    override val route = "BudgetGoals"
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

object Accounts: Destinations{
    override val route = "Accounts"
}

object ScanBill: Destinations{
    override val route = "ScanBill"
}

object AddTracker: Destinations{
    override val route = "AddTracker"
    const val type = "type"

    /** Which of the four tracker forms opens first; defaults to Budget. */
    val routePattern = "$route?$type={$type}"

    fun forType(type: String) = "$route?${AddTracker.type}=$type"
}

object AddAccount: Destinations{
    override val route = "AddAccount"
    const val accountId = "accountId"

    /** Full route pattern; [accountId] is optional so the screen doubles as an editor. */
    val routePattern = "$route?$accountId={$accountId}"

    fun editRoute(id: Int) = "$route?$accountId=$id"
}

object Transactions: Destinations{
    override val route = "Transactions"
}

object Expenses: Destinations{
    override val route = "Expenses"
}

object Appearance: Destinations{
    override val route = "Appearance"
}

/** Generic destination for Profile rows that are either a static single-value
 * display (Currency, Language, Help Center) or an honest "Coming soon" page
 * (Security, Payment Methods, Premium Membership) — see [com.piggylabs.piggyflow.features.profile.presentation.InfoScreen]. */
object InfoScreen: Destinations{
    override val route = "InfoScreen"
    const val topic = "topic"
    val routePattern = "$route/{$topic}"

    fun forTopic(topic: String) = "$route/$topic"
}