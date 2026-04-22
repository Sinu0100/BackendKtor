package presentation.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class StatistikMahasiswaResponse(
    val id: Int,
    val tahun: Int,
    val jumlah_pendaftar: Int,
    val jumlah_diterima: Int,
    val jumlah_lulusan: Int
)
