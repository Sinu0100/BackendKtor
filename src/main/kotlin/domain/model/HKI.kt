package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class HKI(
    val id: String? = null,
    @SerialName("dosen_id")
    val dosenId: String,
    @SerialName("judul_invensi")
    val judulInvensi: String,
    val inventor: String? = null,
    @SerialName("jenis_hki")
    val jenisHki: String? = null,
    @SerialName("nomor_paten")
    val nomorPaten: String? = null,
    val tahun: Int? = null,
    @SerialName("file_url")
    val fileUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)
