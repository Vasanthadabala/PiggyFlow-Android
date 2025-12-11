package com.piggylabs.piggyflow.ui.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.ui.navigation.About
import com.piggylabs.piggyflow.ui.navigation.AddData
import com.piggylabs.piggyflow.ui.screens.about.AboutScreen
import com.piggylabs.piggyflow.ui.screens.home.adddata.AddDataScreen
import com.piggylabs.piggyflow.ui.screens.home.viewmodel.HomeViewModel


@ExperimentalMaterial3Api
fun NavGraphBuilder.addDataScreenGraph(navController: NavHostController, viewModel: HomeViewModel) {

    composable(
        route = AddData.route,
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
        AddDataScreen(navController, viewModel)
    }
}