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

    suspend fun create(userId: String, sertifikat: Sertifikat, fileBytes: ByteArray?, fileName: String): Sertifikat {
        val dosen = getDosenByUserId(userId)
        var fileUrl = sertifikat.fileUrl
        
        if (fileBytes != null && fileBytes.size > 0) {
            val media = manageMediaUseCase.uploadMediaBytes("sertifikat", "temp", fileName, fileBytes)
            fileUrl = media.fileUrl
        }

        return repository.create(sertifikat.copy(dosenId = dosen.id!!, fileUrl = fileUrl))
    }

    suspend fun update(
        id: String, userId: String, role: String?, 
        nama: String?, penerbit: String?, tahun: Int?, 
        fileBytes: ByteArray?, fileName: String
    ): Sertifikat {
        val existing = repository.getById(id) ?: throw Exception("Not Found")
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN")
        }

        var fileUrl = existing.fileUrl
        if (fileBytes != null && fileBytes.size > 0) {
            val media = manageMediaUseCase.uploadMediaBytes("sertifikat", id, fileName, fileBytes)
            fileUrl = media.fileUrl
        }

        val updated = existing.copy(
            namaSertifikat = nama ?: existing.namaSertifikat,
            penerbit = penerbit ?: existing.penerbit,
            tahun = tahun ?: existing.tahun,
            fileUrl = fileUrl
        )

        repository.update(updated)
        return repository.getById(id)!!
    }

    suspend fun delete(id: String, userId: String, role: String?) {
        val existing = repository.getById(id) ?: throw Exception("Not Found")
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN")
        }
        repository.delete(id)
    }

    private suspend fun getDosenByUserId(userId: String) =
        dosenRepository.getByUserId(userId) ?: throw Exception("Dosen not found")
}
