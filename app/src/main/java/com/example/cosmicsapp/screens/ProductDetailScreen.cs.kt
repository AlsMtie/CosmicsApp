package com.example.cosmicsapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var addedToCart by remember { mutableStateOf(false) }

    LaunchedEffect(productId, retryKey) {
        isLoading = true
        errorMessage = null
        try {
            withTimeout(15_000) {
                val base = supabase.postgrest["Product"].select {
                    filter { eq("product_id", productId) }
                }.decodeSingleOrNull<Product>() ?: error("Товар не найден")

                val brandId = base.brandId
                val categoryId = base.categoryId
                val rawVariants = supabase.postgrest["Product"].select {
                    filter {
                        eq("name", base.name)
                        if (brandId != null) eq("brand_id", brandId)
                        if (categoryId != null) eq("category_id", categoryId)
                    }
                }.decodeList<Product>()

                val brands = supabase.postgrest["Brand"].select().decodeList<Brand>()
                val categories = supabase.postgrest["Category"].select().decodeList<Category>()
                val brandName = brands.firstOrNull { it.brandId == base.brandId }?.name
                val categoryName = categories.firstOrNull { it.categoryId == base.categoryId }?.name

                variants = (rawVariants.ifEmpty { listOf(base) }).map {
                    it.copy(brandName = brandName, categoryName = categoryName)
                }.distinctBy { it.productId }
                product = variants.firstOrNull { it.productId == productId } ?: base.copy(
                    brandName = brandName,
                    categoryName = categoryName
                )
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
        topBar = { CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() }) },
        bottomBar = {
            Column {
                product?.let { item ->
                    Button(
                        onClick = {
                            CartStore.add(item)
                            addedToCart = true
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, contentColor = BrandInk),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (addedToCart) "Добавлено в корзину" else "В корзину", fontFamily = DefaultFont, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(displayPrice(item.price), fontFamily = DefaultFont, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp)
            }
            errorMessage != null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(errorMessage.orEmpty(), fontFamily = DefaultFont, color = BrandMuted)
                TextButton(onClick = { retryKey++ }) { Text("Повторить", color = BrandPrimary, fontFamily = DefaultFont) }
            }
            product != null -> {
                val item = product!!
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(18.dp))
                    AsyncImage(
                        model = buildImageUrl(item.image),
                        contentDescription = item.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().height(270.dp).padding(horizontal = 18.dp)
                    )
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 13.dp)) {
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
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandInk
                        )

                        val sizeOptions = variants.mapNotNull { it.size?.trim()?.takeIf(String::isNotBlank) }.distinct()
                        if (sizeOptions.isNotEmpty()) {
                            HorizontalDivider(Modifier.padding(top = 14.dp, bottom = 12.dp), color = BrandBorder)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Объём, мл",
                                    modifier = Modifier.width(82.dp),
                                    fontFamily = DefaultFont,
                                    fontSize = 12.sp,
                                    color = BrandInk
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
                                    sizeOptions.forEach { size ->
                                        val selected = item.size?.trim() == size
                                        Box(
                                            modifier = Modifier.size(35.dp)
                                                .border(
                                                    width = if (selected) 1.5.dp else 1.dp,
                                                    color = if (selected) BrandPrimary else BrandBlue,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    variants.firstOrNull { it.size?.trim() == size }?.let { product = it }
                                                    addedToCart = false
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                size.filter { it.isDigit() || it == '.' || it == ',' }.ifBlank { size },
                                                fontFamily = DefaultFont,
                                                fontSize = 10.sp,
                                                color = BrandInk,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item.color?.takeIf { it.isNotBlank() }?.let { color ->
                            Spacer(Modifier.height(10.dp))
                            Text("Цвет: $color", fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted)
                        }

                        HorizontalDivider(Modifier.padding(top = 14.dp, bottom = 14.dp), color = BrandBorder)
                    }
                    Text(
                        text = "Описание",
                        modifier = Modifier.padding(start = 60.dp, end = 60.dp),
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
