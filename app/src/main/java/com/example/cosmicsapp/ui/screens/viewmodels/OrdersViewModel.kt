package com.example.cosmicsapp.ui.screens.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmicsapp.data.Product
import com.example.cosmicsapp.data.StoreOrder
import com.example.cosmicsapp.data.StoreOrderItem
import com.example.cosmicsapp.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class OrdersViewModel : ViewModel() {

    var orders by mutableStateOf<List<StoreOrder>>(emptyList())
    var loading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)

    private var allOrderItems by mutableStateOf<List<StoreOrderItem>>(emptyList())
    private var allProductImages by mutableStateOf<Map<Long, String?>>(emptyMap())

    fun loadOrders() {
        viewModelScope.launch {
            loading = true
            error = null

            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId == null) {
                error = "Войдите в аккаунт, чтобы увидеть заказы"
                loading = false
                return@launch
            }

            try {
                withTimeout(15_000) {
                    orders = supabase.postgrest["Orders"]
                        .select { filter { eq("user_id", userId) } }
                        .decodeList<StoreOrder>()
                        .sortedByDescending { it.date }

                    allOrderItems = if (orders.isEmpty()) {
                        emptyList()
                    } else {
                        supabase.postgrest["Order_items"].select().decodeList<StoreOrderItem>()
                    }

                    allProductImages = if (allOrderItems.isEmpty()) {
                        emptyMap()
                    } else {
                        supabase.postgrest["Product"].select().decodeList<Product>()
                            .associate { it.productId to it.image }
                    }
                }
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки истории"
            } finally {
                loading = false
            }
        }
    }

    fun getOrderItemCount(orderId: Long): Int {
        return allOrderItems.filter { it.orderId == orderId }.sumOf { it.quantity.toIntOrNull() ?: 0 }
    }

    fun getOrderPreviewImage(orderId: Long): String? {
        val items = allOrderItems.filter { it.orderId == orderId }
        val item = items.firstOrNull() ?: return null
        return item.productImage ?: allProductImages[item.productId]
    }
}