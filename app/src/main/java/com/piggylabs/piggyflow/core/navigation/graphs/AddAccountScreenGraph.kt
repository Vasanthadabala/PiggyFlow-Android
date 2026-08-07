package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.piggylabs.piggyflow.core.navigation.AddAccount
import com.piggylabs.piggyflow.features.accounts.presentation.AddAccountScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.addAccountScreenGraph(navController: NavHostController) {
    composable(
        route = AddAccount.routePattern,
        arguments = listOf(
            navArgument(AddAccount.accountId) {
                type = NavType.IntType
                defaultValue = -1
            }
        )
    ) { backStackEntry ->
        val accountId = backStackEntry.arguments?.getInt(AddAccount.accountId) ?: -1
        AddAccountScreen(
            navController = navController,
            accountId = accountId.takeIf { it > 0 }
        )
    }
}
