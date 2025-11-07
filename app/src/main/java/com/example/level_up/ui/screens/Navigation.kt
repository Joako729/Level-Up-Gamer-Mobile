package com.example.level_up.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val AUTH = "auth"
}

@Composable
fun LevelUpNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.CATALOG) { CatalogScreen(navController) }
        composable(Routes.CART) { CartScreen(navController) }
        composable(Routes.PROFILE) { ProfileScreen(navController) }
        composable(Routes.AUTH) { AuthScreen(navController) }
    }
}