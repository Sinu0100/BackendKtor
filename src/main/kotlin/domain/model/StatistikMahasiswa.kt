package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class StatistikMahasiswa(
    val id: Int? = null,
    val tahun: Int,
    
    @SerialName("jumlah_pendaftar")
    val jumlahPendaftar: Int,
    
    @SerialName("jumlah_diterima")
    val jumlahDiterima: Int,
    
    @SerialName("jumlah_lulusan")
    val jumlahLulusan: Int,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null
)
