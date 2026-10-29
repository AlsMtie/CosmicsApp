package com.example.cosmicsapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cosmicsapp.data.CartStore
import com.example.cosmicsapp.ui.components.CustomTextField
import com.example.cosmicsapp.ui.components.CosmicsTopBar
import com.example.cosmicsapp.ui.theme.*

@Composable
fun CheckoutScreen(
    navController: NavController,
    viewModel: CheckoutViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    val recipientName = listOf(viewModel.firstName.trim(), viewModel.lastName.trim())
        .filter { it.isNotBlank() }.joinToString(" ")

    Scaffold(
        topBar = {
            CosmicsTopBar(
                showBack = true,
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            Surface(
                color = BrandSurface,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.submitOrder { orderId ->
                                navController.navigate("order_detail/$orderId") {
                                    popUpTo("catalog") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        enabled = !viewModel.isSubmitting && !viewModel.isLoading && viewModel.pointsLoaded && CartStore.lines.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue,
                            contentColor = BrandInk
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (viewModel.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = BrandInk
                            )
                        } else {
                            Text(
                                "Оформить заказ",
                                fontFamily = DefaultFont,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(5.dp))

                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = BrandMuted)) {
                                append("Оформляя, я соглашаюсь с ")
                            }
                            withStyle(SpanStyle(color = BrandInk)) {
                                append("правилами Интернет-магазина COSMICS")
                            }
                        },
                        fontFamily = DefaultFont,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 22.dp)
            ) {
                Text(
                    "Пункт выдачи товаров",
                    fontFamily = DefaultFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = BrandInk
                )

                Spacer(Modifier.height(10.dp))

                when {
                    !viewModel.errorMessage.isNullOrBlank() && !viewModel.pointsLoaded -> {
                        Text(
                            viewModel.errorMessage.orEmpty(),
                            fontFamily = DefaultFont,
                            fontSize = 12.sp,
                            color = BrandError
                        )
                    }
                    viewModel.points.isEmpty() -> {
                        Text(
                            "Пункты выдачи пока не добавлены.",
                            fontFamily = DefaultFont,
                            fontSize = 12.sp,
                            color = BrandMuted
                        )
                    }
                    else -> {
                        viewModel.points.forEachIndexed { index, point ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectedPointId = point.id }
                                    .padding(vertical = 9.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    RadioButton(
                                        selected = viewModel.selectedPointId == point.id,
                                        onClick = { viewModel.selectedPointId = point.id },
                                        modifier = Modifier.size(28.dp),
                                        colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                                    )
                                    Spacer(Modifier.width(7.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "ПВЗ ${point.addressName}",
                                            fontFamily = DefaultFont,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = BrandInk
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            point.address,
                                            fontFamily = DefaultFont,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            color = BrandInk
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Режим работы: ${point.workTime.orEmpty()}",
                                            fontFamily = DefaultFont,
                                            fontSize = 11.sp,
                                            color = BrandMuted
                                        )
                                    }
                                }
                            }
                            if (index < viewModel.points.lastIndex) {
                                HorizontalDivider(color = BrandBorder, thickness = 1.dp)
                            }
                        }
                    }
                }

                SectionDivider()

                Text("Дата выдачи", fontFamily = DefaultFont, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                Spacer(Modifier.height(5.dp))
                Text("Соберем в магазине за 24 часа", fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted)

                SectionDivider()

                Text("Получатель", fontFamily = DefaultFont, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                Spacer(Modifier.height(10.dp))

                CustomTextField(viewModel.firstName, { viewModel.firstName = it }, "Имя", width = 360.dp)
                Spacer(Modifier.height(8.dp))
                CustomTextField(viewModel.phone, { viewModel.phone = it }, "Номер телефона", width = 360.dp)
                Spacer(Modifier.height(8.dp))
                CustomTextField(viewModel.email, { viewModel.email = it }, "Email", width = 360.dp)

                SectionDivider()

                Text("Оплата", fontFamily = DefaultFont, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = BrandInk)
                Spacer(Modifier.height(5.dp))
                Text("При получении", fontFamily = DefaultFont, fontSize = 12.sp, color = BrandMuted)

                if (!viewModel.errorMessage.isNullOrBlank() && viewModel.pointsLoaded) {
                    Spacer(Modifier.height(10.dp))
                    Text(viewModel.errorMessage.orEmpty(), fontFamily = DefaultFont, fontSize = 12.sp, color = BrandError)
                }
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(top = 16.dp, bottom = 13.dp),
        thickness = 1.dp,
        color = BrandBorder
    )
}