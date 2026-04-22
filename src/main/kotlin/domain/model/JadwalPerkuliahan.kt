package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class JadwalPerkuliahan(
    val id: Int? = null,
    
    @SerialName("nama_jadwal")
    val namaJadwal: String,
    
    @SerialName("tanggal_upload")
    val tanggalUpload: String? = null,
    
    @SerialName("file_url")
    val fileUrl: String
)
