package infrastructure.repository

import domain.model.Keahlian
import domain.model.DosenKeahlian
import domain.repository.KeahlianRepository
import infrastructure.config.SupabaseConfig
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.*

class KeahlianRepositoryImpl : KeahlianRepository {
    private val client = SupabaseConfig.getClient()
    private val tableKeahlian = client.postgrest["keahlian"]
    private val tableDosenKeahlian = client.postgrest["dosen_keahlian"]

    override suspend fun getAll(): List<Keahlian> {
        return tableKeahlian.select().decodeList<Keahlian>()
    }

    override suspend fun create(nama: String): Keahlian {
        val content = buildJsonObject {
            put("nama_keahlian", nama)
        }
        return tableKeahlian.insert(content) { select() }.decodeSingle<Keahlian>()
    }

    override suspend fun delete(id: Int) {
        tableKeahlian.delete {
            filter { eq("id", id) }
        }
    }

    override suspend fun getByDosenId(dosenId: String): List<DosenKeahlian> {
        return try {
            // Kita ambil data dari dosen_keahlian join keahlian
            val response = tableDosenKeahlian.select(Columns.list("id", "dosen_id", "keahlian_id", "keahlian(nama_keahlian)")) {
                filter {
                    eq("dosen_id", dosenId)
                }
            }
            
            val jsonArray = response.decodeAs<JsonArray>()
            println("DEBUG KEAHLIAN DB: $jsonArray") // LIAT DI CONSOLE ANDROID STUDIO LU!
            
            jsonArray.map { element ->
                val obj = element.jsonObject
                
                // Ambil ID Keahlian
                val kId = obj["keahlian_id"]?.jsonPrimitive?.intOrNull ?: 0
                
                // Bongkar nested "keahlian": { "nama_keahlian": "..." }
                val nested = obj["keahlian"]?.jsonObject
                val namaK = nested?.get("nama_keahlian")?.jsonPrimitive?.content ?: "Unknown"
                
                DosenKeahlian(
                    id = obj["id"]?.jsonPrimitive?.intOrNull,
                    dosen_id = dosenId,
                    keahlian_id = kId,
                    nama_keahlian = namaK
                )
            }
        } catch (e: Exception) {
            println("FATAL ERROR MAPPING KEAHLIAN: ${e.message}")
            emptyList()
        }
    }

    override suspend fun addKeahlianToDosen(dosenId: String, keahlianId: Int) {
        val content = buildJsonObject {
            put("dosen_id", dosenId)
            put("keahlian_id", keahlianId)
        }
        tableDosenKeahlian.insert(content)
    }

    override suspend fun removeKeahlianFromDosen(dosenId: String, keahlianId: Int) {
        tableDosenKeahlian.delete {
            filter {
                eq("dosen_id", dosenId)
                eq("keahlian_id", keahlianId)
            }
        }
    }
}
