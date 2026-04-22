package application.usecase.hki

import domain.model.HKI
import domain.repository.HKIRepository
import domain.repository.DosenRepository
import application.usecase.media.ManageMediaUseCase

class ManageHKIUseCase(
    private val repository: HKIRepository,
    private val dosenRepository: DosenRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getAll(): List<HKI> = repository.getAll()

    suspend fun getById(id: String): HKI? = repository.getById(id)

    suspend fun create(userId: String, hki: HKI, fileBytes: ByteArray?, fileName: String): HKI {
        val dosen = getDosenByUserId(userId)
        var fileUrl = hki.fileUrl
        
        if (fileBytes != null && fileBytes.size > 0) {
            val media = manageMediaUseCase.uploadMediaBytes("hki", "temp", fileName, fileBytes)
            fileUrl = media.fileUrl
        }

        return repository.create(hki.copy(dosenId = dosen.id!!, fileUrl = fileUrl))
    }

    suspend fun update(
        id: String, userId: String, role: String?, 
        judul: String?, tahun: Int?, deskripsi: String?, 
        fileBytes: ByteArray?, fileName: String
    ): HKI {
        val existing = repository.getById(id) ?: throw Exception("HKI tidak ditemukan")
        
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN: Bukan milik Anda")
        }

        var fileUrl = existing.fileUrl
        if (fileBytes != null && fileBytes.size > 0) {
            val media = manageMediaUseCase.uploadMediaBytes("hki", id, fileName, fileBytes)
            fileUrl = media.fileUrl
        }

        val updated = existing.copy(
            judul = judul ?: existing.judul,
            tahun = tahun ?: existing.tahun,
            deskripsi = deskripsi ?: existing.deskripsi,
            fileUrl = fileUrl
        )

        repository.update(updated)
        return repository.getById(id)!!
    }

    suspend fun delete(id: String, userId: String, role: String?) {
        val existing = repository.getById(id) ?: throw Exception("HKI tidak ditemukan")
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN: Bukan milik Anda")
        }
        
        // Opsional: hapus media terkait
        manageMediaUseCase.deleteMediaByEntity("hki", id)

        repository.delete(id)
    }

    private suspend fun getDosenByUserId(userId: String) =
        dosenRepository.getByUserId(userId) ?: throw Exception("Dosen not found")
}
