package com.example.cosmicsapp.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cosmicsapp.R
import com.example.cosmicsapp.ui.components.*
import com.example.cosmicsapp.ui.theme.*

@Composable
fun CatalogScreen(
    navController: NavController,
    viewModel: CatalogViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    Scaffold(
        topBar = {
            CosmicsTopBar()
        },
        bottomBar = {
            CosmicsBottomNav("catalog") { route ->
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp)
        ) {
            item {
                Spacer(Modifier.height(35.dp))
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.aksia),
                        contentDescription = "Акция",
                        contentScale = ContentScale.None,
                        modifier = Modifier.width(600.dp)
                    )
                }
            }

            item {
                Spacer(Modifier.height(55.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandSurface, RoundedCornerShape(10.dp))
                        .padding(start = 24.dp, end = 24.dp, top = 15.dp, bottom = 10.dp)
                ) {
                    Text(
                        text = "Каталог товаров",
                        fontFamily = DefaultFont,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = BrandInk
                    )

                    Spacer(Modifier.height(9.dp))

                    when {
                        viewModel.isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = BrandPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        viewModel.errorMessage != null -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp)
                            ) {
                                Text(
                                    text = "Не удалось загрузить категории",
                                    fontFamily = DefaultFont,
                                    fontSize = 12.sp,
                                    color = BrandMuted
                                )
                                TextButton(
                                    onClick = { viewModel.loadCategories() }
                                ) {
                                    Text(
                                        text = "Повторить",
                                        color = BrandPrimary
                                    )
                                }
                            }
                        }

                        viewModel.categories.isEmpty() -> {
                            Text(
                                text = "Категории пока не добавлены",
                                modifier = Modifier.padding(vertical = 17.dp),
                                fontFamily = DefaultFont,
                                fontSize = 12.sp,
                                color = BrandMuted
                            )
                        }

                        else -> {
                            viewModel.categories.forEachIndexed { index, category ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 37.dp)
                                        .clickable {
                                            navController.navigate("category/${category.categoryId}/${Uri.encode(category.name)}")
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = category.name,
                                        fontFamily = DefaultFont,
                                        fontSize = 13.sp,
                                        color = BrandInk
                                    )
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_next),
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }

                                if (index < viewModel.categories.lastIndex) {
                                    HorizontalDivider(
                                        color = BrandBorder,
                                        thickness = 0.6.dp
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