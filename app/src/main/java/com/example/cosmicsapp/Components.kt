package com.example.cosmicsapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cosmicsapp.ui.theme.DefaultFont
import io.github.jan.supabase.auth.auth

@Composable
fun CosmicsTopBar(
    showBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(107.dp)
            .background(Color(0xFFC9DFF4))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = "Back",
                    tint = Color(0xFF102C3A),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            painter = painterResource(R.drawable.logo),
            contentDescription = "logo",
            modifier = Modifier.size(141.dp, 55.dp)
        )
    }
}

@Composable
fun CosmicsBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    // Проверяем, авторизован ли пользователь
    val isAuthorized = remember(currentRoute) {
        supabase.auth.currentUserOrNull() != null
    }
    val profileRoute = if (isAuthorized) "profile" else "login"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFFFFFFFF))
            .border(1.dp, Color(0xFFE0E0E0)),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val items = listOf(
            Triple("cart", R.drawable.shopping_bag, "Корзина"),
            Triple("catalog", R.drawable.main_logo, "Каталог"),
            Triple(profileRoute, R.drawable.user, "Профиль")
        )
        items.forEach { (route, iconRes, label) ->
            val isSelected = currentRoute == route
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigate(route) }
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 345.dp,
    height: Dp = 58.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.width(width).height(height),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFC7DDF2),
            contentColor = Color(0xFF000000),
            disabledContainerColor = Color(0xFFC7E1F4).copy(alpha = 0.5f),
            disabledContentColor = Color(0xFF000000).copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DefaultFont,
                color = Color(0xFF000000)
            )
        )
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier,
    width: Dp = 306.dp,
    height: Dp = 52.dp
) {
    val hasValue = value.isNotEmpty()

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(Color(0xFFFFFFFF), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            if (hasValue) {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = Color(0xFF757575),
                        fontFamily = DefaultFont
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = Color(0xFF1A1A1A),
                    fontFamily = DefaultFont
                ),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (!hasValue) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = Color(0xFF757575),
                                    fontFamily = DefaultFont
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}