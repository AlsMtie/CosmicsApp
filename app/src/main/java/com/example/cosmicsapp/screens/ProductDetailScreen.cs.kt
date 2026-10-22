package com.example.cosmicsapp.screens

import androidx.compose.foundation.Image
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cosmicsapp.R
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cosmicsapp.CartStore
import com.example.cosmicsapp.CosmicsBottomNav
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.buildImageUrl
import com.example.cosmicsapp.data.Brand
import com.example.cosmicsapp.data.Category
import com.example.cosmicsapp.data.Product
import com.example.cosmicsapp.displayPrice
import com.example.cosmicsapp.supabase
import com.example.cosmicsapp.ui.theme.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout

@Composable
fun ProductDetailScreen(navController: NavController, productId: Long) {
    var product by remember(productId) { mutableStateOf<Product?>(null) }
    var variants by remember(productId) { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember(productId) { mutableStateOf(true) }
    var errorMessage by remember(productId) { mutableStateOf<String?>(null) }
    var retryKey by remember(productId) { mutableIntStateOf(0) }

    LaunchedEffect(productId, retryKey) {
        isLoading = true
        errorMessage = null
        try {
            withTimeout(15_000) {
                val base = supabase.postgrest["Product"].select {
                    filter { eq("product_id", productId) }
                }.decodeSingleOrNull<Product>() ?: error("Товар не найден")

                val rawVariants = if (base.variantGroup != null) {
                    supabase.postgrest["Product"].select {
                        filter { eq("variant_group", base.variantGroup) }
                    }.decodeList<Product>()
                } else {
                    listOf(base)
                }

                val brands = supabase.postgrest["Brand"].select().decodeList<Brand>()
                val categories = supabase.postgrest["Category"].select().decodeList<Category>()
                val brandName = brands.firstOrNull { it.brandId == base.brandId }?.name
                val categoryName = categories.firstOrNull { it.categoryId == base.categoryId }?.name

                variants = rawVariants
                    .ifEmpty { listOf(base) }
                    .map { it.copy(brandName = brandName, categoryName = categoryName) }
                    .distinctBy { it.productId }

                product = variants.firstOrNull { it.productId == productId }
                    ?: base.copy(brandName = brandName, categoryName = categoryName)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = e.message ?: "Не удалось загрузить товар"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() })
        },
        bottomBar = {
            Column {
                product?.let { item ->
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
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
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
                                        .background(Color.White, RoundedCornerShape(7.dp)),
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
                                            painter = painterResource(id = R.drawable.minus),
                                            contentDescription = "Уменьшить количество",
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
                                            painter = painterResource(id = R.drawable.plus),
                                            contentDescription = "Увеличить количество",
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
                CosmicsBottomNav(currentRoute = "product") { route ->
                    navController.navigate(route) { launchSingleTop = true }
                }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        when {
            isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp)
            }

            errorMessage != null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(errorMessage.orEmpty(), fontFamily = DefaultFont, color = BrandMuted)
                TextButton(onClick = { retryKey++ }) {
                    Text("Повторить", color = BrandPrimary, fontFamily = DefaultFont)
                }
            }

            product != null -> {
                val item = product!!
                val sizeOptions = variants.mapNotNull { it.size?.trim()?.takeIf(String::isNotBlank) }.distinct()
                val colorOptions = variants.mapNotNull { it.color?.trim()?.takeIf(String::isNotBlank) }.distinct()

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
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 13.dp)
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
                            displayPrice(item.price),
                            fontFamily = DefaultFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandInk
                        )

                        if (sizeOptions.isNotEmpty()) {
                            HorizontalDivider(
                                Modifier.padding(top = 8.dp, bottom = 6.dp),
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
                                        val sizeVariant = variants.firstOrNull {
                                            it.size?.trim() == sizeOption
                                        }
                                        val sizeOutOfStock =
                                            (sizeVariant?.quantity?.trim()?.toIntOrNull() ?: 0) <= 0

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
                                                    sizeVariant?.let { product = it }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = sizeOption
                                                    .filter { it.isDigit() || it == '.' || it == ',' }
                                                    .ifBlank { sizeOption },
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
                                                        start = Offset(
                                                            size.width * 0.2f,
                                                            size.height * 0.8f
                                                        ),
                                                        end = Offset(
                                                            size.width * 0.8f,
                                                            size.height * 0.2f
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        } else if (colorOptions.isNotEmpty()) {
                            HorizontalDivider(
                                Modifier.padding(top = 8.dp, bottom = 8.dp),
                                color = BrandBorder
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Цвет",
                                    fontFamily = DefaultFont,
                                    fontSize = 14.sp,
                                    color = BrandMuted
                                )
                                colorOptions.forEach { colorName ->
                                    val selected = item.color?.trim() == colorName
                                    val colorVariant = variants.firstOrNull { it.color?.trim() == colorName }
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
                                                colorVariant?.let { product = it }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (swatch == BrandSurface || swatch == BrandBackground) {
                                            Text(
                                                colorName.take(3),
                                                fontFamily = DefaultFont,
                                                fontSize = 9.sp,
                                                color = if (colorOutOfStock) BrandMuted else BrandInk,
                                                maxLines = 1
                                            )
                                        }
                                        if (colorOutOfStock) {
                                            Canvas(Modifier.matchParentSize()) {
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
                            Modifier.padding(top = 12.dp, bottom = 12.dp),
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
        Color(AndroidColor.parseColor(normalized))
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
