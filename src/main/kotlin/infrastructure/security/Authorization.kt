package infrastructure.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import presentation.dto.ApiResponse
import com.auth0.jwt.JWT

object SecurityUtils {
    fun getRole(call: ApplicationCall): String? {
        return try {
            // 1. Ambil principal resmi
            val principal = call.principal<JWTPrincipal>()
            val role = principal?.payload?.getClaim("role")?.asString()
            if (role != null) return role

            // 2. Fallback manual (Ambil header pertama saja untuk menghindari ParseException)
            val authHeaders = call.request.headers.getAll("Authorization")
            val authHeader = authHeaders?.firstOrNull() 
            
            if (authHeader != null && authHeader.startsWith("Bearer ", ignoreCase = true)) {
                val token = authHeader.substringAfter("Bearer ")
                val decoded = JWT.decode(token)
                decoded.getClaim("role").asString()
            } else null
        } catch (e: Exception) {
            println("DEBUG Security: Gagal ambil Role - ${e.message}")
            null
        }
    }

    fun getUserId(call: ApplicationCall): String? {
        return try {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.getClaim("userId")?.asString()
            if (userId != null) return userId

            val authHeaders = call.request.headers.getAll("Authorization")
            val authHeader = authHeaders?.firstOrNull()

            if (authHeader != null && authHeader.startsWith("Bearer ", ignoreCase = true)) {
                val token = authHeader.substringAfter("Bearer ")
                val decoded = JWT.decode(token)
                decoded.getClaim("userId").asString()
            } else null
        } catch (e: Exception) {
            println("DEBUG Security: Gagal ambil UserId - ${e.message}")
            null
        }
    }
}

suspend inline fun ApplicationCall.withRole(vararg roles: String, crossinline block: suspend () -> Unit) {
    val role = SecurityUtils.getRole(this)
    val userId = SecurityUtils.getUserId(this)

    if (role != null && role.lowercase() in roles.map { it.lowercase() }) {
        block()
    } else {
        println("AUTH FAILED: Role '$role' tidak diijinkan akses (Butuh: ${roles.joinToString()})")
        respond(
            HttpStatusCode.Forbidden, 
            ApiResponse<Unit>(
                success = false, 
                message = if (role == null) "Token tidak valid atau tidak terbaca. Pastikan header Authorization benar." 
                          else "Akses ditolak: Role '$role' tidak memiliki ijin."
            )
        )
    }
}
