package com.example.cosmicsapp.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cosmicsapp.CosmicsBottomNav
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.R
import com.example.cosmicsapp.data.Category
import com.example.cosmicsapp.supabase
import com.example.cosmicsapp.ui.theme.DefaultFont
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(navController: NavController) {
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                categories = supabase.postgrest["Category"]
                    .select()
                    .decodeList<Category>()
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = { CosmicsTopBar() },
        bottomBar = { CosmicsBottomNav(currentRoute = "catalog", onNavigate = { navController.navigate(it) }) },
        containerColor = Color(0xFFF4F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 40.dp)
                    .padding(horizontal = 60.dp)
                    .fillMaxWidth()
                    .height(151.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.aksia),
                    contentDescription = "Картинка акции",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFFFF), RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "Каталог товаров",
                        style = TextStyle(
                            fontFamily = DefaultFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF5795B5))
                            }
                        }
                        errorMessage != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Ошибка: $errorMessage",
                                    style = TextStyle(fontSize = 13.sp, fontFamily = DefaultFont, color = Color(0xFF757575))
                                )
                            }
                        }
                        categories.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Категории не найдены",
                                    style = TextStyle(fontSize = 13.sp, fontFamily = DefaultFont, color = Color(0xFF757575))
                                )
                            }
                        }
                        else -> {
                            LazyColumn {
                                items(categories, key = { it.categoryId }) { category ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                navController.navigate("category/${category.categoryId}/${category.name}")
                                            }
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            category.name,
                                            style = TextStyle(
                                                fontSize = 13.sp,
                                                color = Color(0xFF1A1A1A),
                                                fontFamily = DefaultFont
                                            )
                                        )
                                        Icon(
                                            painter = painterResource(id = R.drawable.arrow_next),
                                            contentDescription = null,
                                            tint = Color(0xFF757575),
                                            modifier = Modifier.size(10.dp)
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