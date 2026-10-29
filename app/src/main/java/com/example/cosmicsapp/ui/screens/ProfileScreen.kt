package com.example.cosmicsapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cosmicsapp.R
import com.example.cosmicsapp.ui.components.*
import com.example.cosmicsapp.ui.screens.viewmodels.ProfileViewModel
import com.example.cosmicsapp.ui.theme.*

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    Scaffold(
        topBar = { CosmicsTopBar() },
        bottomBar = {
            CosmicsBottomNav(currentRoute = "profile") { route ->
                navController.navigate(route) { launchSingleTop = true }
            }
        },
        containerColor = BrandBackground
    ) { padding ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPrimary, strokeWidth = 2.dp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 22.dp, vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ScreenTitle(
                        title = "ПРОФИЛЬ",
                        iconRes = R.drawable.user,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 22.dp)
                    )

                    CustomTextField(
                        value = viewModel.firstName,
                        onValueChange = { viewModel.firstName = it; viewModel.statusMessage = null },
                        placeholder = "Имя"
                    )
                    Spacer(Modifier.height(8.dp))

                    CustomTextField(
                        value = viewModel.lastName,
                        onValueChange = { viewModel.lastName = it; viewModel.statusMessage = null },
                        placeholder = "Фамилия"
                    )
                    Spacer(Modifier.height(8.dp))

                    CustomTextField(
                        value = viewModel.phone,
                        onValueChange = { viewModel.phone = it; viewModel.statusMessage = null },
                        placeholder = "Телефон"
                    )
                    Spacer(Modifier.height(8.dp))

                    CustomTextField(
                        value = viewModel.email,
                        onValueChange = {},
                        placeholder = "Email",
                        readOnly = true
                    )

                    viewModel.statusMessage?.let {
                        Spacer(Modifier.height(10.dp))
                        InlineStatus(it, isError = viewModel.isError, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(Modifier.height(14.dp))

                    CustomButton(
                        text = if (viewModel.isSaving) "Сохраняем..." else "Сохранить",
                        onClick = { viewModel.saveProfile() },
                        enabled = !viewModel.isSaving,
                        width = 190.dp,
                        height = 36.dp
                    )
                }

                Button(
                    onClick = { navController.navigate("order_history") { launchSingleTop = true } },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(0.82f)
                        .height(43.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, contentColor = BrandInk),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "История заказов",
                        fontFamily = DefaultFont,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                TextButton(
                    onClick = {
                        viewModel.logout {
                            navController.navigate("login") {
                                popUpTo("catalog") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "Выйти из аккаунта",
                        fontFamily = DefaultFont,
                        fontSize = 11.sp,
                        color = BrandMuted
                    )
                }
            }
        }
    }
}