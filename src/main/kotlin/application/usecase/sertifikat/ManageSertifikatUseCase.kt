package application.usecase.sertifikat

import domain.model.Sertifikat
import domain.repository.SertifikatRepository
import domain.repository.DosenRepository
import application.usecase.media.ManageMediaUseCase

class ManageSertifikatUseCase(
    private val repository: SertifikatRepository,
    private val dosenRepository: DosenRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getAll(): List<Sertifikat> = repository.getAll()

    suspend fun getById(id: String): Sertifikat? = repository.getById(id)

    suspend fun getMySertifikat(userId: String): List<Sertifikat> {
        val dosen = getDosenByUserId(userId)
        return repository.getByDosen(dosen.id!!)
    }

    suspend fun create(userId: String, sertifikat: Sertifikat, fileBytes: ByteArray?, fileName: String?): Sertifikat {
        val dosen = getDosenByUserId(userId)
        val id = java.util.UUID.randomUUID().toString()
        var finalFileUrl = sertifikat.fileUrl

        if (fileBytes != null && fileName != null) {
            val media = manageMediaUseCase.uploadMediaBytes("sertifikat", id, fileName, fileBytes)
            finalFileUrl = media.fileUrl
        }

        val created = repository.create(sertifikat.copy(id = id, dosenId = dosen.id!!, fileUrl = finalFileUrl))
        return repository.getById(created.id!!) ?: created
    }

    suspend fun update(
        id: String, 
        userId: String, 
        role: String?, 
        sertifikat: Sertifikat,
        fileBytes: ByteArray?, 
        fileName: String?
    ): Sertifikat {
        val existing = repository.getById(id) ?: throw Exception("Sertifikat tidak ditemukan")
        
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN: Bukan milik Anda")
        }

        var finalFileUrl = existing.fileUrl
        if (fileBytes != null && fileName != null) {
            // HAPUS file lama dari Storage + DB sebelum upload baru
            manageMediaUseCase.deleteMediaByEntity("sertifikat", id)
            
            val media = manageMediaUseCase.uploadMediaBytes("sertifikat", id, fileName, fileBytes)
            finalFileUrl = media.fileUrl
        }

        val toUpdate = existing.copy(
            judulSertifikat = sertifikat.judulSertifikat,
            tahun = sertifikat.tahun,
            fileUrl = finalFileUrl
        )

        repository.update(toUpdate)
        return repository.getById(id) ?: toUpdate
    }

    suspend fun delete(id: String, userId: String, role: String?) {
        val existing = repository.getById(id) ?: throw Exception("Sertifikat tidak ditemukan")
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN: Bukan milik Anda")
        }
        
        manageMediaUseCase.deleteMediaByEntity("sertifikat", id)
        repository.delete(id)
    }

    private suspend fun getDosenByUserId(userId: String) =
        dosenRepository.getByUserId(userId) ?: throw Exception("Dosen not found")
}
