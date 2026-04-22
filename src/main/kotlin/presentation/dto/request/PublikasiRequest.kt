package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class PublikasiRequest(
    val judul: String,
    val nama_jurnal_konferensi: String? = null,
    val deskripsi: String? = null,
    val link_tautan: String? = null,
    val tahun: Int? = null
)
