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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cosmicsapp.CartStore
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.CustomTextField
import com.example.cosmicsapp.data.PickupPoint
import com.example.cosmicsapp.data.PlacedOrder
import com.example.cosmicsapp.data.UserProfile
import com.example.cosmicsapp.supabase
import com.example.cosmicsapp.ui.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Composable
fun CheckoutScreen(navController: NavController) {
    var points by remember { mutableStateOf<List<PickupPoint>>(emptyList()) }
    var selectedPointId by remember { mutableStateOf<Long?>(null) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var pointsLoaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val user = supabase.auth.currentUserOrNull()
        if (user == null) {
            isLoading = false
            navController.navigate("login") { launchSingleTop = true }
            return@LaunchedEffect
        }
        email = user.email.orEmpty()
        try {
            withTimeout(15_000) {
                points = supabase.postgrest["Pickup_points"].select().decodeList<PickupPoint>()
                val profile = supabase.postgrest["Users"].select {
                    filter { eq("user_id", user.id) }
                }.decodeSingleOrNull<UserProfile>()
                if (profile != null) {
                    firstName = profile.firstName.orEmpty()
                    lastName = profile.lastName.orEmpty()
                    phone = profile.phone.orEmpty()
                    if (!profile.email.isNullOrBlank()) email = profile.email
                }
            }
            pointsLoaded = true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = e.message ?: "Не удалось загрузить пункты выдачи"
        } finally {
            isLoading = false
        }
    }

    val recipientName = listOf(firstName.trim(), lastName.trim()).filter { it.isNotBlank() }.joinToString(" ")
    val itemCount = CartStore.lines.sumOf { it.quantity }

    Scaffold(
        topBar = { CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() }) },
        bottomBar = {
            Surface(color = BrandSurface, shadowElevation = 0.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            val userId = supabase.auth.currentUserOrNull()?.id
                            when {
                                userId == null -> errorMessage = "Войдите в аккаунт, чтобы оформить заказ"
                                CartStore.lines.isEmpty() -> errorMessage = "Корзина пуста"
                                selectedPointId == null -> errorMessage = "Выберите пункт выдачи"
                                recipientName.isBlank() -> errorMessage = "Введите имя и фамилию получателя"
                                phone.isBlank() -> errorMessage = "Введите номер телефона"
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> errorMessage = "Введите корректный email"
                                else -> {
                                    scope.launch {
                                        isSubmitting = true
                                        errorMessage = null
                                        try {
                                            val itemsJson = buildJsonArray {
                                                CartStore.lines.forEach { line ->
                                                    add(buildJsonObject {
                                                        put("product_id", line.product.productId)
                                                        put("quantity", line.quantity)
                                                    })
                                                }
                                            }
                                            val result = withTimeout(25_000) {
                                                supabase.postgrest.rpc(
                                                    function = "place_order",
                                                    parameters = buildJsonObject {
                                                        put("p_pickpoint_id", selectedPointId!!)
                                                        put("p_recipient_name", recipientName)
                                                        put("p_recipient_phone", phone.trim())
                                                        put("p_recipient_email", email.trim())
                                                        put("p_items", itemsJson)
                                                    }
                                                ).decodeList<PlacedOrder>().firstOrNull()
                                                    ?: error("Сервер не вернул номер заказа")
                                            }
                                            CartStore.clear()
                                            navController.navigate("order_detail/${result.id}") {
                                                popUpTo("catalog") { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = e.message ?: "Не удалось оформить заказ"
                                        } finally {
                                            isSubmitting = false
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isSubmitting && !isLoading && pointsLoaded && CartStore.lines.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(47.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, contentColor = BrandInk),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = BrandInk)
                        } else {
                            Text("Оформить заказ", fontFamily = DefaultFont, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = BrandMuted)) { append("Оформляя, я соглашаюсь с ") }
                            withStyle(SpanStyle(color = BrandInk)) { append("правилами Интернет-магазина COSMICS") }
                        },
                        fontFamily = DefaultFont,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
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
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 22.dp)
            ) {
                Text("Пункт выдачи товаров", fontFamily = DefaultFont, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                Spacer(Modifier.height(10.dp))

                when {
                    !errorMessage.isNullOrBlank() && !pointsLoaded -> Text(
                        errorMessage.orEmpty(), fontFamily = DefaultFont, fontSize = 12.sp, color = BrandError
                    )
                    points.isEmpty() -> Text(
                        "Пункты выдачи пока не добавлены.", fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted
                    )
                    else -> points.forEachIndexed { index, point ->
                        Column(
                            modifier = Modifier.fillMaxWidth().clickable { selectedPointId = point.id }.padding(vertical = 9.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                RadioButton(
                                    selected = selectedPointId == point.id,
                                    onClick = { selectedPointId = point.id },
                                    modifier = Modifier.size(28.dp).padding(top = 0.dp),
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                                )
                                Spacer(Modifier.width(7.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(point.addressName, fontFamily = DefaultFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                                    Spacer(Modifier.height(3.dp))
                                    Text(point.address, fontFamily = DefaultFont, fontSize = 12.sp, lineHeight = 16.sp, color = BrandInk)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Режим работы: ${point.workTime.orEmpty()}", fontFamily = DefaultFont, fontSize = 11.sp, color = BrandMuted)
                                }
                            }
                        }
                        if (index < points.lastIndex) HorizontalDivider(color = BrandBorder, thickness = 1.dp)
                    }
                }

                SectionDivider()
                Text("Дата выдачи", fontFamily = DefaultFont, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                Spacer(Modifier.height(5.dp))
                Text("Соберем в магазине за 24 часа", fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted)

                SectionDivider()
                Text("Получатель", fontFamily = DefaultFont, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                Spacer(Modifier.height(10.dp))
                CustomTextField(firstName, { firstName = it }, "Имя", width = 360.dp)
                Spacer(Modifier.height(8.dp))
                CustomTextField(lastName, { lastName = it }, "Фамилия", width = 360.dp)
                Spacer(Modifier.height(8.dp))
                CustomTextField(phone, { phone = it }, "Номер телефона", width = 360.dp)
                Spacer(Modifier.height(8.dp))
                CustomTextField(email, { email = it }, "Email", width = 360.dp)

                SectionDivider()
                Text("Оплата", fontFamily = DefaultFont, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                Spacer(Modifier.height(5.dp))
                Text("При получении", fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted)

                if (!errorMessage.isNullOrBlank() && pointsLoaded) {
                    Spacer(Modifier.height(10.dp))
                    Text(errorMessage.orEmpty(), fontFamily = DefaultFont, fontSize = 12.sp, color = BrandError)
                }
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(top = 16.dp, bottom = 13.dp),
        thickness = 1.dp,
        color = BrandBorder
    )
}
