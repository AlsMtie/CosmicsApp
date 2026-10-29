package com.example.cosmicsapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cosmicsapp.ui.components.*
import com.example.cosmicsapp.ui.screens.viewmodels.AuthViewModel
import com.example.cosmicsapp.ui.theme.*

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    Scaffold(
        topBar = { CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() }) },
        bottomBar = {
            CosmicsBottomNav(currentRoute = "login") { route ->
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScreenTitle(
                    title = "АВТОРИЗАЦИЯ",
                    iconRes = com.example.cosmicsapp.R.drawable.user,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
                Spacer(Modifier.height(150.dp))

                CustomTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it; viewModel.errorMessage = null },
                    placeholder = "Email"
                )
                Spacer(Modifier.height(10.dp))
                CustomTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.password = it; viewModel.errorMessage = null },
                    placeholder = "Пароль",
                    isPassword = true
                )

                if (viewModel.errorMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    InlineStatus(viewModel.errorMessage!!, isError = true, modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate("register") { launchSingleTop = true } }) {
                    Text("Регистрация", fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted)
                }
            }

            CustomButton(
                text = if (viewModel.isLoading) "Входим..." else "Войти",
                onClick = {
                    viewModel.login {
                        navController.navigate("profile") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                enabled = !viewModel.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 4.dp)
            )
        }
    }
}