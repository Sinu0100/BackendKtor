package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class PengabdianRequest(
    val judul_pengabdian: String,
    val deskripsi: String? = null,
    val tahun: Int? = null
)
