package com.example.cosmicsapp.screens

import android.R.attr.fontFamily
import android.R.attr.textStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cosmicsapp.CosmicsBottomNav
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.R
import com.example.cosmicsapp.supabase
import com.example.cosmicsapp.ui.theme.DefaultFont
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val fieldWidth = 306.dp
    val fieldHeight = 52.dp

    Scaffold(
        topBar = { CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() }) },
        bottomBar = { CosmicsBottomNav(currentRoute = "login", onNavigate = { navController.navigate(it) }) },
        containerColor = Color(0xFFF4F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    text = "АВТОРИЗАЦИЯ",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = DefaultFont,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF031927)
                    )
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = {
                        Text(
                            text = "Email",
                            style = TextStyle(
                                fontFamily = DefaultFont
                            ))
                            },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(fieldWidth)
                        .height(fieldHeight),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5795B5),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedLabelColor = Color(0xFF5795B5),
                        unfocusedLabelColor = Color(0xFF757575),
                        cursorColor = Color(0xFF5795B5),
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(text = "Пароль", style = TextStyle(fontFamily = DefaultFont)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .width(fieldWidth)
                        .height(fieldHeight),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5795B5),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedLabelColor = Color(0xFF5795B5),
                        unfocusedLabelColor = Color(0xFF757575),
                        cursorColor = Color(0xFF5795B5),
                        focusedTextColor = Color(0xFF1A1A1A),
                        unfocusedTextColor = Color(0xFF1A1A1A)
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        style = TextStyle(fontSize = 12.sp, color = Color(0xFFD32F2F), fontFamily = DefaultFont)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Регистрация",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = Color(0xFF6D6D6D),
                        fontWeight = FontWeight.Medium,
                        fontFamily = DefaultFont
                    ),
                    modifier = Modifier.clickable { navController.navigate("register") }
                )
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Заполните все поля"
                        return@Button
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
                                popUpTo("catalog") { inclusive = false }
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Ошибка авторизации"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .width(345.dp)
                    .height(58.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFC7DDF2),
                    contentColor = Color(0xFF000000),
                    disabledContainerColor = Color(0xFFC7E1F4).copy(alpha = 0.5f),
                    disabledContentColor = Color(0xFF000000).copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = if (isLoading) "Загрузка..." else "Войти",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DefaultFont,
                        color = Color(0xFF000000)
                    )
                )
            }
        }
    }
}