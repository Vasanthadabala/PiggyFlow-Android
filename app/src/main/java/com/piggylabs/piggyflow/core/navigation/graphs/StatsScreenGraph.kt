package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.core.navigation.Stats
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel
import com.piggylabs.piggyflow.features.reports.presentation.StatsScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.statsScreenGraph(navController: NavHostController, viewModel: HomeViewModel) {

    composable(
        route = Stats.route,
    ) {
        StatsScreen(navController, viewModel)
    }
}