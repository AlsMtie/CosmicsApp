package com.example.cosmicsapp.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmicsapp.data.Brand
import com.example.cosmicsapp.data.Product
import com.example.cosmicsapp.data.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class CategoryViewModel : ViewModel() {

    var products by mutableStateOf<List<Product>>(emptyList())
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)

    fun loadProducts(categoryId: Long, categoryName: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                products = withTimeout(12_000) {
                    val baseProducts = supabase.postgrest["Product"]
                        .select { filter { eq("category_id", categoryId) } }
                        .decodeList<Product>()

                    val brands = supabase.postgrest["Brand"]
                        .select()
                        .decodeList<Brand>()

                    baseProducts.map { product ->
                        product.copy(
                            brandName = brands.firstOrNull { it.brandId == product.brandId }?.name,
                            categoryName = categoryName
                        )
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Ошибка загрузки"
            } finally {
                isLoading = false
            }
        }
    }
}