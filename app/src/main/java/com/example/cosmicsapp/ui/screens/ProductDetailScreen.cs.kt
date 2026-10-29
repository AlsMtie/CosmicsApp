package com.example.cosmicsapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cosmicsapp.R
import com.example.cosmicsapp.utils.buildImageUrl
import com.example.cosmicsapp.data.CartStore
import com.example.cosmicsapp.ui.components.*
import com.example.cosmicsapp.ui.screens.viewmodels.ProductDetailViewModel
import com.example.cosmicsapp.ui.theme.*

@Composable
fun ProductDetailScreen(
    navController: NavController,
    productId: Long,
    viewModel: ProductDetailViewModel = viewModel()
) {
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    Scaffold(
        topBar = {
            CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() })
        },
        bottomBar = {
            Column {
                viewModel.product?.let { item ->
                    val quantityInCart = CartStore.lines
                        .firstOrNull { it.product.productId == item.productId }?.quantity ?: 0
                    val stock = item.quantity?.trim()?.toIntOrNull() ?: 0
                    val outOfStock = stock <= 0

                    Button(
                        onClick = {
                            if (!outOfStock && quantityInCart == 0) CartStore.add(item)
                        },
                        enabled = !outOfStock,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .height(58.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue,
                            contentColor = BrandInk
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (quantityInCart > 0) {
                                Row(
                                    modifier = Modifier
                                        .width(105.dp)
                                        .height(34.dp)
                                        .background(BrandSurface, RoundedCornerShape(7.dp)),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable { CartStore.removeOne(item.productId) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.minus),
                                            contentDescription = "Уменьшить",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Text(
                                        quantityInCart.toString(),
                                        fontFamily = DefaultFont,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BrandInk
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable(enabled = quantityInCart < stock) { CartStore.add(item) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.plus),
                                            contentDescription = "Увеличить",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    "В корзину",
                                    fontFamily = DefaultFont,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                displayPrice(item.price),
                                fontFamily = DefaultFont,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                CosmicsBottomNav("product") { route ->
                    navController.navigate(route) { launchSingleTop = true }
                }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        when {
            viewModel.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp)
                }
            }
            viewModel.errorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(viewModel.errorMessage!!, fontFamily = DefaultFont, color = BrandMuted)
                    TextButton(onClick = { viewModel.loadProduct(productId) }) {
                        Text("Повторить", color = BrandPrimary, fontFamily = DefaultFont)
                    }
                }
            }
            viewModel.product != null -> {
                val item = viewModel.product!!
                val sizeOptions = viewModel.variants.mapNotNull { it.size?.trim()?.takeIf(String::isNotBlank) }.distinct()
                val colorOptions = viewModel.variants.mapNotNull { it.color?.trim()?.takeIf(String::isNotBlank) }.distinct()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(18.dp))
                    AsyncImage(
                        model = buildImageUrl(item.image),
                        contentDescription = item.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(270.dp)
                            .padding(horizontal = 18.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 13.dp)
                    ) {
                        Text(
                            text = item.brandName.orEmpty(),
                            fontFamily = DefaultFont,
                            fontSize = 13.sp,
                            color = BrandMuted
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = item.name,
                            fontFamily = DefaultFont,
                            fontSize = 25.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandInk
                        )
                        item.categoryName?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, fontFamily = DefaultFont, fontSize = 13.sp, color = BrandMuted)
                        }
                        Spacer(Modifier.height(9.dp))
                        Text(
                            text = displayPrice(item.price),
                            fontFamily = DefaultFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandInk
                        )

                        if (sizeOptions.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
                                color = BrandBorder
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "мл",
                                    modifier = Modifier.width(29.dp),
                                    fontFamily = DefaultFont,
                                    fontSize = 16.sp,
                                    color = BrandMuted
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    sizeOptions.forEach { sizeOption ->
                                        val selected = item.size?.trim() == sizeOption
                                        val sizeVariant = viewModel.variants.firstOrNull { it.size?.trim() == sizeOption }
                                        val sizeOutOfStock = (sizeVariant?.quantity?.trim()?.toIntOrNull() ?: 0) <= 0

                                        Box(
                                            modifier = Modifier
                                                .size(35.dp)
                                                .border(
                                                    width = if (selected) 2.dp else 1.dp,
                                                    color = if (selected) BrandPrimary else BrandBlue,
                                                    shape = CircleShape
                                                )
                                                .background(BrandSurface, CircleShape)
                                                .clickable(enabled = !sizeOutOfStock) {
                                                    sizeVariant?.let { viewModel.selectVariant(it) }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = sizeOption.filter { it.isDigit() || it == '.' || it == ',' }.ifBlank { sizeOption },
                                                fontFamily = DefaultFont,
                                                fontSize = 14.sp,
                                                color = if (sizeOutOfStock) BrandMuted else BrandInk,
                                                maxLines = 1
                                            )
                                            if (sizeOutOfStock) {
                                                Canvas(modifier = Modifier.matchParentSize()) {
                                                    drawLine(
                                                        color = Color(0xFF518AA8),
                                                        strokeWidth = 1.dp.toPx(),
                                                        start = Offset(size.width * 0.2f, size.height * 0.8f),
                                                        end = Offset(size.width * 0.8f, size.height * 0.2f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (colorOptions.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                                color = BrandBorder
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("Цвет", fontFamily = DefaultFont, fontSize = 14.sp, color = BrandMuted)
                                colorOptions.forEach { colorName ->
                                    val selected = item.color?.trim() == colorName
                                    val colorVariant = viewModel.variants.firstOrNull { it.color?.trim() == colorName }
                                    val colorOutOfStock = (colorVariant?.quantity?.trim()?.toIntOrNull() ?: 0) <= 0
                                    val swatch = colorFromValue(colorName)

                                    Box(
                                        modifier = Modifier
                                            .size(35.dp)
                                            .border(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) BrandPrimary else BrandBlue,
                                                shape = CircleShape
                                            )
                                            .padding(3.dp)
                                            .background(swatch, CircleShape)
                                            .clickable(enabled = !colorOutOfStock) {
                                                colorVariant?.let { viewModel.selectVariant(it) }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (swatch == BrandSurface || swatch == BrandBackground) {
                                            Text(
                                                text = colorName.take(3),
                                                fontFamily = DefaultFont,
                                                fontSize = 9.sp,
                                                color = if (colorOutOfStock) BrandMuted else BrandInk,
                                                maxLines = 1
                                            )
                                        }
                                        if (colorOutOfStock) {
                                            Canvas(modifier = Modifier.matchParentSize()) {
                                                drawLine(
                                                    color = BrandMuted,
                                                    start = Offset(size.width * 0.15f, size.height * 0.85f),
                                                    end = Offset(size.width * 0.85f, size.height * 0.15f),
                                                    strokeWidth = 1.dp.toPx()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
                            color = BrandBorder
                        )
                    }

                    Text(
                        text = "Описание",
                        modifier = Modifier.padding(start = 30.dp, end = 60.dp),
                        fontFamily = DefaultFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = BrandInk
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = item.description.orEmpty(),
                        modifier = Modifier.padding(start = 60.dp, end = 60.dp, bottom = 22.dp),
                        fontFamily = DefaultFont,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = BrandInk
                    )
                }
            }
        }
    }
}

private fun colorFromValue(value: String): Color {
    val normalized = value.trim()
    return try {
        Color(android.graphics.Color.parseColor(normalized))
    } catch (_: IllegalArgumentException) {
        when (normalized.lowercase()) {
            "белый", "white" -> Color.White
            "черный", "чёрный", "black" -> Color(0xFF242424)
            "красный", "red" -> Color(0xFFC94B4B)
            "розовый", "pink" -> Color(0xFFE8A4B8)
            "синий", "blue" -> Color(0xFF518AA8)
            "зеленый", "зелёный", "green" -> Color(0xFF78A878)
            "коричневый", "brown" -> Color(0xFF8D6E63)
            else -> BrandSurface
        }
    }
}