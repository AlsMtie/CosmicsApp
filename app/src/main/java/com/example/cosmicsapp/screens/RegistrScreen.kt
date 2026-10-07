package com.example.cosmicsapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.CustomButton
import com.example.cosmicsapp.CustomTextField
import com.example.cosmicsapp.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() }) },
        containerColor = Color(0xFFF4F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Регистрация",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A1A1A)
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            CustomTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "Имя"
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email"
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Пароль",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Повтор пароля",
                isPassword = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    style = TextStyle(fontSize = 12.sp, color = Color(0xFFD32F2F))
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            CustomButton(
                text = if (isLoading) "Загрузка..." else "Зарегистрироваться",
                onClick = {
                    // Валидация
                    when {
                        name.isBlank() || email.isBlank() || password.isBlank() -> {
                            errorMessage = "Заполните все поля"
                            return@CustomButton
                        }
                        password != confirmPassword -> {
                            errorMessage = "Пароли не совпадают"
                            return@CustomButton
                        }
                        password.length < 6 -> {
                            errorMessage = "Пароль должен быть не менее 6 символов"
                            return@CustomButton
                        }
                    }
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            supabase.auth.signUpWith(Email) {
                                this.email = email.trim()
                                this.password = password
                            }
                            navController.navigate("profile") {
                                popUpTo("catalog") { inclusive = false }
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Ошибка регистрации"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Уже есть аккаунт? Войти",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = Color(0xFF5795B5),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.clickable { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}