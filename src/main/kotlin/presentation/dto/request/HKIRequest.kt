package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class HKIRequest(
    val id: String? = null,
    val judul: String,
    val tahun: Int,
    val deskripsi: String? = null
)
