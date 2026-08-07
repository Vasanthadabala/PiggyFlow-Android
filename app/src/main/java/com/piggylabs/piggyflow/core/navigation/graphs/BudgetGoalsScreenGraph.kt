package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.core.navigation.BudgetGoals
import com.piggylabs.piggyflow.features.budget.presentation.BudgetGoalsScreen
import com.piggylabs.piggyflow.features.budget.presentation.BudgetGoalsViewModel

@ExperimentalMaterial3Api
fun NavGraphBuilder.budgetGoalsScreenGraph(navController: NavHostController) {
    composable(
        route = BudgetGoals.route,

        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(1000, easing = FastOutSlowInEasing)
            )
        }
    ) {
        val viewModel: BudgetGoalsViewModel = hiltViewModel()
        BudgetGoalsScreen(navController = navController, viewModel = viewModel)
    }
}
