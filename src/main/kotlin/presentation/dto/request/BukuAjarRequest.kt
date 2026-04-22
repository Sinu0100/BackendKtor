package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class BukuAjarRequest(
    val judul: String,
    val tahun: Int,
    val deskripsi: String? = null,
    val peranPenulis: String
)
