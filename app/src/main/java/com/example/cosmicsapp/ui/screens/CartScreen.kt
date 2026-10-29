package com.example.cosmicsapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cosmicsapp.R
import com.example.cosmicsapp.utils.buildImageUrl
import com.example.cosmicsapp.ui.components.*
import com.example.cosmicsapp.ui.screens.viewmodels.CartViewModel
import com.example.cosmicsapp.ui.theme.*

@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = viewModel()
) {
    val itemCount = viewModel.getItemCount()

    Scaffold(
        topBar = {
            CosmicsTopBar()
        },
        bottomBar = {
            Column {
                if (viewModel.lines.isNotEmpty()) {
                    Button(
                        onClick = {
                            navController.navigate("checkout") { launchSingleTop = true }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp, vertical = 20.dp)
                            .height(58.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue,
                            contentColor = BrandInk
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Оформить заказ",
                                fontFamily = DefaultFont,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = viewModel.getTotal(),
                                    fontFamily = DefaultFont,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "$itemCount ${viewModel.itemNoun(itemCount)}",
                                    fontFamily = DefaultFont,
                                    fontSize = 10.sp,
                                    color = BrandMuted
                                )
                            }
                        }
                    }
                }
                CosmicsBottomNav("cart") { route ->
                    navController.navigate(route) { launchSingleTop = true }
                }
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
            ScreenTitle(
                title = "КОРЗИНА",
                iconRes = R.drawable.shopping_bag,
                modifier = Modifier.padding(top = 17.dp, bottom = 16.dp)
            )

            if (viewModel.lines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Корзина пуста",
                        fontFamily = DefaultFont,
                        fontSize = 15.sp,
                        color = BrandInk
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.lines, key = { it.product.productId }) { line ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(131.dp)
                                .background(BrandSurface, RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            AsyncImage(
                                model = buildImageUrl(line.product.image),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(86.dp)
                                    .fillMaxHeight()
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                            ) {
                                Text(
                                    text = line.product.name,
                                    fontFamily = DefaultFont,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandInk,
                                    maxLines = 2
                                )
                                Text(
                                    text = line.product.brandName.orEmpty(),
                                    fontFamily = DefaultFont,
                                    fontSize = 11.sp,
                                    color = BrandMuted
                                )

                                Spacer(Modifier.weight(1f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = displayPrice(line.product.price),
                                        fontFamily = DefaultFont,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "−",
                                            modifier = Modifier
                                                .clickable { viewModel.removeOne(line.product.productId) }
                                                .padding(4.dp)
                                        )
                                        Text(
                                            text = line.quantity.toString(),
                                            fontFamily = DefaultFont,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "+",
                                            modifier = Modifier
                                                .clickable { viewModel.add(line.product) }
                                                .padding(4.dp)
                                        )
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