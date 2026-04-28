package application.usecase.pengabdian

import domain.model.Pengabdian
import domain.repository.PengabdianRepository
import domain.repository.DosenRepository
import application.usecase.media.ManageMediaUseCase

class ManagePengabdianUseCase(
    private val repository: PengabdianRepository,
    private val dosenRepository: DosenRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getAll(): List<Pengabdian> = repository.getAll()

    suspend fun getById(id: String): Pengabdian? = repository.getById(id)

    suspend fun getMyPengabdian(userId: String): List<Pengabdian> {
        val dosen = getDosenByUserId(userId)
        return repository.getByDosen(dosen.id!!)
    }

    suspend fun createWithMedia(
        userId: String, 
        pengabdian: Pengabdian, 
        files: List<Pair<String, ByteArray>>
    ): Pengabdian {
        val dosen = getDosenByUserId(userId)
        
        // 1. Simpan data utama
        val created = repository.create(pengabdian.copy(dosenId = dosen.id!!))

        // 2. Upload file media (bisa banyak)
        files.forEach { (fileName, bytes) ->
            manageMediaUseCase.uploadMediaBytes("pengabdian", created.id!!, fileName, bytes)
        }

        return repository.getById(created.id!!) ?: created
    }

    suspend fun updateWithMedia(
        id: String, 
        userId: String, 
        role: String?, 
        pengabdian: Pengabdian,
        files: List<Pair<String, ByteArray>>
    ): Pengabdian {
        val existing = repository.getById(id) ?: throw Exception("Pengabdian tidak ditemukan")
        
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN: Bukan milik Anda")
        }

        val toUpdate = existing.copy(
            judulPengabdian = pengabdian.judulPengabdian,
            deskripsi = pengabdian.deskripsi,
            tahun = pengabdian.tahun
        )
        
        repository.update(toUpdate)

        // Upload file baru kalau ada
        files.forEach { (fileName, bytes) ->
            manageMediaUseCase.uploadMediaBytes("pengabdian", id, fileName, bytes)
        }

        return repository.getById(id) ?: toUpdate
    }

    suspend fun delete(id: String, userId: String, role: String?) {
        val existing = repository.getById(id) ?: throw Exception("Pengabdian tidak ditemukan")
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN: Bukan milik Anda")
        }

        manageMediaUseCase.deleteMediaByEntity("pengabdian", id)
        repository.delete(id)
    }

    private suspend fun getDosenByUserId(userId: String) =
        dosenRepository.getByUserId(userId) ?: throw Exception("Dosen not found")
}
