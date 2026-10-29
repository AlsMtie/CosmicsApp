package com.example.cosmicsapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cosmicsapp.utils.buildImageUrl
import com.example.cosmicsapp.data.CartStore
import com.example.cosmicsapp.data.Product
import com.example.cosmicsapp.ui.components.*
import com.example.cosmicsapp.ui.theme.*

@Composable
fun CategoryScreen(
    navController: NavController,
    categoryId: Long,
    categoryName: String,
    viewModel: CategoryViewModel = viewModel()
) {
    LaunchedEffect(categoryId) {
        viewModel.loadProducts(categoryId, categoryName)
    }

    Scaffold(
        topBar = {
            CosmicsTopBar(
                showBack = true,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            CosmicsBottomNav("category") { route ->
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = categoryName,
                modifier = Modifier.padding(top = 17.dp, bottom = 13.dp),
                fontFamily = DefaultFont,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = BrandInk
            )

            when {
                viewModel.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandPrimary)
                    }
                }

                viewModel.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Не удалось загрузить товары",
                            color = BrandMuted
                        )
                        TextButton(
                            onClick = { viewModel.loadProducts(categoryId, categoryName) }
                        ) {
                            Text(
                                text = "Повторить",
                                color = BrandPrimary
                            )
                        }
                    }
                }

                viewModel.products.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "В этой категории пока нет товаров",
                            color = BrandMuted
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(viewModel.products, key = { it.productId }) { product ->
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
}

@Composable
fun ProductCard(
    product: Product,
    onOpen: () -> Unit,
    onAddToCart: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = buildImageUrl(product.image),
            contentDescription = product.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BrandSurface)
                .clickable(onClick = onOpen)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(top = 7.dp)
        ) {
            product.brandName?.let {
                Text(
                    text = it,
                    fontFamily = DefaultFont,
                    fontSize = 10.sp,
                    color = BrandMuted
                )
            }
            Text(
                text = product.name,
                fontFamily = DefaultFont,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = BrandInk,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = displayPrice(product.price),
                fontFamily = DefaultFont,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = BrandInk
            )
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