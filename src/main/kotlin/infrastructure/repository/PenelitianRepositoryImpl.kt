package infrastructure.repository

import domain.model.Penelitian
import domain.model.PenelitianAnggota
import domain.repository.MediaRepository
import domain.repository.PenelitianRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.PenelitianTable
import io.github.jan.supabase.postgrest.postgrest

class PenelitianRepositoryImpl(
    private val mediaRepository: MediaRepository
) : PenelitianRepository {
    private val client = SupabaseConfig.getClient()
    private val table = client.postgrest[PenelitianTable.TABLE_NAME]
    private val anggotaTable = client.postgrest[PenelitianTable.Anggota.TABLE_NAME]

    override suspend fun getAll(): List<Penelitian> {
        val baseData = table.select().decodeList<Penelitian>()
        return baseData.map { fillPenelitianData(it) }
    }

    override suspend fun getById(id: String): Penelitian? {
        val research = table.select {
            filter { eq(PenelitianTable.ID, id) }
        }.decodeSingleOrNull<Penelitian>() ?: return null
        
        return fillPenelitianData(research)
    }

    override suspend fun getByDosen(dosenId: String): List<Penelitian> {
        val baseData = table.select {
            filter { eq(PenelitianTable.DOSEN_ID, dosenId) }
        }.decodeList<Penelitian>()
        return baseData.map { fillPenelitianData(it) }
    }

    private suspend fun fillPenelitianData(penelitian: Penelitian): Penelitian {
        val id = penelitian.id!!
        val media = mediaRepository.getByEntity("penelitian", id)
        val anggota = anggotaTable.select {
            filter { eq(PenelitianTable.Anggota.PENELITIAN_ID, id) }
        }.decodeList<PenelitianAnggota>()
        
        return penelitian.copy(media = media, anggota = anggota)
    }

    override suspend fun create(penelitian: Penelitian): Penelitian {
        val created = table.insert(penelitian) {
            select()
        }.decodeSingle<Penelitian>()

        // Handle initial anggota if any
        penelitian.anggota.forEach { 
            addAnggota(created.id!!, it.dosenId, it.peran)
        }

        return fillPenelitianData(created)
    }

    override suspend fun update(penelitian: Penelitian): Boolean {
        return try {
            table.update(penelitian) {
                filter { eq(PenelitianTable.ID, penelitian.id ?: "") }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            table.delete {
                filter { eq(PenelitianTable.ID, id) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun addAnggota(penelitianId: String, dosenId: String, peran: String): Boolean {
        return try {
            anggotaTable.insert(mapOf(
                PenelitianTable.Anggota.PENELITIAN_ID to penelitianId,
                PenelitianTable.Anggota.DOSEN_ID to dosenId,
                PenelitianTable.Anggota.PERAN to peran
            ))
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun removeAnggota(penelitianId: String, dosenId: String): Boolean {
        return try {
            anggotaTable.delete {
                filter {
                    eq(PenelitianTable.Anggota.PENELITIAN_ID, penelitianId)
                    eq(PenelitianTable.Anggota.DOSEN_ID, dosenId)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
