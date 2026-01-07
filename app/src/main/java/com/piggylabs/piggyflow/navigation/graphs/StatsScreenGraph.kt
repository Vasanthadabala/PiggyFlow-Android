package com.piggylabs.piggyflow.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.navigation.Stats
import com.piggylabs.piggyflow.ui.screens.home.viewmodel.HomeViewModel
import com.piggylabs.piggyflow.ui.screens.stats.StatsScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.statsScreenGraph(navController: NavHostController, viewModel: HomeViewModel) {

    composable(
        route = Stats.route,
    ) {
        StatsScreen(navController, viewModel)
    }
}