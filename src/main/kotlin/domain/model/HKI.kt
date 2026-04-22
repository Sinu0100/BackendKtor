package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Serializable
data class HKI(
    val id: String? = null,
    @SerialName("dosen_id")
    val dosenId: String,
    val judul: String,
    val tahun: Int? = null,
    val deskripsi: String? = null,
    @SerialName("file_url")
    val fileUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
