package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@Serializable
data class Publikasi(
    val id: String? = null,
    @SerialName("dosen_id")
    val dosenId: String,
    val judul: String,
    @SerialName("nama_jurnal_konferensi")
    val namaJurnalKonferensi: String? = null,
    val deskripsi: String? = null,
    @SerialName("link_tautan")
    val linkTautan: String? = null,
    val tahun: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
