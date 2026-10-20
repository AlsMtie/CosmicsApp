package com.example.cosmicsapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

private data class OrderHistoryRow(
    val order: StoreOrder,
    val itemCount: Int,
    val previewImage: String?
)

@Composable
fun OrderHistoryScreen(navController: NavController) {
    var rows by remember { mutableStateOf<List<OrderHistoryRow>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(retryKey) {
        loading = true
        error = null
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            error = "Войдите в аккаунт, чтобы увидеть заказы."
            loading = false
            return@LaunchedEffect
        }
        try {
            withTimeout(15_000) {
                val orders = supabase.postgrest["Orders"].select {
                    filter { eq("user_id", userId) }
                }.decodeList<StoreOrder>()
                val orderItems = if (orders.isEmpty()) emptyList() else {
                    // RLS restricts this result to items belonging to the current user's orders.
                    supabase.postgrest["Order_items"].select().decodeList<StoreOrderItem>()
                }
                val byOrder = orderItems.groupBy { it.orderId }
                rows = orders.sortedByDescending { it.date }.map { order ->
                    val items = byOrder[order.id].orEmpty()
                    OrderHistoryRow(
                        order = order,
                        itemCount = items.sumOf { it.quantity.toIntOrNull() ?: 0 },
                        previewImage = items.firstOrNull()?.productImage
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "Не удалось загрузить историю заказов"
        } finally {
            loading = false
        }
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
                loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp)
                }
                error != null -> Column(Modifier.fillMaxWidth().padding(top = 20.dp)) {
                    Text(error.orEmpty(), fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted)
                    TextButton(onClick = { retryKey++ }) { Text("Повторить", fontFamily = DefaultFont, color = BrandPrimary) }
                }
                rows.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Заказов пока нет", fontFamily = DefaultFont, fontSize = 13.sp, color = BrandMuted)
                }
                else -> LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rows, key = { it.order.id }) { row ->
                        Column(
                            modifier = Modifier.fillMaxWidth().background(BrandSurface, RoundedCornerShape(5.dp))
                                .clickable { navController.navigate("order_detail/${row.order.id}") }
                                .padding(horizontal = 21.dp, vertical = 13.dp)
                        ) {
                            Text(
                                text = "Заказ номер №${row.order.numberOrder ?: row.order.id}",
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
                                    model = buildImageUrl(row.previewImage),
                                    contentDescription = "Товар из заказа",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.width(74.dp).height(100.dp).background(BrandBackground)
                                )
                                Column(Modifier.weight(1f).fillMaxHeight()) {
                                    Text(row.order.status, fontFamily = DefaultFont, fontSize = 12.sp, color = BrandInk)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Оплата при получении", fontFamily = DefaultFont, fontSize = 11.sp, color = BrandMuted)
                                    Spacer(Modifier.height(4.dp))
                                    Text("${row.itemCount} ${russianItems(row.itemCount)}", fontFamily = DefaultFont, fontSize = 11.sp, color = BrandInk)
                                    Text("На сумму ${displayTotal(row.order.totalPrice)} ₽", fontFamily = DefaultFont, fontSize = 11.sp, color = BrandInk)
                                    Spacer(Modifier.weight(1f))
                                    Text(formatOrderDate(row.order.date), fontFamily = DefaultFont, fontSize = 10.sp, color = BrandMuted)
                                }
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(top = 12.dp),
                                color = BrandBorder,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun displayTotal(raw: String?): String {
    val value = raw?.trim().orEmpty()
    val normalized = value.replace(",", ".").filter { it.isDigit() || it == '.' }
    val amount = normalized.toBigDecimalOrNull() ?: return value.ifBlank { "0" }
    return amount.stripTrailingZeros().toPlainString()
}

internal fun formatOrderDate(value: String): String {
    val date = value.take(10)
    val time = value.substringAfter('T', "").take(5)
    return if (time.isBlank()) date else "$date в $time"
}

internal fun russianItems(count: Int): String = when {
    count % 100 in 11..14 -> "товаров"
    count % 10 == 1 -> "товар"
    count % 10 in 2..4 -> "товара"
    else -> "товаров"
}
