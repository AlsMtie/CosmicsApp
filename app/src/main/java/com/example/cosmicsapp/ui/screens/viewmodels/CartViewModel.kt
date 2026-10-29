package com.example.cosmicsapp.ui.screens.viewmodels

import androidx.lifecycle.ViewModel
import com.example.cosmicsapp.data.CartStore
import com.example.cosmicsapp.data.Product
import java.util.Locale

class CartViewModel : ViewModel() {
    val lines = CartStore.lines

    fun getTotal(): String {
        return String.format(Locale("ru", "RU"), "%.0f ₽", CartStore.total())
    }

    fun getItemCount(): Int {
        return lines.sumOf { it.quantity }
    }

    fun itemNoun(count: Int): String {
        return when {
            count % 100 in 11..14 -> "товаров"
            count % 10 == 1 -> "товар"
            count % 10 in 2..4 -> "товара"
            else -> "товаров"
        }
    }

    fun add(product: Product) {
        CartStore.add(product)
    }

    fun removeOne(productId: Long) {
        CartStore.removeOne(productId)
    }
}