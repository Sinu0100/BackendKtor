package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class JadwalPerkuliahanResponse(
    val id: Int,
    val nama_jadwal: String,
    val tanggal_upload: String? = null,
    val file_url: String
)
