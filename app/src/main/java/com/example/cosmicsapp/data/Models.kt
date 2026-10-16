package com.example.cosmicsapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    @SerialName("category_id") val categoryId: Long,
    val name: String
)

@Serializable
data class Brand(
    @SerialName("brand_id") val brandId: Long,
    val name: String,
    val logo: String? = null
)

@Serializable
data class Product(
    @SerialName("product_id") val productId: Long,
    val name: String,
    val description: String? = null,
    val price: String? = null,
    val image: String? = null,             // ← имя файла в Storage
    @SerialName("category_id") val categoryId: Long? = null,
    @SerialName("brand_id") val brandId: Long? = null,
    val size: String? = null,
    val color: String? = null,
    val quantity: String? = null
)