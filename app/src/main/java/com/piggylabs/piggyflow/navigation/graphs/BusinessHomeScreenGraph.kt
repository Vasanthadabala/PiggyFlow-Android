package com.piggylabs.piggyflow.navigation.graphs

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.navigation.BusinessHome
import com.piggylabs.piggyflow.ui.screens.business.business_home.BusinessHomeScreen
import com.piggylabs.piggyflow.ui.screens.business.viewmodel.BusinessLedgerViewModel

@androidx.compose.material3.ExperimentalMaterial3Api
fun NavGraphBuilder.businessHomeScreenGraph(navController: NavHostController) {
    composable(route = BusinessHome.route) {
        val viewModel: BusinessLedgerViewModel = viewModel()
        BusinessHomeScreen(navController = navController, viewModel = viewModel)
    }
}
