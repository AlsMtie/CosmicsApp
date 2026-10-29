package com.example.cosmicsapp.utils


fun displayTotal(raw: String?): String {
    val value = raw?.trim().orEmpty()
    val normalized = value.replace(",", ".").filter { it.isDigit() || it == '.' }
    val amount = normalized.toBigDecimalOrNull() ?: return value.ifBlank { "0" }
    return amount.stripTrailingZeros().toPlainString()
}

fun russianItems(count: Int): String = when {
    count % 100 in 11..14 -> "товаров"
    count % 10 == 1 -> "товар"
    count % 10 in 2..4 -> "товара"
    else -> "товаров"
}

fun formatOrderDate(value: String): String {
    val date = value.take(10)
    val time = value.substringAfter('T', "").take(5)
    return if (time.isBlank()) date else "$date в $time"
}