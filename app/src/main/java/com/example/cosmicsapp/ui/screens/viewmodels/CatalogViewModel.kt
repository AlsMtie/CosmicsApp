package com.example.cosmicsapp.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmicsapp.data.Category
import com.example.cosmicsapp.data.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class CatalogViewModel : ViewModel() {

    var categories by mutableStateOf<List<Category>>(emptyList())
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)

    fun loadCategories() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                categories = withTimeout(12_000) {
                    supabase.postgrest["Category"]
                        .select()
                        .decodeList<Category>()
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Не удалось загрузить категории"
            } finally {
                isLoading = false
            }
        }
    }
}