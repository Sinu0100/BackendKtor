package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class JadwalPerkuliahanRequest(
    val nama_jadwal: String,
    val file_url: String,
    val tanggal_upload: String? = null
)
