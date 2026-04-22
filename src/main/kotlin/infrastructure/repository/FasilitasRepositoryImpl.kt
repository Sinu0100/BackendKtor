package infrastructure.repository

import domain.model.Fasilitas
import domain.repository.FasilitasRepository
import domain.repository.MediaRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.FasilitasTable
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class FasilitasRepositoryImpl(private val mediaRepository: MediaRepository) : FasilitasRepository {
    
    private val client = SupabaseConfig.getClient()
    private val table = client.postgrest[FasilitasTable.TABLE_NAME]

    override suspend fun getAll(): List<Fasilitas> {
        val fasilitasRaw = table.select {
            order(FasilitasTable.CREATED_AT, order = Order.DESCENDING)
        }.decodeList<Fasilitas>()

        return fasilitasRaw.map { f ->
            val media = mediaRepository.getByEntity("fasilitas", f.id!!)
            f.copy(media = media)
        }
    }

    override suspend fun getById(id: String): Fasilitas? {
        val fasilitas = table.select {
            filter { eq(FasilitasTable.ID, id) }
        }.decodeSingleOrNull<Fasilitas>() ?: return null

        val media = mediaRepository.getByEntity("fasilitas", id)
        return fasilitas.copy(media = media)
    }

    override suspend fun create(fasilitas: Fasilitas): Fasilitas {
        return table.insert(fasilitas) { select() }.decodeSingle<Fasilitas>()
    }

    override suspend fun update(fasilitas: Fasilitas): Boolean {
        return try {
            table.update(fasilitas) {
                filter { eq(FasilitasTable.ID, fasilitas.id ?: "") }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            table.delete {
                filter { eq(FasilitasTable.ID, id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
