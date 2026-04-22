package infrastructure.repository

import domain.model.Pengabdian
import domain.repository.MediaRepository
import domain.repository.PengabdianRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.PengabdianTable
import io.github.jan.supabase.postgrest.postgrest

class PengabdianRepositoryImpl(
    private val mediaRepository: MediaRepository
) : PengabdianRepository {
    private val client = SupabaseConfig.getClient()
    private val table = client.postgrest[PengabdianTable.TABLE_NAME]

    override suspend fun getAll(): List<Pengabdian> {
        val baseData = table.select().decodeList<Pengabdian>()
        return baseData.map { fillPengabdianData(it) }
    }

    override suspend fun getById(id: String): Pengabdian? {
        val data = table.select {
            filter { eq(PengabdianTable.ID, id) }
        }.decodeSingleOrNull<Pengabdian>() ?: return null
        
        return fillPengabdianData(data)
    }

    override suspend fun getByDosen(dosenId: String): List<Pengabdian> {
        val baseData = table.select {
            filter { eq(PengabdianTable.DOSEN_ID, dosenId) }
        }.decodeList<Pengabdian>()
        return baseData.map { fillPengabdianData(it) }
    }

    private suspend fun fillPengabdianData(pengabdian: Pengabdian): Pengabdian {
        val id = pengabdian.id!!
        val media = mediaRepository.getByEntity("pengabdian", id)
        return pengabdian.copy(media = media)
    }

    override suspend fun create(pengabdian: Pengabdian): Pengabdian {
        val created = table.insert(pengabdian) {
            select()
        }.decodeSingle<Pengabdian>()

        return fillPengabdianData(created)
    }

    override suspend fun update(pengabdian: Pengabdian): Boolean {
        return try {
            table.update(pengabdian) {
                filter { eq(PengabdianTable.ID, pengabdian.id ?: "") }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            table.delete {
                filter { eq(PengabdianTable.ID, id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
