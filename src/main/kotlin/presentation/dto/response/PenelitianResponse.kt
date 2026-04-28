package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class PenelitianResponse(
    val id: String,
    val dosen_id: String,
    val judul: String,
    val tahun: Int?,
    val deskripsi: String,
    val created_at: String? = null,
    val media: List<MediaResponse>,
    val anggota: List<AnggotaResponse>
)

@Serializable
data class AnggotaResponse(
    val dosen_id: String,
    val nama_dosen: String,
    val peran: String
)
