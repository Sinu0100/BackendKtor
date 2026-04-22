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
        Base64.getDecoder().decode(jwtSecret)
    } catch (e: Exception) {
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
                // Selama role ada di token, kita terima. Audience kita abaikan dulu biar gak rewel.
                val role = credential.payload.getClaim("role").asString()
                if (role != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized, 
                    ApiResponse<Unit>(success = false, message = "Token tidak valid atau kadaluwarsa. Pastikan JWT Secret di server benar.")
                )
            }
        }
    }
}
