package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class SertifikatResponse(
    val id: String,
    val dosen_id: String,
    val judul_sertifikat: String,
    val tahun: Int? = null,
    val file_url: String? = null,
    val created_at: String? = null
)
