package infrastructure.repository

import domain.model.HKI
import domain.repository.HKIRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.HKITable
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class HKIRepositoryImpl : HKIRepository {
    private val client = SupabaseConfig.getClient()
    private val table = client.postgrest[HKITable.TABLE_NAME]

    override suspend fun getAll(): List<HKI> {
        return table.select(Columns.ALL).decodeList<HKI>()
    }

    override suspend fun getAllByDosenId(dosenId: String): List<HKI> {
        return table.select(Columns.ALL) {
            filter {
                eq(HKITable.DOSEN_ID, dosenId)
            }
        }.decodeList<HKI>()
    }

    override suspend fun getById(id: String): HKI? {
        return table.select(Columns.ALL) {
            filter {
                eq(HKITable.ID, id)
            }
        }.decodeSingleOrNull<HKI>()
    }

    override suspend fun create(hki: HKI): HKI {
        return table.insert(hki) {
            select()
        }.decodeSingle<HKI>()
    }

    override suspend fun update(hki: HKI): Boolean {
        return try {
            table.update(hki) {
                filter {
                    eq(HKITable.ID, hki.id ?: "")
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            table.delete {
                filter {
                    eq(HKITable.ID, id)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
