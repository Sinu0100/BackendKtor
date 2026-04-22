package infrastructure.repository

import domain.model.JadwalPerkuliahan
import domain.repository.JadwalPerkuliahanRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.JadwalTable
import io.github.jan.supabase.postgrest.postgrest

class JadwalPerkuliahanRepositoryImpl : JadwalPerkuliahanRepository {
    private val client = SupabaseConfig.getClient()
    private val table = client.postgrest[JadwalTable.TABLE_NAME]

    override suspend fun getAll(): List<JadwalPerkuliahan> {
        return table.select().decodeList<JadwalPerkuliahan>()
    }

    override suspend fun getById(id: Int): JadwalPerkuliahan? {
        return table.select {
            filter { eq(JadwalTable.ID, id) }
        }.decodeSingleOrNull<JadwalPerkuliahan>()
    }

    override suspend fun create(jadwal: JadwalPerkuliahan): JadwalPerkuliahan {
        return table.insert(jadwal) {
            select()
        }.decodeSingle<JadwalPerkuliahan>()
    }

    override suspend fun update(id: Int, jadwal: JadwalPerkuliahan): JadwalPerkuliahan {
        return table.update(jadwal) {
            filter { eq(JadwalTable.ID, id) }
            select()
        }.decodeSingle<JadwalPerkuliahan>()
    }

    override suspend fun delete(id: Int): Boolean {
        return try {
            table.delete {
                filter { eq(JadwalTable.ID, id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
