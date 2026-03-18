package com.piggylabs.piggyflow.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.navigation.Tracker
import com.piggylabs.piggyflow.ui.screens.common.tracker.TrackerScreen
import com.piggylabs.piggyflow.ui.screens.common.tracker.viewmodel.TrackerViewModel

fun NavGraphBuilder.trackerScreenGraph(navController: NavHostController) {

    composable(
        route = Tracker.route,

        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000, easing = FastOutSlowInEasing)
            )
        }
    ) {
        val trackerViewModel: TrackerViewModel = viewModel()
        TrackerScreen(navController, trackerViewModel)
    }
}
