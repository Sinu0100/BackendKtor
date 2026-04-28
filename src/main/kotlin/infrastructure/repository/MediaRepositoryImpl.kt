package infrastructure.repository

import domain.model.Media
import domain.repository.MediaRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.MediaTable
import io.github.jan.supabase.postgrest.postgrest

class MediaRepositoryImpl : MediaRepository {
    private val client = SupabaseConfig.getClient()
    private val table = client.postgrest[MediaTable.TABLE_NAME]

    override suspend fun getById(id: String): Media? {
        return try {
            table.select {
                filter { eq(MediaTable.ID, id) }
            }.decodeSingleOrNull<Media>()
        } catch (e: Exception) {
            null
        }
    }
    override suspend fun getByEntity(entityType: String, entityId: String): List<Media> {
        return table.select {
            filter {
                eq(MediaTable.ENTITY_TYPE, entityType)
                // Jika entityId tidak kosong, filter spesifik. Jika kosong, ambil semua media tipe tersebut.
                if (entityId.isNotEmpty()) {
                    eq(MediaTable.ENTITY_ID, entityId)
                }
            }
        }.decodeList<Media>()
    }

    override suspend fun create(media: Media): Media {
        return table.insert(media) {
            select()
        }.decodeSingle()
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            table.delete {
                filter { eq(MediaTable.ID, id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteByEntity(entityType: String, entityId: String): Boolean {
        return try {
            table.delete {
                filter {
                    eq(MediaTable.ENTITY_TYPE, entityType)
                    eq(MediaTable.ENTITY_ID, entityId)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteByUrl(url: String): Boolean {
        return try {
            table.delete {
                filter { eq(MediaTable.FILE_URL, url) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
