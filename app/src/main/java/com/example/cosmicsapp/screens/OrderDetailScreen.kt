package com.example.cosmicsapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.cosmicsapp.CosmicsBottomNav
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.R
import com.example.cosmicsapp.ScreenTitle
import com.example.cosmicsapp.buildImageUrl
import com.example.cosmicsapp.data.StoreOrder
import com.example.cosmicsapp.data.StoreOrderItem
import com.example.cosmicsapp.supabase
import com.example.cosmicsapp.ui.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout

@Composable
fun OrderDetailScreen(navController: NavController, orderId: Long) {
    var order by remember(orderId) { mutableStateOf<StoreOrder?>(null) }
    var orderLines by remember(orderId) { mutableStateOf<List<StoreOrderItem>>(emptyList()) }
    var loading by remember(orderId) { mutableStateOf(true) }
    var error by remember(orderId) { mutableStateOf<String?>(null) }
    var retryKey by remember(orderId) { mutableIntStateOf(0) }

    LaunchedEffect(orderId, retryKey) {
        loading = true
        error = null
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            error = "Войдите в аккаунт, чтобы открыть заказ."
            loading = false
            return@LaunchedEffect
        }
        try {
            withTimeout(15_000) {
                order = supabase.postgrest["Orders"].select {
                    filter {
                        eq("orders_id", orderId)
                        eq("user_id", userId)
                    }
                }.decodeSingleOrNull<StoreOrder>()
                if (order == null) throw IllegalStateException("Заказ не найден")
                orderLines = supabase.postgrest["Order_items"].select {
                    filter { eq("order_id", orderId) }
                }.decodeList<StoreOrderItem>()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Не удалось загрузить заказ"
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = { CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() }) },
        bottomBar = {
            CosmicsBottomNav(currentRoute = "order_detail") { route ->
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp)
            }
            error != null || order == null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(22.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(error ?: "Заказ не найден", fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted)
                TextButton(onClick = { retryKey++ }) { Text("Повторить", fontFamily = DefaultFont, color = BrandPrimary) }
            }
            else -> {
                val currentOrder = order!!
                val totalCount = orderLines.sumOf { it.quantity.toIntOrNull() ?: 0 }
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
                            fontFamily = DefaultFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandInk
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    items(orderLines, key = { it.id ?: it.productId }) { item ->
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
                                    Text("Количество", fontFamily = DefaultFont, fontSize = 10.sp, color = BrandMuted)
                                }
                                Text("${lineTotal(item.price, item.quantity)} ₽", fontFamily = DefaultFont, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = BrandInk)
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
                                modifier = Modifier.widthIn(max = 310.dp).fillMaxWidth().height(48.dp).background(BrandSurface, RoundedCornerShape(10.dp))
                                    .padding(0.dp),
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
                                Text("Итого", fontFamily = DefaultFont, fontSize = 13.sp, color = BrandInk)
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
    val unitPrice = price.orEmpty().replace(",", ".").filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
    val count = quantity.toIntOrNull() ?: 0
    return String.format(java.util.Locale("ru", "RU"), "%.0f", unitPrice * count)
}
