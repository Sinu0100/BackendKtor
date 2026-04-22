package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BukuAjarResponse(
    val id: String,
    val dosen_id: String,
    val judul: String,
    val tahun: Int? = null,
    val deskripsi: String? = null,
    val peran_penulis: String? = null,
    val created_at: String? = null,
    val media: List<MediaResponse> = emptyList()
)
