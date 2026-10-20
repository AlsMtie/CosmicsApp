package com.example.cosmicsapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cosmicsapp.CartStore
import com.example.cosmicsapp.CosmicsBottomNav
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.CustomButton
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
fun CategoryScreen(
    navController: NavController,
    categoryId: Long,
    categoryName: String
) {
    var products by remember(categoryId) { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember(categoryId) { mutableStateOf(true) }
    var errorMessage by remember(categoryId) { mutableStateOf<String?>(null) }
    var retryKey by remember(categoryId) { mutableIntStateOf(0) }

    LaunchedEffect(categoryId, retryKey) {
        isLoading = true
        errorMessage = null
        try {
            products = withTimeout(12_000) {
                val baseProducts = supabase.postgrest["Product"].select {
                    filter { eq("category_id", categoryId) }
                }.decodeList<Product>()
                val brands = supabase.postgrest["Brand"].select().decodeList<Brand>()
                baseProducts.map { product ->
                    product.copy(
                        brandName = brands.firstOrNull { it.brandId == product.brandId }?.name,
                        categoryName = categoryName
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = e.message ?: "Не удалось загрузить товары"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = { CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() }) },
        bottomBar = {
            CosmicsBottomNav(currentRoute = "category") { route ->
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Text(
                categoryName,
                modifier = Modifier.padding(top = 17.dp, bottom = 13.dp),
                fontFamily = DefaultFont,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = BrandInk
            )
            when {
                isLoading -> Box(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp) }

                errorMessage != null -> Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Не удалось загрузить товары", fontFamily = DefaultFont, color = BrandMuted)
                    TextButton(onClick = { retryKey++ }) {
                        Text("Повторить", fontFamily = DefaultFont, color = BrandPrimary)
                    }
                }

                products.isEmpty() -> Box(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("В этой категории пока нет товаров", fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted)
                }

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(products, key = { it.productId }) { product ->
                        ProductCard(
                            product = product,
                            onOpen = { navController.navigate("product/${product.productId}") },
                            onAddToCart = { CartStore.add(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onOpen: () -> Unit,
    onAddToCart: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        AsyncImage(
            model = buildImageUrl(product.image),
            contentDescription = product.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BrandBackground)
                .clickable(onClick = onOpen)
        )
        Column(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(top = 7.dp)
        ) {
            product.brandName?.takeIf { it.isNotBlank() }?.let {
                Text(it, fontFamily = DefaultFont, fontSize = 10.sp, color = BrandMuted, maxLines = 1)
            }
            Text(
                product.name,
                fontFamily = DefaultFont,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
                color = BrandInk,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(displayPrice(product.price), fontFamily = DefaultFont, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = BrandInk)
        }
        Spacer(Modifier.height(7.dp))
        CustomButton(
            text = "В корзину",
            onClick = onAddToCart,
            width = 160.dp,
            height = 29.dp
        )
    }
}
