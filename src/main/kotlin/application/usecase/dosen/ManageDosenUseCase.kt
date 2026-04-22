package application.usecase.dosen

import domain.model.Dosen
import domain.repository.DosenRepository
import application.usecase.media.ManageMediaUseCase

class ManageDosenUseCase(
    private val repository: DosenRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getDosenById(id: String): Dosen? {
        return repository.getById(id)
    }

    suspend fun createDosen(dosen: Dosen, photo: Pair<String, ByteArray>?, role: String?): Dosen {
        if (role != "admin" && role != "authenticated") {
            throw Exception("FORBIDDEN: Hanya Admin yang dapat menambah dosen")
        }

        var photoUrl: String? = null
        if (photo != null) {
            photoUrl = manageMediaUseCase.uploadFile("dosen", photo.first, photo.second)
        }

        val newDosen = dosen.copy(fotoUrl = photoUrl)
        val id = repository.insert(newDosen)
        
        // Update entityId media jika perlu, tapi untuk foto profil biasanya langsung simpan URL di tabel dosen
        return newDosen.copy(id = id)
    }

    suspend fun updateDosen(id: String, dosen: Dosen, photo: Pair<String, ByteArray>?, role: String?): Dosen {
        if (role != "admin" && role != "authenticated") {
            throw Exception("FORBIDDEN: Hanya Admin yang dapat mengubah dosen")
        }

        val existing = repository.getById(id) ?: throw Exception("Dosen tidak ditemukan")
        
        var photoUrl = existing.fotoUrl
        if (photo != null) {
            // Kita asumsikan storage service menghapus file lama via URL atau entity
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
