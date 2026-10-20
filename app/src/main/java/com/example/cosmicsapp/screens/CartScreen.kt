package com.example.cosmicsapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.cosmicsapp.ScreenTitle
import com.example.cosmicsapp.buildImageUrl
import com.example.cosmicsapp.displayPrice
import com.example.cosmicsapp.R
import com.example.cosmicsapp.ui.theme.*
import java.util.Locale

@Composable
fun CartScreen(navController: NavController) {
    val lines = CartStore.lines.toList()
    val itemCount = lines.sumOf { it.quantity }
    val total = String.format(Locale("ru", "RU"), "%.0f ₽", CartStore.total())

    Scaffold(
        topBar = { CosmicsTopBar() },
        bottomBar = {
            Column {
                if (lines.isNotEmpty()) {
                    Button(
                        onClick = { navController.navigate("checkout") { launchSingleTop = true } },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp).height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, contentColor = BrandInk),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Оформить заказ", fontFamily = DefaultFont, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Column(horizontalAlignment = Alignment.End) {
                                Text(total, fontFamily = DefaultFont, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "$itemCount ${cartItemNoun(itemCount)}",
                                    fontFamily = DefaultFont,
                                    fontSize = 10.sp,
                                    color = BrandMuted
                                )
                            }
                        }
                    }
                }
                CosmicsBottomNav(currentRoute = "cart") { route ->
                    navController.navigate(route) { launchSingleTop = true }
                }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            ScreenTitle(
                title = "КОРЗИНА",
                iconRes = R.drawable.shopping_bag,
                modifier = Modifier.padding(top = 17.dp, bottom = 16.dp)
            )

            if (lines.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Корзина пуста", fontFamily = DefaultFont, fontSize = 15.sp, color = BrandInk)
                        Spacer(Modifier.height(13.dp))
                        Button(
                            onClick = {
                                navController.navigate("catalog") {
                                    popUpTo("catalog") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            shape = RoundedCornerShape(9.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, contentColor = BrandInk),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text("Перейти в каталог", fontFamily = DefaultFont, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 14.dp)
                ) {
                    items(lines, key = { it.product.productId }) { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth().height(131.dp)
                                .background(BrandSurface, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            AsyncImage(
                                model = buildImageUrl(line.product.image),
                                contentDescription = line.product.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.width(86.dp).height(115.dp).background(BrandBackground)
                            )
                            Column(modifier = Modifier.fillMaxHeight().weight(1f)) {
                                Text(
                                    line.product.name,
                                    fontFamily = DefaultFont,
                                    fontSize = 13.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandInk,
                                    maxLines = 2
                                )
                                line.product.brandName?.takeIf { it.isNotBlank() }?.let {
                                    Text(it, fontFamily = DefaultFont, fontSize = 11.sp, color = BrandMuted, maxLines = 1)
                                }
                                val variantText = listOfNotNull(
                                    line.product.color?.takeIf { it.isNotBlank() },
                                    line.product.size?.takeIf { it.isNotBlank() }
                                ).joinToString(" · ")
                                if (variantText.isNotBlank()) {
                                    Text(variantText, fontFamily = DefaultFont, fontSize = 10.sp, color = BrandMuted, maxLines = 1)
                                }

                                Spacer(Modifier.weight(1f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        displayPrice(line.product.price),
                                        fontFamily = DefaultFont,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BrandInk
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        QuantityButton("−") { CartStore.removeOne(line.product.productId) }
                                        Text(line.quantity.toString(), fontFamily = DefaultFont, fontSize = 12.sp, color = BrandInk)
                                        QuantityButton("+") { CartStore.add(line.product) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = BrandBlue,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.size(27.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontFamily = DefaultFont, fontSize = 17.sp, color = BrandInk)
        }
    }
}

private fun cartItemNoun(count: Int): String = when {
    count % 100 in 11..14 -> "товаров"
    count % 10 == 1 -> "товар"
    count % 10 in 2..4 -> "товара"
    else -> "товаров"
}
