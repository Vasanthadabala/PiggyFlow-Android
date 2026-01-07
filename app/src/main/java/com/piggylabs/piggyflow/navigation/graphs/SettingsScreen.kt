package com.piggylabs.piggyflow.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.data.remote.SyncViewModel
import com.piggylabs.piggyflow.navigation.Settings
import com.piggylabs.piggyflow.ui.screens.settings.SettingScreen


fun NavGraphBuilder.settingScreenGraph(navController: NavHostController, viewModel: SyncViewModel) {

    composable(
        route = Settings.route,

        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000, easing = FastOutSlowInEasing)
            )
        }
    ) {
        SettingScreen(navController, viewModel)
    }
}