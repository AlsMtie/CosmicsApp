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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class RegisterViewModel : ViewModel() {

    var name by mutableStateOf("")
    var lastName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)

    fun register(onSuccess: () -> Unit) {
        val cleanEmail = email.trim()
        val cleanPassword = password.trim()

        when {
            name.isBlank() || lastName.isBlank() || cleanEmail.isBlank() || cleanPassword.isBlank() -> {
                message = "Заполните все поля"
                isSuccess = false
                return
            }
            cleanPassword.length < 6 -> {
                message = "Пароль слишком короткий (мин. 6 символов)"
                isSuccess = false
                return
            }
            cleanPassword != confirmPassword.trim() -> {
                message = "Пароли не совпадают"
                isSuccess = false
                return
            }
        }

        viewModelScope.launch {
            isLoading = true
            message = null
            isSuccess = false

            try {
                supabase.auth.signUpWith(Email) {
                    this.email = cleanEmail
                    this.password = cleanPassword
                    this.data = buildJsonObject {
                        put("first_name", name.trim())
                        put("last_name", lastName.trim())
                    }
                }

                if (supabase.auth.currentUserOrNull() != null) {
                    onSuccess()
                } else {
                    message = "Аккаунт создан. Теперь войдите."
                    isSuccess = true
                }
            } catch (e: Exception) {
                val rawError = e.message.orEmpty()
                message = when {
                    rawError.contains("already registered", ignoreCase = true) ||
                            rawError.contains("User already exists", ignoreCase = true) ->
                        "Этот email уже зарегистрирован"
                    rawError.contains("Password should be", ignoreCase = true) ->
                        "Слишком простой пароль"
                    rawError.contains("network", ignoreCase = true) ->
                        "Проблемы с интернетом"
                    else ->
                        "Ошибка регистрации. Попробуйте позже"
                }
                isSuccess = false
            } finally {
                isLoading = false
            }
        }
    }
}