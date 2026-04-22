package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class HKIResponse(
    val id: String,
    val dosen_id: String,
    val judul: String,
    val tahun: Int? = null,
    val deskripsi: String? = null,
    val file_url: String? = null
)
