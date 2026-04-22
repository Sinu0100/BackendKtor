package infrastructure.repository

import domain.model.Publikasi
import domain.repository.PublikasiRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.PublikasiTable
import io.github.jan.supabase.postgrest.postgrest

class PublikasiRepositoryImpl : PublikasiRepository {
    private val client = SupabaseConfig.getClient()
    private val table = client.postgrest[PublikasiTable.TABLE_NAME]

    override suspend fun getAll(): List<Publikasi> {
        return table.select().decodeList<Publikasi>()
    }

    override suspend fun getById(id: String): Publikasi? {
        return table.select {
            filter { eq(PublikasiTable.ID, id) }
        }.decodeSingleOrNull<Publikasi>()
    }

    override suspend fun getByDosen(dosenId: String): List<Publikasi> {
        return table.select {
            filter { eq(PublikasiTable.DOSEN_ID, dosenId) }
        }.decodeList<Publikasi>()
    }

    override suspend fun create(publikasi: Publikasi): Publikasi {
        return table.insert(publikasi) {
            select()
        }.decodeSingle<Publikasi>()
    }

    override suspend fun update(publikasi: Publikasi): Boolean {
        return try {
            table.update(publikasi) {
                filter { eq(PublikasiTable.ID, publikasi.id ?: "") }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            table.delete {
                filter { eq(PublikasiTable.ID, id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
