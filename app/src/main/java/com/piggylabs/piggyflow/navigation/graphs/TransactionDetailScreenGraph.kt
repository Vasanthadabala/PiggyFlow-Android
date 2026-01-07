package com.piggylabs.piggyflow.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.piggylabs.piggyflow.navigation.TransactionDetail
import com.piggylabs.piggyflow.ui.screens.home.viewmodel.HomeViewModel
import com.piggylabs.piggyflow.ui.screens.transaction_detail.TransactionDetailScreen


@ExperimentalMaterial3Api
fun NavGraphBuilder.transactionDetailScreenScreenGraph(navController: NavHostController, viewModel: HomeViewModel) {

    composable(
        route = "${TransactionDetail.route}/{type}/{listID}",
        arguments = listOf(
            navArgument(TransactionDetail.type){type = NavType.StringType},
            navArgument(TransactionDetail.listID){type = NavType.StringType}
        ),
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
    ) {

        val type = it.arguments?.getString(TransactionDetail.type) ?: ""
        val listID = it.arguments?.getString(TransactionDetail.listID) ?: ""
        TransactionDetailScreen(navController,viewModel, type, listID)
    }
}