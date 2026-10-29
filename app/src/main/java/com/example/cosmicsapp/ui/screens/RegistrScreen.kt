package com.example.cosmicsapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cosmicsapp.R
import com.example.cosmicsapp.ui.components.CosmicsBottomNav
import com.example.cosmicsapp.ui.components.CosmicsTopBar
import com.example.cosmicsapp.ui.components.CustomButton
import com.example.cosmicsapp.ui.components.CustomTextField
import com.example.cosmicsapp.ui.screens.viewmodels.RegisterViewModel
import com.example.cosmicsapp.ui.theme.*

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
) {
    val fieldWidth = 306.dp

    Scaffold(
        topBar = {
            CosmicsTopBar(
                showBack = true,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CosmicsBottomNav(
                currentRoute = "register",
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                }
            )
        },
        containerColor = BrandBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .width(fieldWidth)
                    .padding(top = 40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.user),
                    contentDescription = null,
                    tint = BrandInk,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "РЕГИСТРАЦИЯ",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = DefaultFont,
                        fontWeight = FontWeight.Bold,
                        color = BrandInk
                    )
                )
            }

            Spacer(Modifier.height(40.dp))

            CustomTextField(
                value = viewModel.name,
                onValueChange = {
                    viewModel.name = it
                    viewModel.message = null
                },
                placeholder = "Имя",
                width = fieldWidth
            )

            Spacer(Modifier.height(12.dp))

            CustomTextField(
                value = viewModel.lastName,
                onValueChange = {
                    viewModel.lastName = it
                    viewModel.message = null
                },
                placeholder = "Фамилия",
                width = fieldWidth
            )

            Spacer(Modifier.height(12.dp))

            CustomTextField(
                value = viewModel.email,
                onValueChange = {
                    viewModel.email = it
                    viewModel.message = null
                },
                placeholder = "Email",
                width = fieldWidth
            )

            Spacer(Modifier.height(12.dp))

            CustomTextField(
                value = viewModel.password,
                onValueChange = {
                    viewModel.password = it
                    viewModel.message = null
                },
                placeholder = "Пароль",
                isPassword = true,
                width = fieldWidth
            )

            Spacer(Modifier.height(12.dp))

            CustomTextField(
                value = viewModel.confirmPassword,
                onValueChange = {
                    viewModel.confirmPassword = it
                    viewModel.message = null
                },
                placeholder = "Повтор пароля",
                isPassword = true,
                width = fieldWidth
            )

            viewModel.message?.let {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = it,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = if (viewModel.isSuccess) BrandSuccess else BrandError,
                        fontFamily = DefaultFont
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Уже есть аккаунт? Войти",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = BrandMuted,
                    fontWeight = FontWeight.Medium,
                    fontFamily = DefaultFont
                ),
                modifier = Modifier.clickable {
                    navController.popBackStack()
                }
            )

            Spacer(Modifier.height(40.dp))

            CustomButton(
                text = if (viewModel.isLoading) "Загрузка..." else "Зарегистрироваться",
                enabled = !viewModel.isLoading,
                modifier = Modifier.padding(bottom = 24.dp),
                onClick = {
                    viewModel.register {
                        navController.navigate("profile") {
                            popUpTo("catalog") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}