package com.example.cosmicsapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("user_id") val userId: String? = null,
    val email: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val phone: String? = null
)

@Composable
fun ProfileScreen(navController: NavController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val fieldWidth = 306.dp

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser == null) {
                    navController.navigate("login") {
                        popUpTo("catalog") { inclusive = false }
                    }
                    return@launch
                }
                email = currentUser.email ?: ""
                val authUserId = currentUser.id

                val profile = supabase.postgrest["Users"]
                    .select { filter { eq("user_id", authUserId) } }
                    .decodeSingleOrNull<UserProfile>()

                if (profile != null) {
                    userId = profile.userId
                    firstName = profile.firstName ?: ""
                    lastName = profile.lastName ?: ""
                    phone = profile.phone ?: ""
                }
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = { CosmicsTopBar() },
        bottomBar = { CosmicsBottomNav(currentRoute = "profile", onNavigate = { navController.navigate(it) }) },
        containerColor = Color(0xFFF4F7FA)
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF5795B5))
            }
            return@Scaffold
        }

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
                    text = "ПРОФИЛЬ",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontFamily = DefaultFont,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF031927)
                    )
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (userId != null) {
                    CustomTextField(
                        value = userId!!,
                        onValueChange = { },
                        placeholder = "ID пользователя",
                        width = fieldWidth
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                CustomTextField(value = firstName, onValueChange = { firstName = it }, placeholder = "Имя", width = fieldWidth)
                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(value = lastName, onValueChange = { lastName = it }, placeholder = "Фамилия", width = fieldWidth)
                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(value = phone, onValueChange = { phone = it }, placeholder = "Телефон", width = fieldWidth)
                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(value = email, onValueChange = { }, placeholder = "Email", width = fieldWidth)

                if (message != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = message!!,
                        style = TextStyle(fontSize = 12.sp, color = Color(0xFF5795B5), fontFamily = DefaultFont)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                CustomButton(
                    text = if (isSaving) "Сохранение..." else "Сохранить",
                    onClick = {
                        scope.launch {
                            isSaving = true
                            message = null
                            try {
                                val authUserId = supabase.auth.currentUserOrNull()?.id ?: return@launch
                                supabase.postgrest["Users"].update(
                                    mapOf(
                                        "first_name" to firstName,
                                        "last_name" to lastName,
                                        "phone" to phone
                                    )
                                ) {
                                    filter { eq("user_id", authUserId) }
                                }
                                message = "Сохранено"
                            } catch (e: Exception) {
                                message = "Ошибка: ${e.message}"
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    width = 190.dp,
                    height = 30.dp
                )
            }

            OutlinedButton(
                onClick = { navController.navigate("order_history") },
                modifier = Modifier.width(345.dp).height(58.dp),
                shape = RoundedCornerShape(20.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFC7DDF2))
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF031927)
                )
            ) {
                Text(
                    text = "История заказов",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, fontFamily = DefaultFont)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Выйти из аккаунта",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = Color(0xFF6D6D6D),
                    fontWeight = FontWeight.Medium,
                    fontFamily = DefaultFont
                ),
                modifier = Modifier
                    .clickable {
                        scope.launch {
                            supabase.auth.signOut()
                            navController.navigate("login") {
                                popUpTo("catalog") { inclusive = false }
                            }
                        }
                    }
                    .padding(vertical = 12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}