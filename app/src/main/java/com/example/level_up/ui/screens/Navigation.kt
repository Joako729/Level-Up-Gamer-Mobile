package com.example.level_up.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.level_up.viewmodel.CatalogViewModel

object Routes {
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val AUTH = "auth"
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
    }
}