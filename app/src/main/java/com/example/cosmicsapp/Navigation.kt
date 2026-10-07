package com.example.cosmicsapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cosmicsapp.screens.AuthScreen
import com.example.cosmicsapp.screens.CatalogScreen
import com.example.cosmicsapp.screens.RegisterScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "catalog") {
        composable("catalog") { CatalogScreen(navController) }
        composable("category/{categoryName}") {}
        composable("login") { AuthScreen(navController) }
        composable("register") { RegisterScreen(navController) }

    }
}