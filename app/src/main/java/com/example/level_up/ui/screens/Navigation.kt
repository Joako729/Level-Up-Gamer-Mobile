package com.example.level_up.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.level_up.viewmodel.CatalogViewModel

object Routes {
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val AUTH = "auth"
    const val ADD_REVIEW = "add_review"
    const val PRODUCT_DETAIL = "product_detail/{productId}"

    fun productDetail(productId: Int): String = "product_detail/$productId"
}

@Composable
fun LevelUpNavHost(navController: NavHostController = rememberNavController()) {
    val catalogViewModel: CatalogViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(navController, catalogViewModel) }
        composable(Routes.CATALOG) { CatalogScreen(navController, catalogViewModel) }
        composable(Routes.CART) { CartScreen(navController) }
        composable(Routes.PROFILE) { ProfileScreen(navController) }
        composable(Routes.AUTH) { AuthScreen(navController) }
        composable(Routes.ADD_REVIEW) { AddReviewScreen(navController) }
        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId")
            val products by catalogViewModel.products.collectAsState()
            val product = products.find { it.id == productId }

            ProductDetailScreen(navController, product)
        }
    }
}