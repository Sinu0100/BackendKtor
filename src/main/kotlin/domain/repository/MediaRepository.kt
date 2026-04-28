package domain.repository

import domain.model.Media

interface MediaRepository {
    suspend fun getById(id: String): Media?
    suspend fun getByEntity(entityType: String, entityId: String): List<Media>
    suspend fun create(media: Media): Media
    suspend fun delete(id: String): Boolean
    suspend fun deleteByEntity(entityType: String, entityId: String): Boolean
    suspend fun deleteByUrl(url: String): Boolean
}
