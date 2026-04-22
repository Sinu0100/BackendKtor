package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SertifikatRequest(
    val namaSertifikat: String,
    val penerbit: String? = null,
    val tahun: Int
)
