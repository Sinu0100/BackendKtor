package infrastructure.repository

import domain.model.StatistikMahasiswa
import domain.repository.StatistikMahasiswaRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.StatistikMahasiswaTable
import io.github.jan.supabase.postgrest.postgrest

class StatistikMahasiswaRepositoryImpl : StatistikMahasiswaRepository {
    private val client = SupabaseConfig.getClient()
    private val table = client.postgrest[StatistikMahasiswaTable.TABLE_NAME]

    override suspend fun getAll(): List<StatistikMahasiswa> {
        return table.select().decodeList<StatistikMahasiswa>()
    }

    override suspend fun getByTahun(tahun: Int): StatistikMahasiswa? {
        return table.select {
            filter { eq(StatistikMahasiswaTable.TAHUN, tahun) }
        }.decodeSingleOrNull<StatistikMahasiswa>()
    }

    override suspend fun create(statistik: StatistikMahasiswa): StatistikMahasiswa {
        return table.insert(statistik) {
            select()
        }.decodeSingle<StatistikMahasiswa>()
    }

    override suspend fun update(id: Int, statistik: StatistikMahasiswa): StatistikMahasiswa {
        return table.update(statistik) {
            filter { eq(StatistikMahasiswaTable.ID, id) }
            select()
        }.decodeSingle<StatistikMahasiswa>()
    }

    override suspend fun delete(id: Int): Boolean {
        return try {
            table.delete {
                filter { eq(StatistikMahasiswaTable.ID, id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
