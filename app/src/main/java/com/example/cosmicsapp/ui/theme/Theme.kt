package com.example.cosmicsapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CosmicsColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandSurface,
    secondary = BrandBlue,
    onSecondary = BrandInk,
    background = BrandBackground,
    onBackground = BrandInk,
    surface = BrandSurface,
    onSurface = BrandInk,
    outline = BrandBorder,
    error = BrandError,
    onError = BrandSurface
)

@Composable
fun CosmicsAppTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CosmicsColorScheme,
        typography = Typography,
        content = content
    )
}
