package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class SertifikatResponse(
    val id: String,
    val dosen_id: String,
    val nama_sertifikat: String,
    val penerbit: String,
    val tahun: Int? = null,
    val file_url: String? = null
)
