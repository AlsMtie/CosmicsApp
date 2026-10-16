package com.example.cosmicsapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cosmicsapp.screens.AuthScreen
import com.example.cosmicsapp.screens.CatalogScreen
import com.example.cosmicsapp.screens.CategoryScreen
import com.example.cosmicsapp.screens.ProductDetailScreen
import com.example.cosmicsapp.screens.ProfileScreen
import com.example.cosmicsapp.screens.RegisterScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "catalog") {
        composable("catalog") { CatalogScreen(navController) }

        composable("category/{categoryId}/{categoryName}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toLongOrNull() ?: 0L
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryScreen(navController, categoryId, categoryName)
        }

        composable("product/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull() ?: 0L
            ProductDetailScreen(navController, productId)
        }

        composable("login") { AuthScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
    }
}