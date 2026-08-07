package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.core.navigation.BusinessHome
import com.piggylabs.piggyflow.features.business.presentation.BusinessHomeScreen
import com.piggylabs.piggyflow.features.business.presentation.BusinessLedgerViewModel

@androidx.compose.material3.ExperimentalMaterial3Api
fun NavGraphBuilder.businessHomeScreenGraph(navController: NavHostController) {
    composable(route = BusinessHome.route) {
        val viewModel: BusinessLedgerViewModel = hiltViewModel()
        BusinessHomeScreen(navController = navController, viewModel = viewModel)
    }
}
