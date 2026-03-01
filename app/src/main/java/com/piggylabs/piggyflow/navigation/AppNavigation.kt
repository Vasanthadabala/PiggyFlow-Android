package com.piggylabs.piggyflow.navigation

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.piggylabs.piggyflow.navigation.graphs.aboutScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.addDataScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.auth.forgotScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.auth.loginOptionsScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.auth.signInScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.auth.signUpScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.dashboard.accountTypeSelectionScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.dashboard.onBoardingScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.homeScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.notificationScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.profileScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.settingScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.statsScreenGraph
import com.piggylabs.piggyflow.navigation.graphs.transactionDetailScreenScreenGraph
import com.piggylabs.piggyflow.ui.screens.home.viewmodel.HomeViewModel

@ExperimentalMaterial3Api
@Composable
fun AppNavigation(context: Context){
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = getStartDestination(context)){

        onBoardingScreenGraph(navController = navController)
        accountTypeSelectionScreenGraph(navController = navController)

        loginOptionsScreenGraph(navController = navController)
        signInScreenGraph(navController = navController)
        signUpScreenGraph(navController = navController)
        forgotScreenGraph(navController = navController)

        homeScreenGraph(navController = navController, homeViewModel)
        addDataScreenGraph(navController = navController, homeViewModel)
        transactionDetailScreenScreenGraph(navController = navController, homeViewModel)
        profileScreenGraph(navController = navController)
        notificationScreenGraph(navController = navController)

        statsScreenGraph(navController = navController, homeViewModel)

        settingScreenGraph(navController = navController)
        aboutScreenGraph(navController = navController)

    }
}

fun getStartDestination(context: Context): String {
    val prefs = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val isLoggedIn = prefs.getBoolean("is_logged_in", false)
    return if (isLoggedIn) Home.route else OnBoarding.route
}