package com.example.cosmicsapp.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    val image: String? = null,
    @SerialName("category_id") val categoryId: Long? = null,
    @SerialName("brand_id") val brandId: Long? = null,
    val size: String? = null,
    val color: String? = null,
    val quantity: String? = null,
    @Transient val brandName: String? = null,
    @Transient val categoryName: String? = null
)

@Serializable
data class PickupPoint(
    @SerialName("pickpoint_id") val id: Long,
    @SerialName("address_name") val addressName: String,
    val address: String,
    val phone: String? = null,
    @SerialName("work_time") val workTime: String? = null
)

@Serializable
data class UserProfile(
    @SerialName("user_id") val userId: String,
    val email: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val phone: String? = null
)

@Serializable
data class StoreOrder(
    @SerialName("orders_id") val id: Long,
    val status: String,
    @SerialName("total_price") val totalPrice: String? = null,
    @SerialName("pickpoint_id") val pickupPointId: Long? = null,
    val date: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("number_order") val numberOrder: String? = null,
    @SerialName("recipient_name") val recipientName: String? = null,
    @SerialName("recipient_phone") val recipientPhone: String? = null,
    @SerialName("recipient_email") val recipientEmail: String? = null
)

@Serializable
data class StoreOrderItem(
    @SerialName("items_id") val id: Long? = null,
    @SerialName("order_id") val orderId: Long,
    @SerialName("product_id") val productId: Long,
    val quantity: String,
    val name: String,
    val brand: String,
    val category: String,
    val price: String? = null,
    @SerialName("product_image") val productImage: String? = null,
    val size: String? = null,
    val color: String? = null
)

@Serializable
data class PlacedOrder(
    @SerialName("orders_id") val id: Long,
    @SerialName("number_order") val numberOrder: String
)
