package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Dosen(
    val id: String? = null,
    
    @SerialName("user_id")
    val userId: String? = null,
    
    val nama: String,
    val nidn: String? = null,
    
    @SerialName("jabatan_fungsional")
    val jabatanFungsional: String? = null,
    
    @SerialName("pangkat_golongan")
    val pangkatGolongan: String? = null,
    
    val email: String,
    
    @SerialName("no_hp")
    val noHp: String? = null,
    
    @SerialName("foto_url")
    val fotoUrl: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null,

    // Field Auth (diambil dari tabel users via Repository)
    @SerialName("password_hash")
    val passwordHash: String? = null,
    
    val role: String? = null
)
