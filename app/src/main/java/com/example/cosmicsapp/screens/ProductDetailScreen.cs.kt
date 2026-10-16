package com.example.cosmicsapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import com.example.cosmicsapp.CosmicsTopBar
import com.example.cosmicsapp.CustomButton
import com.example.cosmicsapp.R
import com.example.cosmicsapp.data.Product
import com.example.cosmicsapp.supabase
import com.example.cosmicsapp.ui.theme.DefaultFont
import com.example.cosmicsapp.buildImageUrl
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(navController: NavController, productId: Long) {
    var product by remember { mutableStateOf<Product?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(productId) {
        scope.launch {
            try {
                product = supabase.postgrest["Product"]
                    .select { filter { eq("product_id", productId) } }
                    .decodeSingleOrNull<Product>()
            } catch (_: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = { CosmicsTopBar(showBack = true, onBack = { navController.popBackStack() }) },
        bottomBar = {
            product?.let { p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFFFF))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = p.price ?: "",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = DefaultFont,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF031927)
                        )
                    )
                    CustomButton(
                        text = "В корзину",
                        onClick = { /* TODO */ },
                        width = 200.dp
                    )
                }
            }
        },
        containerColor = Color(0xFFF4F7FA)
    ) { padding ->
        when {
            isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF5795B5))
            }
            product == null -> Box(
                Modifier.fillMaxSize().padding(padding),
                Alignment.Center
            ) {
                Text("Товар не найден")
            }
            else -> {
                val p = product!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(Color(0xFFFFFFFF), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val imageUrl = buildImageUrl(p.image)
                        if (!imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = p.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.main_logo),
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(p.name, style = TextStyle(fontSize = 20.sp, fontFamily = DefaultFont, fontWeight = FontWeight.Bold, color = Color(0xFF031927)))
                    Spacer(Modifier.height(8.dp))
                    Text(p.price ?: "", style = TextStyle(fontSize = 18.sp, fontFamily = DefaultFont, color = Color(0xFF031927)))

                    Spacer(Modifier.height(24.dp))
                    Text("Описание", style = TextStyle(fontSize = 16.sp, fontFamily = DefaultFont, fontWeight = FontWeight.Medium))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        p.description ?: "Описание отсутствует",
                        style = TextStyle(fontSize = 13.sp, fontFamily = DefaultFont, color = Color(0xFF757575))
                    )
                }
            }
        }
    }
}