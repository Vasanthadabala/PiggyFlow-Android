package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.core.navigation.Accounts
import com.piggylabs.piggyflow.features.accounts.presentation.AccountsScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.accountsScreenGraph(navController: NavHostController) {
    composable(
        route = Accounts.route,
    ) {
        AccountsScreen(navController = navController)
    }
}
