package com.example.cosmicsapp.ui.screens.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmicsapp.data.Brand
import com.example.cosmicsapp.data.Category
import com.example.cosmicsapp.data.Product
import com.example.cosmicsapp.data.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ProductDetailViewModel : ViewModel() {

    var product by mutableStateOf<Product?>(null)
    var variants by mutableStateOf<List<Product>>(emptyList())
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)

    fun loadProduct(productId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                withTimeout(15_000) {
                    val base = supabase.postgrest["Product"].select {
                        filter { eq("product_id", productId) }
                    }.decodeSingleOrNull<Product>() ?: error("Товар не найден")

                    val rawVariants = if (base.variantGroup != null) {
                        supabase.postgrest["Product"].select {
                            filter { eq("variant_group", base.variantGroup) }
                        }.decodeList<Product>()
                    } else {
                        listOf(base)
                    }

                    val brands = supabase.postgrest["Brand"].select().decodeList<Brand>()
                    val categories = supabase.postgrest["Category"].select().decodeList<Category>()

                    val brandName = brands.firstOrNull { it.brandId == base.brandId }?.name
                    val categoryName = categories.firstOrNull { it.categoryId == base.categoryId }?.name

                    variants = rawVariants
                        .ifEmpty { listOf(base) }
                        .map { it.copy(brandName = brandName, categoryName = categoryName) }
                        .distinctBy { it.productId }

                    product = variants.firstOrNull { it.productId == productId }
                        ?: base.copy(brandName = brandName, categoryName = categoryName)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Не удалось загрузить товар"
            } finally {
                isLoading = false
            }
        }
    }

    fun selectVariant(newProduct: Product) {
        product = newProduct
    }
}