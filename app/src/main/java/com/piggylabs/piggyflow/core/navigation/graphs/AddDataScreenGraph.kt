package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.piggylabs.piggyflow.core.navigation.AddData
import com.piggylabs.piggyflow.features.adddata.presentation.AddDataScreen
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel


@ExperimentalMaterial3Api
fun NavGraphBuilder.addDataScreenGraph(navController: NavHostController, viewModel: HomeViewModel) {

    composable(
        route = AddData.routePattern,
        arguments = listOf(
            navArgument(AddData.type) {
                type = NavType.StringType
                defaultValue = "Expense"
            }
        ),
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(1000, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(1000, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000, easing = FastOutSlowInEasing)
            )
        }
    ) { backStackEntry ->
        val initialType = backStackEntry.arguments?.getString(AddData.type) ?: "Expense"
        AddDataScreen(navController, viewModel, initialType)
    }
}