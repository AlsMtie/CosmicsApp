package com.example.cosmicsapp

import android.os.Bundle
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
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

        Coil.setImageLoader(
            ImageLoader.Builder(applicationContext)
                .crossfade(true)
                .memoryCache {
                    MemoryCache.Builder(applicationContext)
                        .maxSizePercent(0.20)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("product_images"))
                        .maxSizeBytes(50L * 1024 * 1024)
                        .build()
                }
                .build()
        )

        setContent {
            CosmicsAppTheme {
                Navigation()
            }
        }
    }
}