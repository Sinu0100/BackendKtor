package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Pengabdian(
    val id: String? = null,
    @SerialName("dosen_id")
    val dosenId: String,
    @SerialName("judul_pengabdian")
    val judulPengabdian: String,
    val deskripsi: String? = null,
    val tahun: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    
    // Media (polymorphic)
    val media: List<Media> = emptyList()
)
