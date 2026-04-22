package infrastructure.repository

import domain.model.BukuAjar
import domain.repository.BukuAjarRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.BukuAjarTable
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class BukuAjarRepositoryImpl : BukuAjarRepository {
    private val client = SupabaseConfig.getClient()

    override suspend fun getAll(): List<BukuAjar> {
        return client.postgrest[BukuAjarTable.TABLE_NAME]
            .select().decodeList<BukuAjar>()
    }

    override suspend fun getById(id: String): BukuAjar? {
        return client.postgrest[BukuAjarTable.TABLE_NAME]
            .select {
                filter { eq(BukuAjarTable.ID, id) }
            }.decodeSingleOrNull<BukuAjar>()
    }

    override suspend fun getByDosen(dosenId: String): List<BukuAjar> {
        return client.postgrest[BukuAjarTable.TABLE_NAME]
            .select {
                filter { eq(BukuAjarTable.DOSEN_ID, dosenId) }
            }.decodeList<BukuAjar>()
    }

    override suspend fun create(buku: BukuAjar): BukuAjar {
        return client.postgrest[BukuAjarTable.TABLE_NAME]
            .insert(buku) {
                select()
            }.decodeSingle<BukuAjar>()
    }

    override suspend fun update(buku: BukuAjar): Boolean {
        client.postgrest[BukuAjarTable.TABLE_NAME]
            .update(buku) {
                filter { eq(BukuAjarTable.ID, buku.id!!) }
            }
        return true
    }

    override suspend fun delete(id: String): Boolean {
        client.postgrest[BukuAjarTable.TABLE_NAME]
            .delete {
                filter { eq(BukuAjarTable.ID, id) }
            }
        return true
    }
}
