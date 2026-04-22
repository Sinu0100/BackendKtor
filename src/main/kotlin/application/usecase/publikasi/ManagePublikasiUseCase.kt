package application.usecase.publikasi

import domain.model.Publikasi
import domain.repository.PublikasiRepository
import domain.repository.DosenRepository
import application.usecase.media.ManageMediaUseCase

class ManagePublikasiUseCase(
    private val repository: PublikasiRepository,
    private val dosenRepository: DosenRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getAll(): List<Publikasi> = repository.getAll()

    suspend fun getById(id: String): Publikasi? = repository.getById(id)

    suspend fun getMyPublikasi(userId: String): List<Publikasi> {
        val dosen = getDosenByUserId(userId)
        return repository.getByDosen(dosen.id!!)
    }

    suspend fun create(userId: String, publikasi: Publikasi): Publikasi {
        val dosen = getDosenByUserId(userId)
        return repository.create(publikasi.copy(dosenId = dosen.id!!))
    }

    suspend fun update(id: String, userId: String, role: String?, publikasi: Publikasi): Publikasi {
        val existing = repository.getById(id) ?: throw Exception("Publikasi tidak ditemukan")
        
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN: Bukan milik Anda")
        }

        repository.update(publikasi.copy(id = id, dosenId = existing.dosenId))
        return repository.getById(id)!!
    }

    suspend fun delete(id: String, userId: String, role: String?) {
        val existing = repository.getById(id) ?: throw Exception("Publikasi tidak ditemukan")

        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) {
                throw Exception("FORBIDDEN: Anda bukan pemilik data publikasi ini")
            }
        }
        repository.delete(id)
    }

    private suspend fun getDosenByUserId(userId: String) =
        dosenRepository.getByUserId(userId)
            ?: throw Exception("Profil dosen tidak ditemukan untuk user ini")
}
