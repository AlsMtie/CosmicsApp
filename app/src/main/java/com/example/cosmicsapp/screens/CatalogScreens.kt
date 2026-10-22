package com.example.cosmicsapp.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cosmicsapp.CosmicsBottomNav
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.R
import com.example.cosmicsapp.data.Category
import com.example.cosmicsapp.supabase
import com.example.cosmicsapp.ui.theme.BrandBackground
import com.example.cosmicsapp.ui.theme.BrandBorder
import com.example.cosmicsapp.ui.theme.BrandInk
import com.example.cosmicsapp.ui.theme.BrandMuted
import com.example.cosmicsapp.ui.theme.BrandPrimary
import com.example.cosmicsapp.ui.theme.BrandSurface
import com.example.cosmicsapp.ui.theme.DefaultFont
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout

@Composable
fun CatalogScreen(navController: NavController) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(retryKey) {
        isLoading = true
        errorMessage = null
        try {
            categories = withTimeout(12_000) { supabase.postgrest["Category"].select().decodeList<Category>() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            errorMessage = e.message ?: "Не удалось загрузить категории"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = { CosmicsTopBar() },
        bottomBar = {
            CosmicsBottomNav(currentRoute = "catalog") { route ->
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 20.dp)
        ) {
            item { Spacer(Modifier.height(35.dp)) }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.aksia),
                        contentDescription = "Акция COSMICS",
                        contentScale = ContentScale.None,
                        modifier = Modifier.width(600.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(55.dp)) }

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
                        isLoading -> Box(
                            Modifier.fillMaxWidth().height(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = BrandPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        errorMessage != null -> Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                "Не удалось загрузить категории",
                                fontFamily = DefaultFont,
                                fontSize = 12.sp,
                                color = BrandMuted
                            )
                            TextButton(
                                onClick = { retryKey++ },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Повторить", fontFamily = DefaultFont, color = BrandPrimary)
                            }
                        }

                        categories.isEmpty() -> Text(
                            text = "Категории пока не добавлены",
                            modifier = Modifier.padding(vertical = 17.dp),
                            fontFamily = DefaultFont,
                            fontSize = 12.sp,
                            color = BrandMuted
                        )

                        else -> categories.forEachIndexed { index, category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 37.dp)
                                    .clickable {
                                        navController.navigate(
                                            "category/${category.categoryId}/${Uri.encode(category.name)}"
                                        )
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    category.name,
                                    fontFamily = DefaultFont,
                                    fontSize = 13.sp,
                                    color = BrandInk,
                                    fontWeight = FontWeight.Normal
                                )
                                Icon(
                                    painter = painterResource(R.drawable.arrow_next),
                                    contentDescription = "Открыть ${category.name}",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            if (index < categories.lastIndex) {
                                HorizontalDivider(color = BrandBorder, thickness = 0.6.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
