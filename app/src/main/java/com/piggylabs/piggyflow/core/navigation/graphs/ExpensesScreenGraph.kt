package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.core.navigation.Expenses
import com.piggylabs.piggyflow.features.transactions.presentation.ExpensesScreen
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel

@ExperimentalMaterial3Api
fun NavGraphBuilder.expensesScreenGraph(navController: NavHostController, viewModel: HomeViewModel) {
    composable(
        route = Expenses.route,
    ) {
        ExpensesScreen(navController = navController, viewModel = viewModel)
    }
}
