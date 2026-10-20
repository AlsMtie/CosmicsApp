package com.example.cosmicsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.cosmicsapp.ui.theme.CosmicsAppTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

const val SUPABASE_PROJECT_URL = "https://rvwpoxrdugsnkgiuxjeq.supabase.co"
private const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_glmMciwgkwdcWthQCzsT_g_W6zIIMMG"

val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = SUPABASE_PROJECT_URL,
    supabaseKey = SUPABASE_PUBLISHABLE_KEY
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