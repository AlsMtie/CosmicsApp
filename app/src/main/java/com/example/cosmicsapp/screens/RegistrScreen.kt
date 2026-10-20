package com.example.cosmicsapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cosmicsapp.CosmicsBottomNav
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.CustomButton
import com.example.cosmicsapp.CustomTextField
import com.example.cosmicsapp.R
import com.example.cosmicsapp.supabase
import com.example.cosmicsapp.ui.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
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
                value = name,
                onValueChange = {
                    name = it
                    message = null
                },
                placeholder = "Имя",
                width = fieldWidth
            )

            Spacer(Modifier.height(12.dp))

            CustomTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    message = null
                },
                placeholder = "Фамилия",
                width = fieldWidth
            )

            Spacer(Modifier.height(12.dp))

            CustomTextField(
                value = email,
                onValueChange = {
                    email = it
                    message = null
                },
                placeholder = "Email",
                width = fieldWidth
            )

            Spacer(Modifier.height(12.dp))

            CustomTextField(
                value = password,
                onValueChange = {
                    password = it
                    message = null
                },
                placeholder = "Пароль",
                isPassword = true,
                width = fieldWidth
            )

            Spacer(Modifier.height(12.dp))

            CustomTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    message = null
                },
                placeholder = "Повтор пароля",
                isPassword = true,
                width = fieldWidth
            )

            message?.let {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = it,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = if (isSuccess) {
                            BrandSuccess
                        } else {
                            BrandError
                        },
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
                text = if (isLoading) {
                    "Загрузка..."
                } else {
                    "Зарегистрироваться"
                },
                enabled = !isLoading,
                modifier = Modifier.padding(bottom = 24.dp),
                onClick = {
                    val cleanEmail = email.trim()

                    when {
                        name.isBlank() ||
                                lastName.isBlank() ||
                                cleanEmail.isBlank() ||
                                password.isBlank() -> {
                            message = "Заполните все поля"
                            isSuccess = false
                            return@CustomButton
                        }

                        !android.util.Patterns.EMAIL_ADDRESS
                            .matcher(cleanEmail)
                            .matches() -> {
                            message = "Введите корректный email"
                            isSuccess = false
                            return@CustomButton
                        }

                        password.length < 6 -> {
                            message = "Пароль должен быть не менее 6 символов"
                            isSuccess = false
                            return@CustomButton
                        }

                        password != confirmPassword -> {
                            message = "Пароли не совпадают"
                            isSuccess = false
                            return@CustomButton
                        }
                    }

                    scope.launch {
                        isLoading = true
                        message = null
                        isSuccess = false

                        try {
                            supabase.auth.signUpWith(Email) {
                                this.email = cleanEmail
                                this.password = password

                                this.data = buildJsonObject {
                                    put("first_name", name.trim())
                                    put("last_name", lastName.trim())
                                }
                            }

                            if (supabase.auth.currentUserOrNull() != null) {
                                navController.navigate("profile") {
                                    popUpTo("catalog") {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                message =
                                    "Аккаунт создан. Теперь войдите в приложение."
                                isSuccess = true
                            }
                        } catch (e: Exception) {
                            message = e.message
                                ?: "Не удалось создать аккаунт"
                            isSuccess = false
                        } finally {
                            isLoading = false
                        }
                    }
                }
            )
        }
    }
}