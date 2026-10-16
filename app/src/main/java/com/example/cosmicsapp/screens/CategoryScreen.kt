
package com.example.cosmicsapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cosmicsapp.CosmicsBottomNav
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.R
import com.example.cosmicsapp.data.Product
import com.example.cosmicsapp.supabase
import com.example.cosmicsapp.ui.theme.DefaultFont
import com.example.cosmicsapp.buildImageUrl
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun CategoryScreen(
    navController: NavController,
    categoryId: Long,
    categoryName: String
) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(categoryId) {
        scope.launch {
            try {
                products = supabase.postgrest["Product"]
                    .select {
                        filter { eq("category_id", categoryId) }
                    }
                    .decodeList<Product>()
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CosmicsTopBar(
                showBack = true,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CosmicsBottomNav(
                currentRoute = "catalog",
                onNavigate = { navController.navigate(it) }
            )
        },
        containerColor = Color(0xFFF4F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Заголовок категории
            Text(
                text = categoryName,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontFamily = DefaultFont,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF031927)
                ),
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF5795B5))
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Ошибка: $errorMessage",
                            style = TextStyle(fontSize = 13.sp, color = Color(0xFF757575))
                        )
                    }
                }
                products.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "В этой категории пока нет товаров",
                            style = TextStyle(fontSize = 13.sp, color = Color(0xFF757575))
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(products, key = { it.productId }) { product ->
                            ProductCard(
                                product = product,
                                onClick = {
                                    navController.navigate("product/${product.productId}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(Color(0xFFFFFFFF), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        // Картинка из Storage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Color(0xFFF4F7FA), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            val imageUrl = buildImageUrl(product.image)
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.main_logo),
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.name,
            style = TextStyle(
                fontSize = 13.sp,
                fontFamily = DefaultFont,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = product.price ?: "",
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = DefaultFont,
                color = Color(0xFF031927),
                fontWeight = FontWeight.Bold
            )
        )
    }
}