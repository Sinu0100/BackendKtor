import io.ktor.server.application.*
import plugins.*
import infrastructure.config.SupabaseConfig
import infrastructure.di.AppComponent
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    // 1. Inisialisasi Supabase Dulu
    try {
        SupabaseConfig.init(environment)
    } catch (e: Exception) {
        log.error("CRITICAL: Gagal inisialisasi Supabase. Cek application.yaml!")
        e.printStackTrace()
    }

    // 2. Inisialisasi DI
    val appComponent = try {
        AppComponent(environment)
    } catch (e: Exception) {
        log.error("CRITICAL: Gagal inisialisasi Dependency Injection!")
        e.printStackTrace()
        null
    }

    // 3. Jalankan Plugins (Pake Try-Catch biar gak mati total)
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureSecurity() // Ini udah gue bikin anti-crash tadi
    configureValidation()
    configureStatusPages()
    
    if (appComponent != null) {
        configureRouting(appComponent)
    } else {
        log.error("ROUTING TIDAK JALAN karena AppComponent null")
    }
    
    log.info(">>>> SERVER KTOR READY DI PORT 8080 <<<<")
}
