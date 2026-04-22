package infrastructure.config

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.server.application.*

object SupabaseConfig {
    private var client: SupabaseClient? = null

    fun init(environment: ApplicationEnvironment): SupabaseClient {
        val url = environment.config.property("supabase.url").getString()
        val key = environment.config.property("supabase.key").getString()

        client = createSupabaseClient(url, key) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
        return client!!
    }

    fun getClient(): SupabaseClient {
        return client ?: throw IllegalStateException("SupabaseClient not initialized. Call init() first.")
    }
}
