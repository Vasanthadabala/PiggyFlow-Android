package com.piggylabs.piggyflow.ui.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.ui.navigation.Home
import com.piggylabs.piggyflow.ui.screens.home.HomeScreen
import com.piggylabs.piggyflow.ui.screens.home.viewmodel.HomeViewModel


@ExperimentalMaterial3Api
fun NavGraphBuilder.homeScreenGraph(navController: NavHostController, viewModel: HomeViewModel) {

    composable(
        route = Home.route,

        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000, easing = FastOutSlowInEasing)
            )
        },
    ) {
        HomeScreen(navController, viewModel)
    }
}