package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class PenelitianRequest(
    val judul: String,
    val tahun: Int? = null,
    val deskripsi: String? = null
)
