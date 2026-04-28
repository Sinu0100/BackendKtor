package plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import presentation.dto.ApiResponse
import java.util.Base64

fun Application.configureSecurity() {
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()
    val jwtSecret = environment.config.property("jwt.secret").getString()
    
    val finalSecret = try {
        if (jwtSecret.contains("-") || jwtSecret.contains("_") || jwtSecret.length < 20) {
            // Jika secret pendek atau ada karakter non-base64, pake raw bytes
            jwtSecret.toByteArray()
        } else {
            Base64.getDecoder().decode(jwtSecret)
        }
    } catch (e: Exception) {
        println("WARNING: JWT Secret bukan Base64, menggunakan raw bytes. Error: ${e.message}")
        jwtSecret.toByteArray()
    }

    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(finalSecret))
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                val role = credential.payload.getClaim("role").asString()
                if (role != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { defaultScheme, realm ->
                val authHeader = call.request.headers["Authorization"]
                val response = when {
                    authHeader == null -> ApiResponse<Unit>(false, "Header Authorization (Bearer Token) tidak ditemukan")
                    !authHeader.startsWith("Bearer ", ignoreCase = true) -> ApiResponse<Unit>(false, "Format token salah. Gunakan 'Bearer <token>'")
                    else -> ApiResponse<Unit>(false, "Token tidak valid, expired, atau Secret server tidak cocok.")
                }
                call.respond(HttpStatusCode.Unauthorized, response)
            }
        }
    }
}
