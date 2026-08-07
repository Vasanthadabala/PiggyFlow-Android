package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.core.navigation.Home
import com.piggylabs.piggyflow.features.home.presentation.HomeScreen
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel


@ExperimentalMaterial3Api
fun NavGraphBuilder.homeScreenGraph(navController: NavHostController, viewModel: HomeViewModel) {

    composable(
        route = Home.route
    ) {
        HomeScreen(navController, viewModel)
    }
}