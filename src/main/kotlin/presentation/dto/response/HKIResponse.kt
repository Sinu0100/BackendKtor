package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class HKIResponse(
    val id: String,
    val dosen_id: String,
    val judul_invensi: String,
    val inventor: String?,
    val jenis_hki: String?,
    val nomor_paten: String?,
    val tahun: Int?,
    val file_url: String?,
    val created_at: String? = null
)
