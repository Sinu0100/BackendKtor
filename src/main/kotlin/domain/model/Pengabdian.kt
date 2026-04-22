package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Serializable
data class Pengabdian(
    val id: String? = null,
    @SerialName("dosen_id")
    val dosenId: String,
    @SerialName("judul_pengabdian")
    val judul: String,
    val deskripsi: String? = null,
    val tahun: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @Transient
    val media: List<Media> = emptyList()
)
