package infrastructure.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import java.util.*

data class TokenInfo(
    val token: String,
    val expiresAt: Long
)

class JwtService(environment: ApplicationEnvironment) {
    private val issuer = environment.config.property("jwt.issuer").getString()
    private val audience = environment.config.property("jwt.audience").getString()
    private val jwtSecret = environment.config.property("jwt.secret").getString()
    private val expiresInMs = 3600000L * 24 // 24 hours

    // SAMA DENGAN Security.kt: Decode Base64 dulu
    private val algorithm: Algorithm by lazy {
        val finalSecret = try {
            Base64.getDecoder().decode(jwtSecret)
        } catch (e: Exception) {
            jwtSecret.toByteArray()
        }
        Algorithm.HMAC256(finalSecret)
    }

    fun generateToken(userId: String, email: String, role: String): TokenInfo {
        val expirationTime = System.currentTimeMillis() + expiresInMs
        val expiresAtDate = Date(expirationTime)
        
        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(expiresAtDate)
            .sign(algorithm)
            
        return TokenInfo(token, expirationTime)
    }
}
