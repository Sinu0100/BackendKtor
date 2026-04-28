package presentation.dto.response

import domain.model.Keahlian
import kotlinx.serialization.Serializable

@Serializable
data class DosenResponse(
    val id: String,
    val nama: String,
    val nidn: String? = null,
    val jabatanFungsional: String? = null,
    val pangkatGolongan: String? = null,
    val email: String,
    val noHp: String? = null,
    val fotoUrl: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val role: String? = null,
    val keahlian: List<Keahlian>
)
