package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.piggylabs.piggyflow.core.navigation.AddTracker
import com.piggylabs.piggyflow.features.tracker.presentation.AddTrackerScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.addTrackerScreenGraph(navController: NavHostController) {

    composable(
        route = AddTracker.routePattern,
        arguments = listOf(
            navArgument(AddTracker.type) {
                type = NavType.StringType
                defaultValue = "budget"
            }
        )
    ) { backStackEntry ->
        AddTrackerScreen(
            navController = navController,
            initialType = backStackEntry.arguments?.getString(AddTracker.type)
        )
    }
}
