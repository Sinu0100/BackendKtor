package plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import presentation.dto.ApiResponse

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Handle 404 Not Found
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ApiResponse<Unit>(
                    success = false,
                    message = "Resource tidak ditemukan"
                )
            )
        }

        // Handle 401 Unauthorized
        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(
                status,
                ApiResponse<Unit>(
                    success = false,
                    message = "Sesi berakhir atau token tidak valid"
                )
            )
        }

        // Handle General Exception
        exception<Throwable> { call, cause ->
            val status = when {
                cause.message?.contains("FORBIDDEN", ignoreCase = true) == true -> HttpStatusCode.Forbidden
                cause.message?.contains("Unauthorized", ignoreCase = true) == true -> HttpStatusCode.Unauthorized
                cause.message?.contains("tidak ditemukan", ignoreCase = true) == true -> HttpStatusCode.NotFound
                else -> HttpStatusCode.InternalServerError
            }
            
            call.respond(
                status,
                ApiResponse<Unit>(
                    success = false,
                    message = cause.message ?: "Terjadi kesalahan internal"
                )
            )
        }
        
        // Custom Exception untuk Business Logic (bisa ditambah nanti)
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(
                    success = false,
                    message = cause.message ?: "Input tidak valid"
                )
            )
        }
    }
}
