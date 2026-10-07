package com.example.cosmicsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.cosmicsapp.ui.theme.CosmicsAppTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://xxqflvscopblihlrjfxu.supabase.co",
    supabaseKey = "sb_publishable_BxOhHLXEA0QrONMyGZBO0w_22-Uziar"
) {
    install(Auth)
    install(Postgrest)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CosmicsAppTheme {
                Navigation()
            }
        }
    }
}