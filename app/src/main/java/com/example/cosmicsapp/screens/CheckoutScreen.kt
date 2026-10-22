
package com.example.cosmicsapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cosmicsapp.CartStore
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.CustomTextField
import com.example.cosmicsapp.data.PickupPoint
import com.example.cosmicsapp.data.Product
import com.example.cosmicsapp.data.Brand
import com.example.cosmicsapp.data.Category
import com.example.cosmicsapp.data.StoreOrder
import com.example.cosmicsapp.data.UserProfile
import com.example.cosmicsapp.supabase
import com.example.cosmicsapp.ui.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.random.Random
import com.example.cosmicsapp.data.OrderInsert
import com.example.cosmicsapp.data.OrderItemInsert

@Composable
fun CheckoutScreen(navController: NavController) {
    var points by remember {
        mutableStateOf<List<PickupPoint>>(emptyList())
    }
    var selectedPointId by remember {
        mutableStateOf<Long?>(null)
    }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }
    var pointsLoaded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val user = supabase.auth.currentUserOrNull()

        if (user == null) {
            isLoading = false
            navController.navigate("login") {
                launchSingleTop = true
            }
            return@LaunchedEffect
        }

        email = user.email.orEmpty()

        try {
            withTimeout(15_000) {
                points = supabase.postgrest["Pickup_points"]
                    .select()
                    .decodeList<PickupPoint>()

                val profile = supabase.postgrest["Users"].select {
                    filter {
                        eq("user_id", user.id)
                    }
                }.decodeSingleOrNull<UserProfile>()

                if (profile != null) {
                    firstName = profile.firstName.orEmpty()
                    lastName = profile.lastName.orEmpty()
                    phone = profile.phone.orEmpty()

                    if (!profile.email.isNullOrBlank()) {
                        email = profile.email
                    }
                }
            }

            pointsLoaded = true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = e.message
                ?: "Не удалось загрузить пункты выдачи"
        } finally {
            isLoading = false
        }
    }

    val recipientName = listOf(
        firstName.trim(),
        lastName.trim()
    ).filter {
        it.isNotBlank()
    }.joinToString(" ")

    Scaffold(
        topBar = {
            CosmicsTopBar(
                showBack = true,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Surface(
                color = BrandSurface,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 18.dp,
                            vertical = 8.dp
                        )
                ) {
                    Button(
                        onClick = {
                            val userId =
                                supabase.auth.currentUserOrNull()?.id

                            when {
                                userId == null -> {
                                    errorMessage =
                                        "Войдите в аккаунт, чтобы оформить заказ"
                                }

                                CartStore.lines.isEmpty() -> {
                                    errorMessage = "Корзина пуста"
                                }

                                selectedPointId == null -> {
                                    errorMessage = "Выберите пункт выдачи"
                                }

                                recipientName.isBlank() -> {
                                    errorMessage =
                                        "Введите имя и фамилию получателя"
                                }

                                phone.isBlank() -> {
                                    errorMessage =
                                        "Введите номер телефона"
                                }

                                !android.util.Patterns.EMAIL_ADDRESS
                                    .matcher(email.trim())
                                    .matches() -> {
                                    errorMessage =
                                        "Введите корректный email"
                                }

                                else -> {
                                    scope.launch {
                                        isSubmitting = true
                                        errorMessage = null

                                        try {
                                            val orderId = withTimeout(25_000) {
                                                createOrderInKotlin(
                                                    pickpointId =
                                                        selectedPointId!!,
                                                    recipientName =
                                                        recipientName,
                                                    recipientPhone =
                                                        phone.trim(),
                                                    recipientEmail =
                                                        email.trim()
                                                )
                                            }

                                            CartStore.clear()

                                            navController.navigate(
                                                "order_detail/$orderId"
                                            ) {
                                                popUpTo("catalog") {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        } catch (
                                            e: CancellationException
                                        ) {
                                            throw e
                                        } catch (e: Exception) {
                                            android.util.Log.e(
                                                "CheckoutScreen",
                                                "Ошибка оформления заказа",
                                                e
                                            )

                                            errorMessage = e.message
                                                ?: "Не удалось оформить заказ"
                                        } finally {
                                            isSubmitting = false
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isSubmitting &&
                                !isLoading &&
                                pointsLoaded &&
                                CartStore.lines.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue,
                            contentColor = BrandInk
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = BrandInk
                            )
                        } else {
                            Text(
                                "Оформить заказ",
                                fontFamily = DefaultFont,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(5.dp))

                    Text(
                        buildAnnotatedString {
                            withStyle(
                                SpanStyle(color = BrandMuted)
                            ) {
                                append("Оформляя, я соглашаюсь с ")
                            }
                            withStyle(
                                SpanStyle(color = BrandInk)
                            ) {
                                append(
                                    "правилами Интернет-магазина COSMICS"
                                )
                            }
                        },
                        fontFamily = DefaultFont,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = BrandPrimary,
                    strokeWidth = 2.dp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 18.dp,
                        bottom = 22.dp
                    )
            ) {
                Text(
                    "Пункт выдачи товаров",
                    fontFamily = DefaultFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandInk
                )

                Spacer(Modifier.height(10.dp))

                when {
                    !errorMessage.isNullOrBlank() && !pointsLoaded -> {
                        Text(
                            errorMessage.orEmpty(),
                            fontFamily = DefaultFont,
                            fontSize = 12.sp,
                            color = BrandError
                        )
                    }

                    points.isEmpty() -> {
                        Text(
                            "Пункты выдачи пока не добавлены.",
                            fontFamily = DefaultFont,
                            fontSize = 12.sp,
                            color = BrandMuted
                        )
                    }

                    else -> {
                        points.forEachIndexed { index, point ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPointId = point.id
                                    }
                                    .padding(vertical = 9.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top
                                ) {
                                    RadioButton(
                                        selected =
                                            selectedPointId == point.id,
                                        onClick = {
                                            selectedPointId = point.id
                                        },
                                        modifier = Modifier
                                            .size(28.dp),
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = BrandPrimary
                                        )
                                    )

                                    Spacer(Modifier.width(7.dp))

                                    Column(
                                        Modifier.weight(1f)
                                    ) {
                                        Text(
                                            "ПВЗ ${point.addressName}",
                                            fontFamily = DefaultFont,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = BrandInk
                                        )

                                        Spacer(Modifier.height(3.dp))

                                        Text(
                                            point.address,
                                            fontFamily = DefaultFont,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            color = BrandInk
                                        )

                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            "Режим работы: ${point.workTime.orEmpty()}",
                                            fontFamily = DefaultFont,
                                            fontSize = 11.sp,
                                            color = BrandMuted
                                        )
                                    }
                                }
                            }

                            if (index < points.lastIndex) {
                                HorizontalDivider(
                                    color = BrandBorder,
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }

                SectionDivider()

                Text(
                    "Дата выдачи",
                    fontFamily = DefaultFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandInk
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    "Соберем в магазине за 24 часа",
                    fontFamily = DefaultFont,
                    fontSize = 12.sp,
                    color = BrandMuted
                )

                SectionDivider()

                Text(
                    "Получатель",
                    fontFamily = DefaultFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandInk
                )

                Spacer(Modifier.height(10.dp))

                CustomTextField(
                    firstName,
                    { firstName = it },
                    "Имя",
                    width = 360.dp
                )

                Spacer(Modifier.height(8.dp))

                CustomTextField(
                    phone,
                    { phone = it },
                    "Номер телефона",
                    width = 360.dp
                )

                Spacer(Modifier.height(8.dp))

                CustomTextField(
                    email,
                    { email = it },
                    "Email",
                    width = 360.dp
                )

                SectionDivider()

                Text(
                    "Оплата",
                    fontFamily = DefaultFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandInk
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    "При получении",
                    fontFamily = DefaultFont,
                    fontSize = 12.sp,
                    color = BrandMuted
                )

                if (!errorMessage.isNullOrBlank() && pointsLoaded) {
                    Spacer(Modifier.height(10.dp))

                    Text(
                        errorMessage.orEmpty(),
                        fontFamily = DefaultFont,
                        fontSize = 12.sp,
                        color = BrandError
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(
            top = 16.dp,
            bottom = 13.dp
        ),
        thickness = 1.dp,
        color = BrandBorder
    )
}

private data class StockChange(
    val productId: Long,
    val previousQuantity: String,
    val newQuantity: String
)

private suspend fun createOrderInKotlin(
    pickpointId: Long,
    recipientName: String,
    recipientPhone: String,
    recipientEmail: String
): Long {
    val userId = supabase.auth.currentUserOrNull()?.id
        ?: error("Войдите в аккаунт, чтобы оформить заказ")

    val cartLines = CartStore.lines.toList()
    require(cartLines.isNotEmpty()) {
        "Корзина пуста"
    }

    val products = cartLines.associate { line ->
        val product = supabase.postgrest["Product"].select {
            filter {
                eq("product_id", line.product.productId)
            }
        }.decodeSingleOrNull<Product>()
            ?: error("Товар «${line.product.name}» больше не найден")

        val stock = product.quantity
            ?.trim()
            ?.toIntOrNull()
            ?: 0

        if (stock <= 0) {
            error("Товар «${product.name}» закончился")
        }

        if (line.quantity > stock) {
            error(
                "Для товара «${product.name}» осталось только $stock шт."
            )
        }

        line.product.productId to product
    }

    val brands = supabase.postgrest["Brand"]
        .select()
        .decodeList<Brand>()
        .associate { it.brandId to it.name }

    val categories = supabase.postgrest["Category"]
        .select()
        .decodeList<Category>()
        .associate { it.categoryId to it.name }

    val stockChanges = mutableListOf<StockChange>()
    var createdOrderId: Long? = null

    try {
        for (line in cartLines) {
            val product = products.getValue(line.product.productId)
            val previous = product.quantity?.trim().orEmpty()
            val oldStock = previous.toIntOrNull() ?: 0

            if (line.quantity > oldStock) {
                error(
                    "Для товара «${product.name}» осталось только $oldStock шт."
                )
            }

            val newStock = oldStock - line.quantity

            stockChanges += StockChange(
                productId = product.productId,
                previousQuantity = previous,
                newQuantity = newStock.toString()
            )

            supabase.postgrest["Product"].update(
                mapOf("quantity" to newStock.toString())
            ) {
                filter {
                    eq("product_id", product.productId)
                    eq("quantity", previous)
                }
            }

            val savedProduct = supabase.postgrest["Product"].select {
                filter {
                    eq("product_id", product.productId)
                }
            }.decodeSingleOrNull<Product>()

            val savedStock = savedProduct
                ?.quantity
                ?.trim()
                ?.toIntOrNull()

            if (savedStock != newStock) {
                error(
                    "Не удалось обновить остаток товара «${product.name}». " +
                            "Проверьте права UPDATE в Supabase и актуальный остаток."
                )
            }
        }

        val total = cartLines.sumOf { line ->
            val rawPrice = products
                .getValue(line.product.productId)
                .price
                .orEmpty()
                .replace(',', '.')
                .filter { it.isDigit() || it == '.' }

            (rawPrice.toDoubleOrNull() ?: 0.0) * line.quantity
        }

        val numberOrder = generateUniqueOrderNumber()

        val orderInsert = OrderInsert(
            status = "В процессе",
            totalPrice = String.format(
                java.util.Locale.US,
                "%.2f",
                total
            ),
            pickupPointId = pickpointId,
            userId = userId,
            numberOrder = numberOrder,
            recipientName = recipientName,
            recipientPhone = recipientPhone,
            recipientEmail = recipientEmail
        )

        val createdOrder = supabase.postgrest["Orders"]
            .insert(orderInsert) {
                select()
            }
            .decodeSingle<StoreOrder>()

        createdOrderId = createdOrder.id

        for (line in cartLines) {
            val product = products.getValue(line.product.productId)

            val itemInsert = OrderItemInsert(
                orderId = createdOrder.id,
                productId = product.productId,
                quantity = line.quantity.toString(),
                name = product.name,
                brand = product.brandId
                    ?.let { brands[it] }
                    ?: line.product.brandName
                    ?: "",
                category = product.categoryId
                    ?.let { categories[it] }
                    ?: line.product.categoryName
                    ?: "",
                price = product.price,
                productImage = product.image,
                size = product.size,
                color = product.color
            )

            supabase.postgrest["Order_items"].insert(itemInsert)
        }

        return createdOrder.id
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val orderId = createdOrderId

        if (orderId != null) {
            runCatching {
                supabase.postgrest["Order_items"].delete {
                    filter {
                        eq("order_id", orderId)
                    }
                }
            }

            runCatching {
                supabase.postgrest["Orders"].delete {
                    filter {
                        eq("orders_id", orderId)
                    }
                }
            }
        }

        stockChanges.asReversed().forEach { change ->
            runCatching {
                supabase.postgrest["Product"].update(
                    mapOf("quantity" to change.previousQuantity)
                ) {
                    filter {
                        eq("product_id", change.productId)
                        eq("quantity", change.newQuantity)
                    }
                }
            }
        }

        throw e
    }
}

private suspend fun generateUniqueOrderNumber(): String {
    repeat(30) {
        val digits = buildString {
            repeat(11) {
                append(Random.nextInt(0, 10))
            }
        }

        val candidate = "T$digits"

        val existing = supabase.postgrest["Orders"].select {
            filter {
                eq("number_order", candidate)
            }
        }.decodeList<StoreOrder>()

        if (existing.isEmpty()) {
            return candidate
        }
    }

    error(
        "Не удалось сгенерировать уникальный номер заказа. Попробуйте ещё раз."
    )
}
