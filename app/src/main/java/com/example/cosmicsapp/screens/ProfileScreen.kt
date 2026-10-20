package com.example.cosmicsapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class ProfileRecord(
    @SerialName("user_id") val userId: String,
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
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var profileExists by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(4_000)
            statusMessage = null
        }
    }

    LaunchedEffect(Unit) {
        try {
            val user = supabase.auth.currentUserOrNull()
            if (user == null) {
                navController.navigate("login") {
                    popUpTo("catalog") { inclusive = false }
                    launchSingleTop = true
                }
                return@LaunchedEffect
            }
            email = user.email.orEmpty()
            val profile = withTimeout(12_000) {
                supabase.postgrest["Users"].select {
                    filter { eq("user_id", user.id) }
                }.decodeSingleOrNull<ProfileRecord>()
            }
            if (profile != null) {
                profileExists = true
                firstName = profile.firstName.orEmpty()
                lastName = profile.lastName.orEmpty()
                phone = profile.phone.orEmpty()
                if (!profile.email.isNullOrBlank()) email = profile.email
            } else {
                statusMessage = "Профиль не найден. Выполни SQL-скрипт setup_cosmics.sql в Supabase."
                isError = true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            statusMessage = e.message ?: "Не удалось загрузить профиль"
            isError = true
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = { CosmicsTopBar() },
        bottomBar = {
            CosmicsBottomNav(currentRoute = "profile") { route ->
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 22.dp, vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScreenTitle(
                        title = "ПРОФИЛЬ",
                        iconRes = R.drawable.user,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp)
                    )
                    CustomTextField(firstName, { firstName = it; statusMessage = null }, "Имя")
                    Spacer(Modifier.height(8.dp))
                    CustomTextField(lastName, { lastName = it; statusMessage = null }, "Фамилия")
                    Spacer(Modifier.height(8.dp))
                    CustomTextField(phone, { phone = it; statusMessage = null }, "Телефон")
                    Spacer(Modifier.height(8.dp))
                    CustomTextField(email, {}, "Email", readOnly = true)

                    statusMessage?.let {
                        Spacer(Modifier.height(10.dp))
                        InlineStatus(it, isError = isError, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(14.dp))
                    CustomButton(
                        text = if (isSaving) "Сохраняем..." else "Сохранить",
                        onClick = {
                            val userId = supabase.auth.currentUserOrNull()?.id
                            if (userId == null) {
                                statusMessage = "Войдите в аккаунт заново"
                                isError = true
                                return@CustomButton
                            }
                            if (!profileExists) {
                                statusMessage = "Профиль отсутствует. Выполни SQL-скрипт настройки Supabase."
                                isError = true
                                return@CustomButton
                            }
                            scope.launch {
                                isSaving = true
                                statusMessage = null
                                try {
                                    withTimeout(12_000) {
                                        supabase.postgrest["Users"].update(
                                            mapOf(
                                                "first_name" to firstName.trim(),
                                                "last_name" to lastName.trim(),
                                                "phone" to phone.trim(),
                                                "email" to email.trim()
                                            )
                                        ) { filter { eq("user_id", userId) } }
                                    }
                                    statusMessage = "Изменения сохранены"
                                    isError = false
                                } catch (e: Exception) {
                                    statusMessage = e.message ?: "Не удалось сохранить изменения"
                                    isError = true
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving,
                        width = 190.dp,
                        height = 36.dp
                    )
                }

                Button(
                    onClick = { navController.navigate("order_history") { launchSingleTop = true } },
                    modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.82f).height(43.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, contentColor = BrandInk),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("История заказов", fontFamily = DefaultFont, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                }
                TextButton(
                    onClick = {
                        scope.launch {
                            supabase.auth.signOut()
                            navController.navigate("login") {
                                popUpTo("catalog") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Выйти из аккаунта", fontFamily = DefaultFont, fontSize = 11.sp, color = BrandMuted)
                }
            }
        }
    }
}
