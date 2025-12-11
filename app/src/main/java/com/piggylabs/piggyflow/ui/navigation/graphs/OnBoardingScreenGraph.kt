package com.piggylabs.piggyflow.ui.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.ui.navigation.OnBoarding
import com.piggylabs.piggyflow.ui.screens.onboarding.OnBoardingScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.onBoardingScreenGraph(navController: NavHostController) {

    composable(
        route = OnBoarding.route,

        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(1000, easing = FastOutSlowInEasing)
            )
        }

    ) {
        OnBoardingScreen(navController)
    }
}