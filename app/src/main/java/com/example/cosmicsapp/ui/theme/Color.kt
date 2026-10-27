package com.example.cosmicsapp.ui.theme

import androidx.compose.ui.graphics.Color

var BrandBlue = Color(0xFFC7DDF2)
var BrandBackground = Color(0xFFF4F7FA)
var BrandSurface = Color(0xFFFFFFFF)
var BrandInk = Color(0xFF031927)
var BrandPrimary = Color(0xFF5795B5)
var BrandMuted = Color(0xFF757575)
var BrandBorder = Color(0xFFE0E0E0)
var BrandError = Color(0xFFD32F2F)
var BrandSuccess = Color(0xFF2E7D32)

private val BrandBlueDark = Color(0xFF2C3E50)
private val BrandBackgroundDark = Color(0xFF121212)
private val BrandSurfaceDark = Color(0xFF1E1E1E)
private val BrandInkDark = Color(0xFFE0E0E0)
private val BrandPrimaryDark = Color(0xFF7BB5D6)
private val BrandMutedDark = Color(0xFF9E9E9E)
private val BrandBorderDark = Color(0xFF333333)

fun applyLightColors() {
    BrandBlue = Color(0xFFC7DDF2)
    BrandBackground = Color(0xFFF4F7FA)
    BrandSurface = Color(0xFFFFFFFF)
    BrandInk = Color(0xFF031927)
    BrandPrimary = Color(0xFF5795B5)
    BrandMuted = Color(0xFF757575)
    BrandBorder = Color(0xFFE0E0E0)
}

fun applyDarkColors() {
    BrandBlue = BrandBlueDark
    BrandBackground = BrandBackgroundDark
    BrandSurface = BrandSurfaceDark
    BrandInk = BrandInkDark
    BrandPrimary = BrandPrimaryDark
    BrandMuted = BrandMutedDark
    BrandBorder = BrandBorderDark
}