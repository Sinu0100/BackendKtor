package application.usecase.fasilitas

import domain.model.Fasilitas
import domain.repository.FasilitasRepository
import application.usecase.media.ManageMediaUseCase

class ManageFasilitasUseCase(
    private val fasilitasRepository: FasilitasRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getAllFasilitas(): List<Fasilitas> {
        return fasilitasRepository.getAll()
    }

    suspend fun getFasilitasById(id: String): Fasilitas? {
        return fasilitasRepository.getById(id)
    }

    suspend fun createFasilitas(fasilitas: Fasilitas, files: List<Pair<String, ByteArray>>, role: String?): Fasilitas {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang dapat menambah fasilitas")
        
        val created = fasilitasRepository.create(fasilitas)
        val entityId = created.id ?: throw Exception("Gagal mendapatkan ID fasilitas baru")
        
        files.forEach { (fileName, fileBytes) ->
            manageMediaUseCase.uploadMediaBytes(
                entityType = "fasilitas",
                entityId = entityId,
                originalName = fileName,
                bytes = fileBytes
            )
        }
        
        return fasilitasRepository.getById(entityId) ?: created
    }

    suspend fun updateFasilitas(
        id: String, 
        judulFasilitas: String?, 
        deskripsi: String?, 
        files: List<Pair<String, ByteArray>>, 
        role: String?
    ): Fasilitas {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang dapat mengubah fasilitas")
        
        val existing = fasilitasRepository.getById(id) ?: throw Exception("Fasilitas tidak ditemukan")

        val updatedFasilitas = existing.copy(
            judulFasilitas = judulFasilitas ?: existing.judulFasilitas,
            deskripsi = deskripsi ?: existing.deskripsi
        )

        fasilitasRepository.update(updatedFasilitas)
        
        files.forEach { (fileName, fileBytes) ->
            manageMediaUseCase.uploadMediaBytes(
                entityType = "fasilitas",
                entityId = id,
                originalName = fileName,
                bytes = fileBytes
            )
        }
        
        return fasilitasRepository.getById(id)!!
    }

    suspend fun deleteFasilitas(id: String, role: String?): Boolean {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang dapat menghapus fasilitas")
        
        fasilitasRepository.getById(id) ?: throw Exception("Fasilitas tidak ditemukan")
        manageMediaUseCase.deleteMediaByEntity("fasilitas", id)
        
        return fasilitasRepository.delete(id)
    }
}
