package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.core.navigation.Transactions
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel
import com.piggylabs.piggyflow.features.transactions.presentation.TransactionsScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.transactionsScreenGraph(navController: NavHostController, viewModel: HomeViewModel) {
    composable(
        route = Transactions.route,
    ) {
        TransactionsScreen(navController = navController, viewModel = viewModel)
    }
}
