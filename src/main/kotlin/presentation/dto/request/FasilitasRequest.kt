package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class FasilitasRequest(
    val nama_fasilitas: String,
    val deskripsi: String,
    val delete_media_ids: List<Int> = emptyList()
)
