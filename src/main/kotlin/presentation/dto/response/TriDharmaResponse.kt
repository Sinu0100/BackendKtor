package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TriDharmaResponse(
    val id: String,
    val judul: String,
    val deskripsi: String,
    val tanggal: String?,
    val created_at: String?,
    val media: List<MediaResponse> = emptyList()
)
