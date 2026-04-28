package infrastructure.repository

import domain.model.Dosen
import domain.repository.DosenRepository
import infrastructure.config.SupabaseConfig
import infrastructure.database.tables.DosenTable
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class DosenRepositoryImpl : DosenRepository {
    
    private val client = SupabaseConfig.getClient()
    private val tableDosen = client.postgrest["dosen"]
    private val tableUsers = client.postgrest["users"]

    override suspend fun getAll(): List<Dosen> {
        return tableDosen.select().decodeList<Dosen>()
    }

    override suspend fun getById(id: String): Dosen? {
        return tableDosen.select {
            filter { eq(DosenTable.ID, id) }
        }.decodeSingleOrNull<Dosen>()
    }

    override suspend fun getByUserId(userId: String): Dosen? {
        return tableDosen.select {
            filter { eq(DosenTable.USER_ID, userId) }
        }.decodeSingleOrNull<Dosen>()
    }

    override suspend fun getByEmail(email: String): Dosen? {
        // 1. Ambil data mentah dari tabel 'users' (pasti ada kolom id, email, password_hash, role)
        val userRow = tableUsers.select {
            filter { eq("email", email) }
        }.decodeSingleOrNull<JsonObject>() ?: return null

        val userIdFromAuth = userRow["id"]?.jsonPrimitive?.content ?: return null
        // CATATAN: Pakai string literal sesuai Database.md (snake_case)
        val passHash = userRow["password_hash"]?.jsonPrimitive?.content ?: ""
        val userRole = userRow["role"]?.jsonPrimitive?.content ?: "user"
        val userNama = userRow["nama"]?.jsonPrimitive?.content ?: ""

        // 2. Cari profil dosennya di tabel 'dosen'
        val dosenProfil = getByUserId(userIdFromAuth)

        // 3. Gabungkan jadi satu objek Dosen (Model Dosen nampung data profil + data auth)
        return (dosenProfil ?: Dosen(
            id = null,
            userId = userIdFromAuth,
            nama = userNama,
            email = email
        )).copy(
            passwordHash = passHash,
            role = userRole,
            email = email
        )
    }

    override suspend fun insert(dosen: Dosen): String {
        // Insert ke tabel 'dosen' (Database columns pakai snake_case)
        val content = buildJsonObject {
            put("nama", dosen.nama)
            put("email", dosen.email)
            dosen.nidn?.let { put("nidn", it) }
            dosen.userId?.let { put("user_id", it) }
            dosen.jabatanFungsional?.let { put("jabatan_fungsional", it) }
            dosen.pangkatGolongan?.let { put("pangkat_golongan", it) }
            dosen.noHp?.let { put("no_hp", it) }
            dosen.fotoUrl?.let { put("foto_url", it) }
        }
        val response = tableDosen.insert(content) { select() }
        val result = response.decodeSingle<Dosen>()
        return result.id!!
    }

    override suspend fun insertUser(email: String, passwordHash: String, nama: String, role: String): String {
        val content = buildJsonObject {
            put("email", email)
            put("password_hash", passwordHash)
            put("nama", nama)
            put("role", role)
        }
        // Gunakan decodeSingle agar otomatis mengambil objek pertama dari array response Supabase
        val response = tableUsers.insert(content) { select() }
        val result = response.decodeSingle<JsonObject>()
        return result["id"]?.jsonPrimitive?.content ?: throw Exception("Gagal mendapatkan User ID")
    }

    override suspend fun update(dosen: Dosen): Boolean {
        val content = buildJsonObject {
            put("nama", dosen.nama)
            put("email", dosen.email)
            dosen.nidn?.let { put("nidn", it) }
            dosen.jabatanFungsional?.let { put("jabatan_fungsional", it) }
            dosen.pangkatGolongan?.let { put("pangkat_golongan", it) }
            dosen.noHp?.let { put("no_hp", it) }
            dosen.fotoUrl?.let { put("foto_url", it) }
        }
        tableDosen.update(content) {
            filter { eq("id", dosen.id!!) }
        }
        return true
    }

    override suspend fun delete(id: String): Boolean {
        tableDosen.delete {
            filter { eq("id", id) }
        }
        return true
    }
}
