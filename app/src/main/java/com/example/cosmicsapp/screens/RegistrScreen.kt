package com.example.cosmicsapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.cosmicsapp.ui.theme.DefaultFont
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val fieldWidth = 306.dp

    Scaffold(
        topBar = { CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() }) },
        bottomBar = { CosmicsBottomNav(currentRoute = "register", onNavigate = { navController.navigate(it) }) },
        containerColor = Color(0xFFF4F7FA)
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
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "РЕГИСТРАЦИЯ",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = DefaultFont,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF031927)
                    )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            CustomTextField(value = name, onValueChange = { name = it }, placeholder = "Имя", width = fieldWidth)
            Spacer(modifier = Modifier.height(12.dp))
            CustomTextField(value = lastName, onValueChange = { lastName = it }, placeholder = "Фамилия", width = fieldWidth)
            Spacer(modifier = Modifier.height(12.dp))
            CustomTextField(value = email, onValueChange = { email = it }, placeholder = "Email", width = fieldWidth)
            Spacer(modifier = Modifier.height(12.dp))
            CustomTextField(value = password, onValueChange = { password = it }, placeholder = "Пароль", isPassword = true, width = fieldWidth)
            Spacer(modifier = Modifier.height(12.dp))
            CustomTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = "Повтор пароля", isPassword = true, width = fieldWidth)

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    style = TextStyle(fontSize = 12.sp, color = Color(0xFFD32F2F), fontFamily = DefaultFont)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Уже есть аккаунт? Войти",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = Color(0xFF6D6D6D),
                    fontWeight = FontWeight.Medium,
                    fontFamily = DefaultFont
                ),
                modifier = Modifier.clickable { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.height(40.dp))

            CustomButton(
                text = if (isLoading) "Загрузка..." else "Зарегистрироваться",
                onClick = {
                    when {
                        name.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() -> {
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

                            val authUserId = supabase.auth.currentUserOrNull()?.id ?: ""
                            supabase.postgrest["Users"].insert(
                                mapOf(
                                    "user_id" to authUserId,
                                    "email" to email.trim(),
                                    "first_name" to name,
                                    "last_name" to lastName,
                                    "phone" to ""
                                )
                            )

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
                enabled = !isLoading,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}