package com.example.cosmicsapp.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmicsapp.data.Brand
import com.example.cosmicsapp.data.CartStore
import com.example.cosmicsapp.data.Category
import com.example.cosmicsapp.data.OrderInsert
import com.example.cosmicsapp.data.OrderItemInsert
import com.example.cosmicsapp.data.PickupPoint
import com.example.cosmicsapp.data.Product
import com.example.cosmicsapp.data.StoreOrder
import com.example.cosmicsapp.data.UserProfile
import com.example.cosmicsapp.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.random.Random

class CheckoutViewModel : ViewModel() {

    var points by mutableStateOf<List<PickupPoint>>(emptyList())
    var selectedPointId by mutableStateOf<Long?>(null)
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")

    var isLoading by mutableStateOf(true)
    var isSubmitting by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var pointsLoaded by mutableStateOf(false)

    fun loadData() {
        viewModelScope.launch {
            val user = supabase.auth.currentUserOrNull()
            if (user == null) {
                isLoading = false
                return@launch
            }

            email = user.email.orEmpty()

            try {
                withTimeout(15_000) {
                    points = supabase.postgrest["Pickup_points"]
                        .select()
                        .decodeList<PickupPoint>()

                    val profile = supabase.postgrest["Users"].select {
                        filter { eq("user_id", user.id) }
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
            } catch (e: Exception) {
                errorMessage = e.message ?: "Не удалось загрузить пункты выдачи"
            } finally {
                isLoading = false
            }
        }
    }

    fun submitOrder(onSuccess: (Long) -> Unit) {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            errorMessage = "Войдите в аккаунт, чтобы оформить заказ"
            return
        }
        if (CartStore.lines.isEmpty()) {
            errorMessage = "Корзина пуста"
            return
        }
        if (selectedPointId == null) {
            errorMessage = "Выберите пункт выдачи"
            return
        }

        val recipientName = listOf(firstName.trim(), lastName.trim())
            .filter { it.isNotBlank() }
            .joinToString(" ")

        if (recipientName.isBlank()) {
            errorMessage = "Введите имя и фамилию"
            return
        }
        if (phone.isBlank()) {
            errorMessage = "Введите номер телефона"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            errorMessage = "Введите корректный email"
            return
        }

        viewModelScope.launch {
            isSubmitting = true
            errorMessage = null
            try {
                val orderId = withTimeout(25_000) {
                    createOrderInKotlin(
                        pickpointId = selectedPointId!!,
                        recipientName = recipientName,
                        recipientPhone = phone.trim(),
                        recipientEmail = email.trim()
                    )
                }
                CartStore.clear()
                onSuccess(orderId)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Не удалось оформить заказ"
            } finally {
                isSubmitting = false
            }
        }
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
        require(cartLines.isNotEmpty()) { "Корзина пуста" }

        val products = cartLines.associate { line ->
            val product = supabase.postgrest["Product"].select {
                filter { eq("product_id", line.product.productId) }
            }.decodeSingleOrNull<Product>()
                ?: error("Товар «${line.product.name}» больше не найден")

            val stock = product.quantity?.trim()?.toIntOrNull() ?: 0
            if (stock <= 0) error("Товар «${product.name}» закончился")
            if (line.quantity > stock) error("Для товара «${product.name}» осталось только $stock шт.")

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
                    error("Для товара «${product.name}» осталось только $oldStock шт.")
                }

                val newStock = oldStock - line.quantity
                stockChanges += StockChange(product.productId, previous, newStock.toString())

                supabase.postgrest["Product"].update(
                    mapOf("quantity" to newStock.toString())
                ) {
                    filter {
                        eq("product_id", product.productId)
                        eq("quantity", previous)
                    }
                }

                val savedProduct = supabase.postgrest["Product"].select {
                    filter { eq("product_id", product.productId) }
                }.decodeSingleOrNull<Product>()

                if (savedProduct?.quantity?.trim()?.toIntOrNull() != newStock) {
                    error("Не удалось обновить остаток товара «${product.name}»")
                }
            }

            val total = cartLines.sumOf { line ->
                val rawPrice = products.getValue(line.product.productId).price
                    .orEmpty()
                    .replace(',', '.')
                    .filter { it.isDigit() || it == '.' }
                (rawPrice.toDoubleOrNull() ?: 0.0) * line.quantity
            }

            val numberOrder = generateUniqueOrderNumber()

            val orderInsert = OrderInsert(
                status = "В процессе",
                totalPrice = String.format(java.util.Locale.US, "%.2f", total),
                pickupPointId = pickpointId,
                userId = userId,
                numberOrder = numberOrder,
                recipientName = recipientName,
                recipientPhone = recipientPhone,
                recipientEmail = recipientEmail
            )

            val createdOrder = supabase.postgrest["Orders"]
                .insert(orderInsert) { select() }
                .decodeSingle<StoreOrder>()

            createdOrderId = createdOrder.id

            for (line in cartLines) {
                val product = products.getValue(line.product.productId)
                val itemInsert = OrderItemInsert(
                    orderId = createdOrder.id,
                    productId = product.productId,
                    quantity = line.quantity.toString(),
                    name = product.name,
                    brand = product.brandId?.let { brands[it] } ?: line.product.brandName ?: "",
                    category = product.categoryId?.let { categories[it] } ?: line.product.categoryName ?: "",
                    price = product.price,
                    productImage = product.image,
                    size = product.size,
                    color = product.color
                )
                supabase.postgrest["Order_items"].insert(itemInsert)
            }

            return createdOrder.id
        } catch (e: Exception) {
            createdOrderId?.let { orderId ->
                runCatching {
                    supabase.postgrest["Order_items"].delete { filter { eq("order_id", orderId) } }
                }
                runCatching {
                    supabase.postgrest["Orders"].delete { filter { eq("orders_id", orderId) } }
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
                repeat(11) { append(Random.nextInt(0, 10)) }
            }
            val candidate = "T$digits"
            val existing = supabase.postgrest["Orders"].select {
                filter { eq("number_order", candidate) }
            }.decodeList<StoreOrder>()

            if (existing.isEmpty()) return candidate
        }
        error("Не удалось сгенерировать уникальный номер заказа")
    }
}