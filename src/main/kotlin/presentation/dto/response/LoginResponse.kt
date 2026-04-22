package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val email: String? = null,
    val role: String? = null,
    val expiresAt: Long? = null
)
