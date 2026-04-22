package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class StatistikMahasiswaRequest(
    val tahun: Int,
    val jumlah_pendaftar: Int,
    val jumlah_diterima: Int,
    val jumlah_lulusan: Int
)
