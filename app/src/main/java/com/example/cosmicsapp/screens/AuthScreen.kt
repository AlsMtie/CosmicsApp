package com.example.cosmicsapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cosmicsapp.CosmicsBottomNav
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.CustomButton
import com.example.cosmicsapp.CustomTextField
import com.example.cosmicsapp.InlineStatus
import com.example.cosmicsapp.R
import com.example.cosmicsapp.ScreenTitle
import com.example.cosmicsapp.supabase
import com.example.cosmicsapp.ui.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CosmicsTopBar(
                showBack = true,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CosmicsBottomNav(currentRoute = "login") { route ->
                navController.navigate(route) {
                    launchSingleTop = true
                }
            }
        },
        containerColor = BrandBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(
                    horizontal = 24.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScreenTitle(
                    title = "АВТОРИЗАЦИЯ",
                    iconRes = R.drawable.user,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )

                Spacer(Modifier.height(150.dp))

                CustomTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    placeholder = "Email"
                )

                Spacer(Modifier.height(10.dp))

                CustomTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    placeholder = "Пароль",
                    isPassword = true
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(10.dp))

                    InlineStatus(
                        errorMessage.orEmpty(),
                        isError = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        navController.navigate("register") {
                            launchSingleTop = true
                        }
                    },
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(
                        "Регистрация",
                        fontFamily = DefaultFont,
                        fontSize = 12.sp,
                        color = BrandMuted
                    )
                }
            }

            CustomButton(
                text = if (isLoading) "Входим..." else "Войти",
                onClick = {
                    if (
                        !android.util.Patterns.EMAIL_ADDRESS
                            .matcher(email.trim())
                            .matches() ||
                        password.isBlank()
                    ) {
                        errorMessage =
                            "Введите корректный email и пароль"
                        return@CustomButton
                    }

                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        try {
                            supabase.auth.signInWith(Email) {
                                this.email = email.trim()
                                this.password = password
                            }

                            navController.navigate("profile") {
                                popUpTo("login") {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message
                                ?: "Не удалось войти. Проверь email и пароль."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 4.dp)
            )
        }
    }
}