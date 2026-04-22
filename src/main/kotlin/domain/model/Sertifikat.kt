package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Serializable
data class Sertifikat(
    val id: String? = null,
    @SerialName("dosen_id")
    val dosenId: String,
    @SerialName("nama_sertifikat")
    val namaSertifikat: String,
    val penerbit: String? = null,
    val tahun: Int? = null,
    @SerialName("file_url")
    val fileUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
