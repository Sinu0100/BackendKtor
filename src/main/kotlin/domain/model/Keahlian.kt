package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Keahlian(
    val id: Int? = null,
    val nama_keahlian: String
)

@Serializable
data class DosenKeahlian(
    val id: Int? = null,
    val dosen_id: String,
    val keahlian_id: Int,
    val nama_keahlian: String? = null
)
