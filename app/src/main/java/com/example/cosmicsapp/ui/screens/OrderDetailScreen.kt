package com.example.cosmicsapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cosmicsapp.R
import com.example.cosmicsapp.ui.components.*
import com.example.cosmicsapp.ui.screens.viewmodels.OrderDetailViewModel
import com.example.cosmicsapp.ui.theme.*
import com.example.cosmicsapp.utils.buildImageUrl
import com.example.cosmicsapp.utils.displayTotal
import com.example.cosmicsapp.utils.russianItems

@Composable
fun OrderDetailScreen(
    navController: NavController,
    orderId: Long,
    viewModel: OrderDetailViewModel = viewModel()
) {
    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    Scaffold(
        topBar = {
            CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() })
        },
        bottomBar = {
            CosmicsBottomNav("order_detail") { route ->
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        when {
            viewModel.loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp)
                }
            }
            viewModel.error != null -> {
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(22.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(viewModel.error ?: "Заказ не найден", fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted)
                    TextButton(onClick = { viewModel.loadOrder(orderId) }) {
                        Text("Повторить", fontFamily = DefaultFont, color = BrandPrimary)
                    }
                }
            }
            viewModel.order != null -> {
                val currentOrder = viewModel.order!!
                val totalCount = viewModel.orderLines.sumOf { it.quantity.toIntOrNull() ?: 0 }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 15.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item {
                        ScreenTitle(
                            title = "Заказ №${currentOrder.numberOrder ?: currentOrder.id}",
                            iconRes = R.drawable.shopping_bag,
                            modifier = Modifier.padding(bottom = 13.dp)
                        )
                        Text(
                            currentOrder.status,
                            modifier = Modifier.padding(start = 22.dp),
                            fontFamily = DefaultFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandInk
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    items(viewModel.orderLines, key = { it.id ?: it.productId }) { item ->
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp).heightIn(min = 100.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                AsyncImage(
                                    model = buildImageUrl(item.productImage),
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.width(78.dp).height(100.dp).background(BrandSurface)
                                )
                                Column(Modifier.weight(1f).heightIn(min = 100.dp)) {
                                    Text(item.brand, fontFamily = DefaultFont, fontSize = 11.sp, color = BrandMuted, maxLines = 1)
                                    Text(item.name, fontFamily = DefaultFont, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                                    Text(item.category, fontFamily = DefaultFont, fontSize = 11.sp, color = BrandMuted)
                                    val variant = listOfNotNull(
                                        item.size?.takeIf { it.isNotBlank() }?.let { "$it мл".replace("мл мл", "мл") },
                                        item.color?.takeIf { it.isNotBlank() }
                                    ).joinToString(" · ")
                                    if (variant.isNotBlank()) Text(variant, fontFamily = DefaultFont, fontSize = 11.sp, color = BrandInk)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier.size(28.dp).background(BrandBlue, RoundedCornerShape(2.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.quantity, fontFamily = DefaultFont, fontSize = 12.sp, color = BrandInk)
                                    }
                                }
                                Text("${lineTotal(item.price, item.quantity)} ₽", fontFamily = DefaultFont, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
                                color = BrandBorder
                            )
                        }
                    }
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().background(BrandSurface, RoundedCornerShape(10.dp)).padding(horizontal = 9.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Ваш заказ", fontFamily = DefaultFont, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                                Text("$totalCount ${russianItems(totalCount)}", fontFamily = DefaultFont, fontSize = 11.sp, color = BrandMuted)
                            }
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(48.dp)
                                    .background(BrandSurface, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = BrandSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("Оплата по получении", fontFamily = DefaultFont, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                                    }
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Итого", fontFamily = DefaultFont, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                                Text("${displayTotal(currentOrder.totalPrice)} ₽", fontFamily = DefaultFont, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun lineTotal(price: String?, quantity: String): String {
    val unitPrice = price.orEmpty()
        .replace(",", ".")
        .filter { it.isDigit() || it == '.' }
        .toDoubleOrNull() ?: 0.0
    val count = quantity.toIntOrNull() ?: 0
    return String.format(java.util.Locale("ru", "RU"), "%.0f", unitPrice * count)
}