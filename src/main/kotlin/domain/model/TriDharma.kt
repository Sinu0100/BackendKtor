package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Serializable
data class TriDharma(
    val id: String? = null,
    
    @SerialName("judul") // Sesuai kolom di DB
    val judulKegiatan: String,
    
    val deskripsi: String? = null,
    
    @SerialName("tanggal") // Sesuai kolom di DB
    val tanggalKegiatan: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null,

    @Transient
    val media: List<Media> = emptyList()
)
