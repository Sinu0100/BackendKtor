package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class PenelitianResponse(
    val id: String,
    val dosen_id: String,
    val judul: String,
    val tahun: Int? = null,
    val deskripsi: String? = null,
    val media: List<MediaResponse> = emptyList(),
    val anggota: List<Map<String, String>> = emptyList()
)
