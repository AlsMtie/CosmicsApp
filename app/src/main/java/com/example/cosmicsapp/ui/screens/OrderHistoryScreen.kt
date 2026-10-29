package com.example.cosmicsapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.cosmicsapp.ui.components.CosmicsBottomNav
import com.example.cosmicsapp.ui.components.CosmicsTopBar
import com.example.cosmicsapp.R
import com.example.cosmicsapp.ui.components.ScreenTitle
import com.example.cosmicsapp.utils.buildImageUrl
import com.example.cosmicsapp.ui.screens.viewmodels.OrdersViewModel
import com.example.cosmicsapp.ui.theme.*
import com.example.cosmicsapp.utils.displayTotal
import com.example.cosmicsapp.utils.formatOrderDate
import com.example.cosmicsapp.utils.russianItems

@Composable
fun OrderHistoryScreen(
    navController: NavController,
    viewModel: OrdersViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Scaffold(
        topBar = { CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() }) },
        bottomBar = {
            CosmicsBottomNav(currentRoute = "order_history") { route ->
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            ScreenTitle(
                title = "История заказов",
                iconRes = R.drawable.shopping_bag,
                modifier = Modifier.padding(top = 15.dp, bottom = 15.dp)
            )
            when {
                viewModel.loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp)
                }
                viewModel.error != null -> Column(Modifier.fillMaxWidth().padding(top = 20.dp)) {
                    Text(viewModel.error.orEmpty(), fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted)
                    TextButton(onClick = { viewModel.loadOrders() }) { Text("Повторить", fontFamily = DefaultFont, color = BrandPrimary) }
                }
                viewModel.orders.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Заказов пока нет", fontFamily = DefaultFont, fontSize = 13.sp, color = BrandMuted)
                }
                else -> LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.orders, key = { it.id }) { order ->
                        val itemCount = viewModel.getOrderItemCount(order.id)
                        val previewImage = viewModel.getOrderPreviewImage(order.id)

                        Column(
                            modifier = Modifier.fillMaxWidth().background(BrandSurface, RoundedCornerShape(5.dp))
                                .clickable { navController.navigate("order_detail/${order.id}") }
                                .padding(horizontal = 21.dp, vertical = 13.dp)
                        ) {
                            Text(
                                text = "Заказ номер №${order.numberOrder ?: order.id}",
                                modifier = Modifier.padding(bottom = 8.dp),
                                fontFamily = DefaultFont,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = BrandInk
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                horizontalArrangement = Arrangement.spacedBy(13.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                AsyncImage(
                                    model = buildImageUrl(previewImage),
                                    contentDescription = "Товар из заказа",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.width(74.dp).height(100.dp).background(BrandSurface)
                                )
                                Column(Modifier.weight(1f).fillMaxHeight()) {
                                    Text(order.status, fontFamily = DefaultFont, fontSize = 12.sp, color = BrandInk)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Оплата при получении", fontFamily = DefaultFont, fontSize = 11.sp, color = BrandMuted)
                                    Spacer(Modifier.height(4.dp))
                                    Text("$itemCount ${russianItems(itemCount)}", fontFamily = DefaultFont, fontSize = 11.sp, color = BrandInk)
                                    Text("На сумму ${displayTotal(order.totalPrice)} ₽", fontFamily = DefaultFont, fontSize = 11.sp, color = BrandInk)
                                    Spacer(Modifier.weight(1f))
                                    Text(formatOrderDate(order.date), fontFamily = DefaultFont, fontSize = 10.sp, color = BrandMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
