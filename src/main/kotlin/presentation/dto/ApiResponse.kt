package presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: List<ValidationError>? = null
)

@Serializable
data class ValidationError(
    val field: String,
    val message: String
)
