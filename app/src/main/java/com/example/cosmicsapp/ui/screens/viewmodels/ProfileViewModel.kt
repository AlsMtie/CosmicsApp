package com.example.cosmicsapp.ui.screens.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmicsapp.data.UserProfile
import com.example.cosmicsapp.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ProfileViewModel : ViewModel() {

    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")

    var isLoading by mutableStateOf(true)
    var isSaving by mutableStateOf(false)
    var statusMessage by mutableStateOf<String?>(null)
    var isError by mutableStateOf(false)
    var profileExists by mutableStateOf(false)

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val user = supabase.auth.currentUserOrNull()
                if (user == null) return@launch

                email = user.email.orEmpty()
                val profile = withTimeout(12_000) {
                    supabase.postgrest["Users"].select {
                        filter { eq("user_id", user.id) }
                    }.decodeSingleOrNull<UserProfile>()
                }

                if (profile != null) {
                    profileExists = true
                    firstName = profile.firstName.orEmpty()
                    lastName = profile.lastName.orEmpty()
                    phone = profile.phone.orEmpty()
                    if (!profile.email.isNullOrBlank()) email = profile.email
                } else {
                    statusMessage = "Профиль не найден."
                    isError = true
                }
            } catch (e: Exception) {
                statusMessage = e.message ?: "Ошибка загрузки"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    fun saveProfile() {
        val userId = supabase.auth.currentUserOrNull()?.id
        if (userId == null) {
            statusMessage = "Войдите в аккаунт заново"
            isError = true
            return
        }

        viewModelScope.launch {
            isSaving = true
            statusMessage = null
            try {
                withTimeout(12_000) {
                    supabase.postgrest["Users"].update(
                        mapOf(
                            "first_name" to firstName.trim(),
                            "last_name" to lastName.trim(),
                            "phone" to phone.trim(),
                            "email" to email.trim()
                        )
                    ) { filter { eq("user_id", userId) } }
                }
                statusMessage = "Изменения сохранены"
                isError = false
                delay(3000) // Сообщение исчезнет через 3 сек
                statusMessage = null
            } catch (e: Exception) {
                statusMessage = e.message ?: "Не удалось сохранить"
                isError = true
            } finally {
                isSaving = false
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            supabase.auth.signOut()
            onSuccess()
        }
    }
}