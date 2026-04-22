package application.usecase.tri_dharma

import domain.model.TriDharma
import domain.repository.TriDharmaRepository
import application.usecase.media.ManageMediaUseCase

class ManageTriDharmaUseCase(
    private val triDharmaRepository: TriDharmaRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getAllTriDharma(): List<TriDharma> {
        return triDharmaRepository.getAll()
    }

    suspend fun getTriDharmaById(id: String): TriDharma? {
        return triDharmaRepository.getById(id)
    }

    suspend fun createTriDharma(triDharma: TriDharma, files: List<Pair<String, ByteArray>>, role: String?): TriDharma {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang dapat menambah Tri Dharma")
        
        val created = triDharmaRepository.create(triDharma)
        val entityId = created.id ?: throw Exception("Gagal mendapatkan ID Tri Dharma baru")
        
        files.forEach { (fileName, fileBytes) ->
            manageMediaUseCase.uploadMediaBytes(
                entityType = "tri_dharma",
                entityId = entityId,
                originalName = fileName,
                bytes = fileBytes
            )
        }
        
        return triDharmaRepository.getById(entityId) ?: created
    }

    suspend fun updateTriDharma(
        id: String, 
        judulKegiatan: String?, 
        deskripsi: String?, 
        tanggalKegiatan: String?,
        files: List<Pair<String, ByteArray>>, 
        role: String?
    ): TriDharma {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang dapat mengubah Tri Dharma")
        
        val existing = triDharmaRepository.getById(id) ?: throw Exception("Tri Dharma tidak ditemukan")

        val updated = existing.copy(
            judulKegiatan = judulKegiatan ?: existing.judulKegiatan,
            deskripsi = deskripsi ?: existing.deskripsi,
            tanggalKegiatan = tanggalKegiatan ?: existing.tanggalKegiatan
        )

        triDharmaRepository.update(updated)
        
        files.forEach { (fileName, fileBytes) ->
            manageMediaUseCase.uploadMediaBytes(
                entityType = "tri_dharma",
                entityId = id,
                originalName = fileName,
                bytes = fileBytes
            )
        }
        
        return triDharmaRepository.getById(id)!!
    }

    suspend fun deleteTriDharma(id: String, role: String?): Boolean {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang dapat menghapus Tri Dharma")
        
        triDharmaRepository.getById(id) ?: throw Exception("Tri Dharma tidak ditemukan")
        manageMediaUseCase.deleteMediaByEntity("tri_dharma", id)
        
        return triDharmaRepository.delete(id)
    }
}
