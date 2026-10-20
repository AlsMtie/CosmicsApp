package com.example.cosmicsapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cosmicsapp.screens.AuthScreen
import com.example.cosmicsapp.screens.CartScreen
import com.example.cosmicsapp.screens.CatalogScreen
import com.example.cosmicsapp.screens.CategoryScreen
import com.example.cosmicsapp.screens.CheckoutScreen
import com.example.cosmicsapp.screens.OrderDetailScreen
import com.example.cosmicsapp.screens.OrderHistoryScreen
import com.example.cosmicsapp.screens.ProductDetailScreen
import com.example.cosmicsapp.screens.ProfileScreen
import com.example.cosmicsapp.screens.RegisterScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "catalog") {
        composable("catalog") { CatalogScreen(navController) }
        composable(
            route = "category/{categoryId}/{categoryName}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { entry ->
            CategoryScreen(
                navController,
                entry.arguments?.getLong("categoryId") ?: 0L,
                entry.arguments?.getString("categoryName").orEmpty()
            )
        }
        composable(
            route = "product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { entry ->
            ProductDetailScreen(navController, entry.arguments?.getLong("productId") ?: 0L)
        }
        composable("cart") { CartScreen(navController) }
        composable("checkout") { CheckoutScreen(navController) }
        composable("order_history") { OrderHistoryScreen(navController) }
        composable(
            route = "order_detail/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { entry ->
            OrderDetailScreen(navController, entry.arguments?.getLong("orderId") ?: 0L)
        }
        composable("login") { AuthScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
    }
}
