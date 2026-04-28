package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Serializable
data class Penelitian(
    val id: String? = null,
    @SerialName("dosen_id")
    val dosenId: String,
    val judul: String,
    val tahun: Int? = null,
    val deskripsi: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @Transient
    val media: List<Media> = emptyList(),
    @Transient
    val anggota: List<PenelitianAnggota> = emptyList()
)

@Serializable
data class PenelitianAnggota(
    val id: Int? = null, // Fix: ID di DB itu Serial (Int)
    @SerialName("penelitian_id")
    val penelitianId: String? = null,
    @SerialName("dosen_id")
    val dosenId: String,
    val peran: String,
    @Transient // Kita isi manual dari hasil JOIN
    val namaDosen: String = ""
)
