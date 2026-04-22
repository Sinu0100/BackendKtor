package infrastructure.repository

import domain.model.Sertifikat
import domain.repository.SertifikatRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.SertifikatTable
import io.github.jan.supabase.postgrest.postgrest

class SertifikatRepositoryImpl : SertifikatRepository {
    private val client = SupabaseConfig.getClient()

    override suspend fun getAll(): List<Sertifikat> {
        return client.postgrest[SertifikatTable.TABLE_NAME]
            .select().decodeList<Sertifikat>()
    }

    override suspend fun getById(id: String): Sertifikat? {
        return client.postgrest[SertifikatTable.TABLE_NAME]
            .select {
                filter { eq(SertifikatTable.ID, id) }
            }.decodeSingleOrNull<Sertifikat>()
    }

    override suspend fun getByDosen(dosenId: String): List<Sertifikat> {
        return client.postgrest[SertifikatTable.TABLE_NAME]
            .select {
                filter { eq(SertifikatTable.DOSEN_ID, dosenId) }
            }.decodeList<Sertifikat>()
    }

    override suspend fun create(sertifikat: Sertifikat): Sertifikat {
        return client.postgrest[SertifikatTable.TABLE_NAME]
            .insert(sertifikat) {
                select()
            }.decodeSingle<Sertifikat>()
    }

    override suspend fun update(sertifikat: Sertifikat): Boolean {
        client.postgrest[SertifikatTable.TABLE_NAME]
            .update(sertifikat) {
                filter { eq(SertifikatTable.ID, sertifikat.id!!) }
            }
        return true
    }

    override suspend fun delete(id: String): Boolean {
        client.postgrest[SertifikatTable.TABLE_NAME]
            .delete {
                filter { eq(SertifikatTable.ID, id) }
            }
        return true
    }
}
