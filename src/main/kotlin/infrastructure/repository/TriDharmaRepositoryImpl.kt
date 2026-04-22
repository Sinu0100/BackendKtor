package infrastructure.repository

import domain.model.TriDharma
import domain.repository.TriDharmaRepository
import domain.repository.MediaRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.TriDharmaTable
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class TriDharmaRepositoryImpl(private val mediaRepository: MediaRepository) : TriDharmaRepository {
    
    private val client = SupabaseConfig.getClient()
    private val table = client.postgrest[TriDharmaTable.TABLE_NAME]

    override suspend fun getAll(): List<TriDharma> {
        val triDharmaRaw = table.select {
            order(TriDharmaTable.CREATED_AT, order = Order.DESCENDING)
        }.decodeList<TriDharma>()

        return triDharmaRaw.map { td ->
            val media = mediaRepository.getByEntity("tri_dharma", td.id!!)
            td.copy(media = media)
        }
    }

    override suspend fun getById(id: String): TriDharma? {
        val triDharma = table.select {
            filter { eq(TriDharmaTable.ID, id) }
        }.decodeSingleOrNull<TriDharma>() ?: return null

        val media = mediaRepository.getByEntity("tri_dharma", id)
        return triDharma.copy(media = media)
    }

    override suspend fun create(triDharma: TriDharma): TriDharma {
        return table.insert(triDharma) { select() }.decodeSingle<TriDharma>()
    }

    override suspend fun update(triDharma: TriDharma): Boolean {
        return try {
            table.update(triDharma) {
                filter { eq(TriDharmaTable.ID, triDharma.id ?: "") }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            table.delete {
                filter { eq(TriDharmaTable.ID, id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
