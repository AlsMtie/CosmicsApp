package com.example.cosmicsapp


import androidx.compose.runtime.mutableStateListOf
import com.example.cosmicsapp.data.Product

data class CartLine(val product: Product, val quantity: Int = 1)

object CartStore {
    val lines = mutableStateListOf<CartLine>()

    fun add(product: Product) {
        val stock = product.quantity?.trim()?.toIntOrNull() ?: 0
        if (stock <= 0) return

        val index = lines.indexOfFirst { it.product.productId == product.productId }
        if (index >= 0) {
            val old = lines[index]
            if (old.quantity < stock) {
                lines[index] = old.copy(quantity = old.quantity + 1, product = product)
            }
        } else {
            lines.add(CartLine(product))
        }
    }

    fun removeOne(productId: Long) {
        val index = lines.indexOfFirst { it.product.productId == productId }
        if (index >= 0) {
            val old = lines[index]
            if (old.quantity > 1) lines[index] = old.copy(quantity = old.quantity - 1)
            else lines.removeAt(index)
        }
    }

    fun clear() = lines.clear()

    fun total(): Double = lines.sumOf {
        (it.product.price?.replace(",", ".")?.filter { ch -> ch.isDigit() || ch == '.' }?.toDoubleOrNull() ?: 0.0) * it.quantity
    }
}
