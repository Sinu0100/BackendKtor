package presentation.dto.response

import domain.model.Media
import kotlinx.serialization.Serializable

@Serializable
data class FasilitasResponse(
    val id: String,
    val nama_fasilitas: String,
    val deskripsi: String,
    val created_at: String?,
    val media: List<MediaResponse>
)

@Serializable
data class MediaResponse(
    val id: String,
    val file_url: String
)
