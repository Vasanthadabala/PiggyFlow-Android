package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.core.navigation.Settings
import com.piggylabs.piggyflow.features.settings.presentation.SettingScreen


fun NavGraphBuilder.settingScreenGraph(navController: NavHostController) {

    composable(
        route = Settings.route,

        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000, easing = FastOutSlowInEasing)
            )
        }
    ) {
        SettingScreen(navController)
    }
}