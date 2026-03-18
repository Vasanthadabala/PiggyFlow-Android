package com.piggylabs.piggyflow.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.piggylabs.piggyflow.navigation.BusinessPartyDetail
import com.piggylabs.piggyflow.ui.screens.business.business_party_details.BusinessPartyDetailScreen
import com.piggylabs.piggyflow.ui.screens.business.viewmodel.BusinessLedgerViewModel

@androidx.compose.material3.ExperimentalMaterial3Api
fun NavGraphBuilder.businessPartyDetailScreenGraph(navController: NavHostController) {
    composable(
        route = "${BusinessPartyDetail.route}/{${BusinessPartyDetail.partyId}}",
        arguments = listOf(navArgument(BusinessPartyDetail.partyId) { type = NavType.IntType }),
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(1000, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(1000, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000, easing = FastOutSlowInEasing)
            )
        }
    ) { backStackEntry ->
        val viewModel: BusinessLedgerViewModel = viewModel()
        val partyId = backStackEntry.arguments?.getInt(BusinessPartyDetail.partyId) ?: return@composable
        BusinessPartyDetailScreen(
            navController = navController,
            viewModel = viewModel,
            partyId = partyId
        )
    }
}
