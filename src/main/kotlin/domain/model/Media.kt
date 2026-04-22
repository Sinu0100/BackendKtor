package domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Media(
    val id: String? = null,
    
    @SerialName("entity_type")
    val entityType: String,
    
    @SerialName("entity_id")
    val entityId: String,
    
    @SerialName("file_url")
    val fileUrl: String,

    @SerialName("tipe_file")
    val tipeFile: String = "image",
    
    @SerialName("created_at")
    val createdAt: String? = null
)
