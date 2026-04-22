package presentation.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class DosenRequest(
    val nama: String,
    val nidn: String? = null,
    val jabatan_fungsional: String? = null,
    val pangkat_golongan: String? = null,
    val email: String? = null,
    val no_hp: String? = null,
    val foto_url: String? = null
)
