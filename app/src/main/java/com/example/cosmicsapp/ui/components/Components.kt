package com.example.cosmicsapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cosmicsapp.R
import com.example.cosmicsapp.data.supabase
import com.example.cosmicsapp.ui.theme.*
import io.github.jan.supabase.auth.auth

@Composable
fun CosmicsTopBar(
    showBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandBlue)
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(96.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = "Назад",
                    tint = BrandInk,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(8.dp))

            Image(
                painter = painterResource(R.drawable.icon_2),
                contentDescription = "COSMICS",
                modifier = Modifier.size(width = 141.dp, height = 55.dp)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "COSMICS",
                modifier = Modifier.size(width = 141.dp, height = 55.dp)
            )
        }
    }
}

@Composable
fun CosmicsBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val profileRoute = if (supabase.auth.currentUserOrNull() != null) "profile" else "login"
    val selectedCatalog = currentRoute in setOf("catalog", "category", "product")
    val selectedProfile = currentRoute in setOf("profile", "login", "register", "order_history", "order_detail")

    Surface(
        color = BrandSurface,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(73.dp),
            horizontalArrangement = Arrangement.spacedBy(100.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavIcon(
                iconRes = R.drawable.shopping_bag,
                label = "Корзина",
                selected = currentRoute == "cart",
                onClick = { onNavigate("cart") }
            )
            BottomNavIcon(
                iconRes = R.drawable.main_logo,
                label = "Каталог",
                selected = selectedCatalog,
                onClick = { onNavigate("catalog") }
            )
            BottomNavIcon(
                iconRes = R.drawable.user,
                label = "Профиль",
                selected = selectedProfile,
                onClick = { onNavigate(profileRoute) }
            )
        }
    }
}

@Composable
private fun BottomNavIcon(
    iconRes: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(30.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = if (selected) BrandPrimary else BrandInk,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 345.dp,
    height: Dp = 42.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.widthIn(max = width).fillMaxWidth().height(height),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandBlue,
            contentColor = BrandInk,
            disabledContainerColor = BrandBlue.copy(alpha = 0.45f),
            disabledContentColor = BrandMuted
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 1.dp)
    ) {
        Text(text, fontFamily = DefaultFont, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
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
    height: Dp = 43.dp,
    readOnly: Boolean = false
) {
    val hasValue = value.isNotEmpty()
    val keyboardType = when {
        isPassword -> KeyboardType.Password
        placeholder.contains("email", ignoreCase = true) -> KeyboardType.Email
        placeholder.contains("телефон", ignoreCase = true) -> KeyboardType.Phone
        else -> KeyboardType.Text
    }

    Box(
        modifier = modifier
            .widthIn(max = width)
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(7.dp))
            .background(BrandSurface)
            .border(1.dp, BrandBorder, RoundedCornerShape(7.dp))
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (hasValue) {
            Text(
                text = placeholder,
                modifier = Modifier.align(Alignment.TopStart).padding(top = 4.dp),
                fontFamily = DefaultFont,
                fontSize = 9.sp,
                color = BrandMuted
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            readOnly = readOnly,
            textStyle = TextStyle(fontFamily = DefaultFont, fontSize = 13.sp, color = BrandInk),
            cursorBrush = SolidColor(BrandPrimary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth().padding(top = if (hasValue) 10.dp else 0.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (!hasValue) {
                        Text(placeholder, fontFamily = DefaultFont, fontSize = 13.sp, color = BrandMuted)
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun ScreenTitle(
    title: String,
    iconRes: Int? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(start = 25.dp, top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = BrandInk,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            title,
            modifier = Modifier.padding(start = 0.dp),
            fontFamily = DefaultFont,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = BrandInk
        )
    }
}

@Composable
fun InlineStatus(message: String, isError: Boolean = false, modifier: Modifier = Modifier) {
    Text(
        text = message,
        modifier = modifier,
        fontFamily = DefaultFont,
        fontSize = 12.sp,
        color = if (isError) BrandError else BrandSuccess
    )
}

fun displayPrice(rawPrice: String?): String {
    val value = rawPrice?.trim().orEmpty()
    if (value.isBlank()) return "Цена не указана"
    return if (value.contains("₽") || value.contains("руб", ignoreCase = true)) value else "$value ₽"
}