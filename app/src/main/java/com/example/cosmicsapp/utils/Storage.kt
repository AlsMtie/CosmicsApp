package com.example.cosmicsapp.utils

import android.net.Uri
import com.example.cosmicsapp.data.SUPABASE_PROJECT_URL

fun buildImageUrl(
    fileName: String?,
    bucket: String = "product"
): String? {
    val value = fileName?.trim().orEmpty()

    if (value.isBlank()) return null

    if (
        value.startsWith("https://") ||
        value.startsWith("http://")
    ) {
        return value
    }

    val safePath = value
        .trimStart('/')
        .split("/")
        .joinToString("/") { Uri.encode(it) }

    return "${SUPABASE_PROJECT_URL}/storage/v1/object/public/${Uri.encode(bucket)}/$safePath"
}