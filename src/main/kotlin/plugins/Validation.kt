package plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureValidation() {
    install(RequestValidation) {
        // Nanti tambahin validasi spesifik di sini kalau perlu
        // Contoh:
        // validate<StatistikMahasiswaRequest> { request ->
        //     if (request.tahun < 2000) ValidationResult.Invalid("Tahun minimal 2000")
        //     else ValidationResult.Valid
        // }
    }
}
