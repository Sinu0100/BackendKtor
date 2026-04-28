package infrastructure.repository

import domain.model.Penelitian
import domain.model.PenelitianAnggota
import domain.repository.MediaRepository
import domain.repository.PenelitianRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.PenelitianTable
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.serialization.json.*

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
        
        // Ambil Anggota + JOIN ke tabel Dosen biar dapet Nama
        val response = anggotaTable.select(Columns.list("id", "penelitian_id", "dosen_id", "peran", "dosen(nama)")) {
            filter { eq("penelitian_id", id) }
        }
        
        val jsonArray = response.decodeAs<JsonArray>()
        val anggotaList = jsonArray.map { element ->
            val obj = element.jsonObject
            val nestedDosen = obj["dosen"]?.jsonObject
            val namaDosen = nestedDosen?.get("nama")?.jsonPrimitive?.content ?: "Unknown"
            
            PenelitianAnggota(
                id = obj["id"]?.jsonPrimitive?.intOrNull,
                penelitianId = id,
                dosenId = obj["dosen_id"]?.jsonPrimitive?.content ?: "",
                peran = obj["peran"]?.jsonPrimitive?.content ?: "Anggota",
                namaDosen = namaDosen
            )
        }
        
        return penelitian.copy(media = media, anggota = anggotaList)
    }

    override suspend fun create(penelitian: Penelitian): Penelitian {
        val content = buildJsonObject {
            put("dosen_id", penelitian.dosenId)
            put("judul", penelitian.judul)
            put("tahun", penelitian.tahun)
            put("deskripsi", penelitian.deskripsi)
        }
        
        return table.insert(content) {
            select()
        }.decodeSingle<Penelitian>()
    }

    override suspend fun update(penelitian: Penelitian): Boolean {
        return try {
            val content = buildJsonObject {
                put("judul", penelitian.judul)
                put("tahun", penelitian.tahun)
                put("deskripsi", penelitian.deskripsi)
            }
            table.update(content) {
                filter { eq(PenelitianTable.ID, penelitian.id ?: "") }
            }
            true
        } catch (e: Exception) {
            println("ERROR REPO [update]: ${e.message}")
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
            println("DEBUG REPO: Inserting Tim -> PenID: $penelitianId, DosenID: $dosenId, Peran: $peran")
            val content = buildJsonObject {
                put("penelitian_id", penelitianId)
                put("dosen_id", dosenId)
                put("peran", peran)
            }
            // Tambahkan select() untuk memastikan data masuk
            anggotaTable.insert(content)
            println("DEBUG REPO: Sukses nambahin $peran ($dosenId)")
            true
        } catch (e: Exception) {
            println("CRITICAL ERROR REPO [addAnggota]: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override suspend fun removeAnggota(penelitianId: String, dosenId: String): Boolean {
        return try {
            anggotaTable.delete {
                filter {
                    eq("penelitian_id", penelitianId)
                    eq("dosen_id", dosenId)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
