package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class PublikasiResponse(
    val id: String,
    val dosen_id: String,
    val judul: String,
    val nama_jurnal_konferensi: String? = null,
    val deskripsi: String? = null,
    val link_tautan: String? = null,
    val tahun: Int? = null,
    val created_at: String? = null,
    val media: List<MediaResponse> = emptyList()
)
