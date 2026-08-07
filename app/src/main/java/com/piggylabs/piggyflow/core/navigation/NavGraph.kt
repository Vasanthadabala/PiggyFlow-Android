package com.piggylabs.piggyflow.core.navigation

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.piggylabs.piggyflow.core.navigation.graphs.aboutScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.accountsScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.addAccountScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.addDataScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.addExpenseScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.addIncomeScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.addOptionsScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.appearanceScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.infoScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.budgetGoalsScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.businessHomeScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.businessPartyDetailScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.expensesScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.forgotScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.loginOptionsScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.signInScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.signUpScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.accountTypeSelectionScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.onBoardingScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.homeScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.notificationScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.profileScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.settingScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.statsScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.scanBillScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.addTrackerScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.trackerScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.transactionDetailScreenScreenGraph
import com.piggylabs.piggyflow.core.navigation.graphs.transactionsScreenGraph
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel
import com.piggylabs.piggyflow.core.di.preferencesManager

@ExperimentalMaterial3Api
@Composable
fun NavGraph(context: Context){
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = getStartDestination(context)){

        /* onBoarding */
        onBoardingScreenGraph(navController = navController)
        accountTypeSelectionScreenGraph(navController = navController)

        /* auth */
        loginOptionsScreenGraph(navController = navController)
        signInScreenGraph(navController = navController)
        signUpScreenGraph(navController = navController)
        forgotScreenGraph(navController = navController)

        /* home */
        homeScreenGraph(navController = navController, homeViewModel)
        expensesScreenGraph(navController = navController, homeViewModel)
        accountsScreenGraph(navController = navController)
        addAccountScreenGraph(navController = navController)
        businessHomeScreenGraph(navController = navController)
        businessPartyDetailScreenGraph(navController = navController)
        addDataScreenGraph(navController = navController, homeViewModel)
        addIncomeScreenGraph(navController = navController, homeViewModel)
        addExpenseScreenGraph(navController = navController, homeViewModel)
        addOptionsScreenGraph(navController = navController)
        scanBillScreenGraph(navController = navController)
        transactionDetailScreenScreenGraph(navController = navController, homeViewModel)
        transactionsScreenGraph(navController = navController, homeViewModel)

        /* profile */
        profileScreenGraph(navController = navController, homeViewModel)
        appearanceScreenGraph(navController = navController)
        infoScreenGraph(navController = navController)

        /* notification */
        notificationScreenGraph(navController = navController)

        /* stats */
        statsScreenGraph(navController = navController, homeViewModel)

        /* tracker */
        trackerScreenGraph(navController = navController)
        addTrackerScreenGraph(navController = navController)

        /* budget */
        budgetGoalsScreenGraph(navController = navController)

        /* settings */
        settingScreenGraph(navController = navController)
        aboutScreenGraph(navController = navController)

    }
}

/**
 * Resolved before the first frame, so this reads the preference store synchronously
 * rather than collecting it - the NavHost needs a start route up front.
 */
fun getStartDestination(context: Context): String {
    val prefs = preferencesManager(context).snapshotBlocking()
    return if (prefs.isLoggedIn) getPrimaryRoute(context) else OnBoarding.route
}
