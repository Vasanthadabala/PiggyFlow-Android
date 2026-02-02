package com.piggylabs.piggyflow.navigation.graphs.dashboard

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.navigation.AccountType
import com.piggylabs.piggyflow.ui.screens.auth.SignInScreen
import com.piggylabs.piggyflow.ui.screens.onboarding.AccountTypeScreen
import com.piggylabs.piggyflow.ui.screens.onboarding.AccountTypeScreenComponent


@ExperimentalMaterial3Api
fun NavGraphBuilder.accountTypeSelectionScreenGraph(navController: NavHostController) {

    composable(
        route = AccountType.route,
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
        AccountTypeScreen(navController)
    }
}