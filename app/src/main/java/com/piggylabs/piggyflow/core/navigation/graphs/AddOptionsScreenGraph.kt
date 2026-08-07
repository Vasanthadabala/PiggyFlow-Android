package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.core.navigation.AddOptions
import com.piggylabs.piggyflow.features.adddata.presentation.AddOptionsScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.addOptionsScreenGraph(navController: NavHostController) {
    composable(route = AddOptions.route) {
        AddOptionsScreen(navController = navController)
    }
}
