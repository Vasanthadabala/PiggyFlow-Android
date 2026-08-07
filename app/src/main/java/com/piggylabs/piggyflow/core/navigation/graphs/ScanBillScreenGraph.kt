package com.piggylabs.piggyflow.core.navigation.graphs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.piggylabs.piggyflow.core.navigation.ScanBill
import com.piggylabs.piggyflow.features.scanbill.presentation.ScanBillScreen

@ExperimentalMaterial3Api
fun NavGraphBuilder.scanBillScreenGraph(navController: NavHostController) {

    composable(
        route = ScanBill.route
    ) {
        ScanBillScreen(navController = navController)
    }
}
