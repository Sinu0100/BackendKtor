package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class BukuAjar(
    val id: String? = null,
    @SerialName("dosen_id")
    val dosenId: String,
    val judul: String,
    val tahun: Int? = null,
    val deskripsi: String? = null,
    @SerialName("peran_penulis")
    val peranPenulis: String = "Anggota",
    @SerialName("created_at")
    val createdAt: String? = null
)
