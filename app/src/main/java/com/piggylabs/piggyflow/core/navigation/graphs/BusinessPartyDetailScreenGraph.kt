package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.piggylabs.piggyflow.core.navigation.BusinessPartyDetail
import com.piggylabs.piggyflow.features.business.presentation.BusinessPartyDetailScreen
import com.piggylabs.piggyflow.features.business.presentation.BusinessLedgerViewModel

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
        val viewModel: BusinessLedgerViewModel = hiltViewModel()
        val partyId = backStackEntry.arguments?.getInt(BusinessPartyDetail.partyId) ?: return@composable
        BusinessPartyDetailScreen(
            navController = navController,
            viewModel = viewModel,
            partyId = partyId
        )
    }
}
