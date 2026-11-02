package com.example.cosmicsapp.ui.screens.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmicsapp.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun login(onSuccess: () -> Unit) {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            errorMessage = "Заполните все поля"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                supabase.auth.signInWith(Email) {
                    this.email = cleanEmail
                    this.password = cleanPassword
                }
                onSuccess()
            } catch (e: Exception) {
                val rawError = e.message.orEmpty()
                errorMessage = when {
                    rawError.contains("Invalid login credentials", ignoreCase = true) ->
                        "Неверный email или пароль"
                    rawError.contains("Email not confirmed", ignoreCase = true) ->
                        "Подтвердите email"
                    rawError.contains("network", ignoreCase = true) ->
                        "Проблемы с интернетом"
                    else ->
                        "Не удалось войти. Попробуйте позже"
                }
            } finally {
                isLoading = false
            }
        }
    }
}