package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class PengabdianResponse(
    val id: String,
    val dosen_id: String,
    val judul_pengabdian: String,
    val deskripsi: String? = null,
    val tahun: Int? = null,
    val media: List<MediaResponse> = emptyList()
)
