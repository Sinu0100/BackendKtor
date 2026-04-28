package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Sertifikat(
    val id: String? = null,
    @SerialName("dosen_id")
    val dosenId: String,
    @SerialName("judul_sertifikat")
    val judulSertifikat: String,
    val tahun: Int? = null,
    @SerialName("file_url")
    val fileUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
