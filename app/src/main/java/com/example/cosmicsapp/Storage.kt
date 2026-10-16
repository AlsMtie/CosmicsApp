package com.example.cosmicsapp

fun buildImageUrl(fileName: String?, bucket: String = "products"): String? {
    if (fileName.isNullOrBlank()) return null
    return "https://xxqflvscopblihlrjfxu.supabase.co/storage/v1/object/public/$bucket/$fileName"
}