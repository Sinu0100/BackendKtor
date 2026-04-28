package application.usecase.dosen

import domain.model.Dosen
import domain.repository.DosenRepository
import application.usecase.media.ManageMediaUseCase
import org.mindrot.jbcrypt.BCrypt

class ManageDosenUseCase(
    private val repository: DosenRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getDosenById(id: String): Dosen? {
        return repository.getById(id)
    }

    suspend fun createDosenWithUser(dosen: Dosen, password: String, photo: Pair<String, ByteArray>?, role: String?): Dosen {
        if (role != "admin") {
            throw Exception("FORBIDDEN: Hanya Admin yang dapat menambah dosen")
        }

        // 1. Cek email duplikat
        val existing = repository.getByEmail(dosen.email)
        if (existing?.id != null || existing?.userId != null) {
            throw Exception("Email sudah terdaftar")
        }

        // 2. Hash Password
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())

        // 3. Create User Akun dulu
        val userId = repository.insertUser(
            email = dosen.email,
            passwordHash = passwordHash,
            nama = dosen.nama,
            role = "dosen"
        )

        // 4. Upload foto jika ada
        var photoUrl: String? = null
        if (photo != null) {
            photoUrl = manageMediaUseCase.uploadFile("dosen", photo.first, photo.second)
        }

        // 5. Create Profil Dosen (link ke user_id)
        val dosenProfile = dosen.copy(userId = userId, fotoUrl = photoUrl)
        val dosenId = repository.insert(dosenProfile)
        
        return dosenProfile.copy(id = dosenId)
    }

    suspend fun getProfileMe(userId: String?): Dosen {
        if (userId == null) throw Exception("UNAUTHORIZED")
        return repository.getByUserId(userId) ?: throw Exception("Profil dosen tidak ditemukan")
    }

    suspend fun updateDosen(id: String, dosen: Dosen, photo: Pair<String, ByteArray>?, role: String?): Dosen {
        if (role != "admin" && role != "authenticated") {
            throw Exception("FORBIDDEN: Hanya Admin yang dapat mengubah dosen")
        }

        val existing = repository.getById(id) ?: throw Exception("Dosen tidak ditemukan")
        
        var photoUrl = existing.fotoUrl
        if (photo != null) {
            // HAPUS foto lama dari storage sebelum upload baru
            if (existing.fotoUrl != null) {
                try {
                    manageMediaUseCase.deleteMediaByUrl(existing.fotoUrl!!)
                } catch (e: Exception) {
                    println("WARNING: Gagal hapus foto lama: ${e.message}")
                }
            }
            photoUrl = manageMediaUseCase.uploadFile("dosen", photo.first, photo.second)
        }

        val updatedDosen = dosen.copy(id = id, fotoUrl = photoUrl)
        val success = repository.update(updatedDosen)
        if (!success) throw Exception("Gagal memperbarui data dosen")
        
        return updatedDosen
    }

    suspend fun deleteDosen(id: String, role: String?) {
        if (role != "admin" && role != "authenticated") {
            throw Exception("FORBIDDEN: Hanya Admin yang dapat menghapus dosen")
        }

        repository.getById(id) ?: throw Exception("Dosen tidak ditemukan")
        manageMediaUseCase.deleteMediaByEntity("dosen", id)
        
        repository.delete(id)
    }
}
