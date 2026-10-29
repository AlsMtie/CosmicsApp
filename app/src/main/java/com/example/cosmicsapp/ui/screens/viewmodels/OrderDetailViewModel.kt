package com.example.cosmicsapp.ui.screens.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmicsapp.data.*
import com.example.cosmicsapp.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class OrderDetailViewModel : ViewModel() {

    var order by mutableStateOf<StoreOrder?>(null)
    var orderLines by mutableStateOf<List<StoreOrderItem>>(emptyList())
    var loading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)

    fun loadOrder(orderId: Long) {
        viewModelScope.launch {
            loading = true
            error = null
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch

            try {
                withTimeout(15_000) {
                    order = supabase.postgrest["Orders"].select {
                        filter { eq("orders_id", orderId); eq("user_id", userId) }
                    }.decodeSingleOrNull<StoreOrder>()

                    if (order == null) throw IllegalStateException("Заказ не найден")

                    val fetchedLines = supabase.postgrest["Order_items"].select { filter { eq("order_id", orderId) } }.decodeList<StoreOrderItem>()
                    val productImages = supabase.postgrest["Product"].select().decodeList<Product>().associate { it.productId to it.image }

                    orderLines = fetchedLines.map { line ->
                        line.copy(productImage = line.productImage ?: productImages[line.productId])
                    }
                }
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки заказа"
            } finally {
                loading = false
            }
        }
    }
}