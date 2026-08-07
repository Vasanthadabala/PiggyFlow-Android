package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.piggylabs.piggyflow.core.navigation.Appearance
import com.piggylabs.piggyflow.core.navigation.InfoScreen
import com.piggylabs.piggyflow.features.profile.presentation.AppearanceScreen
import com.piggylabs.piggyflow.features.profile.presentation.InfoScreen as InfoScreenComposable

@ExperimentalMaterial3Api
fun NavGraphBuilder.appearanceScreenGraph(navController: NavHostController) {
    composable(route = Appearance.route) {
        AppearanceScreen(navController = navController)
    }
}

@ExperimentalMaterial3Api
fun NavGraphBuilder.infoScreenGraph(navController: NavHostController) {
    composable(
        route = InfoScreen.routePattern,
        arguments = listOf(navArgument(InfoScreen.topic) { type = NavType.StringType })
    ) {
        val topic = it.arguments?.getString(InfoScreen.topic) ?: ""
        InfoScreenComposable(navController = navController, topic = topic)
    }
}
