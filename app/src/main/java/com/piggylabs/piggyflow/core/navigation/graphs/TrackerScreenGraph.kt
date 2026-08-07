package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.core.navigation.Tracker
import com.piggylabs.piggyflow.features.tracker.presentation.TrackerScreen
import com.piggylabs.piggyflow.features.tracker.presentation.TrackerViewModel

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
        val trackerViewModel: TrackerViewModel = hiltViewModel()
        TrackerScreen(navController, trackerViewModel)
    }
}
