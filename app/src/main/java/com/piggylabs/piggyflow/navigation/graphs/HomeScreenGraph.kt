package com.piggylabs.piggyflow.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.navigation.Home
import com.piggylabs.piggyflow.ui.screens.personal.home.HomeScreen
import com.piggylabs.piggyflow.ui.screens.personal.home.viewmodel.HomeViewModel


@ExperimentalMaterial3Api
fun NavGraphBuilder.homeScreenGraph(navController: NavHostController, viewModel: HomeViewModel) {

    composable(
        route = Home.route
    ) {
        HomeScreen(navController, viewModel)
    }
}