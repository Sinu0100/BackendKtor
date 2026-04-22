package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Serializable
data class Fasilitas(
    val id: String? = null,
    
    @SerialName("judul_fasilitas")
    val judulFasilitas: String,
    
    val deskripsi: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null,

    @Transient // WAJIB biar gak error pas INSERT/UPDATE
    val media: List<Media> = emptyList()
)
