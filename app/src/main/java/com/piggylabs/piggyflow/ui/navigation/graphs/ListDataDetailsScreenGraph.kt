package com.piggylabs.piggyflow.ui.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.piggylabs.piggyflow.ui.navigation.ListDataDetails
import com.piggylabs.piggyflow.ui.screens.home.details.ListDataDetailsScreen
import com.piggylabs.piggyflow.ui.screens.home.viewmodel.HomeViewModel


@ExperimentalMaterial3Api
fun NavGraphBuilder.listDataDetailsScreenGraph(navController: NavHostController, viewModel: HomeViewModel) {

    composable(
        route = "${ListDataDetails.route}/{type}/{listID}",
        arguments = listOf(
            navArgument(ListDataDetails.type){type = NavType.StringType},
            navArgument(ListDataDetails.listID){type = NavType.StringType}
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
    ) {

        val type = it.arguments?.getString(ListDataDetails.type) ?: ""
        val listID = it.arguments?.getString(ListDataDetails.listID) ?: ""
        ListDataDetailsScreen(navController,viewModel, type, listID)
    }
}