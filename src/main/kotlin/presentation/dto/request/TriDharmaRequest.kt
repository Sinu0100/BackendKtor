package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class TriDharmaRequest(
    val judul: String,
    val deskripsi: String? = null,
    val tanggal: String? = null,
    val delete_media_ids: List<String> = emptyList() // Pakai String karena ID media di Schema v4 adalah UUID
)
