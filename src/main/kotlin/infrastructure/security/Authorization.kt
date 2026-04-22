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
        // 1. Coba cara resmi Ktor
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.payload?.getClaim("role")?.asString()
        if (role != null) return role

        // 2. Cara Brute-force (Bongkar Header Manual)
        try {
            val authHeader = call.request.headers["Authorization"]
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substringAfter("Bearer ")
                val decoded = JWT.decode(token)
                return decoded.getClaim("role").asString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getUserId(call: ApplicationCall): String? {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.payload?.getClaim("userId")?.asString()
        if (userId != null) return userId

        try {
            val authHeader = call.request.headers["Authorization"]
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substringAfter("Bearer ")
                val decoded = JWT.decode(token)
                return decoded.getClaim("userId").asString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

suspend inline fun ApplicationCall.withRole(vararg roles: String, crossinline block: suspend () -> Unit) {
    val role = SecurityUtils.getRole(this)
    
    if (role?.lowercase() in roles.map { it.lowercase() }) {
        block()
    } else {
        respond(
            HttpStatusCode.Forbidden, 
            ApiResponse<Unit>(
                success = false, 
                message = "Akses ditolak: Anda memiliki role '$role', tapi butuh (${roles.joinToString()})"
            )
        )
    }
}
