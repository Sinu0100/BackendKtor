package application.usecase.penelitian

import domain.model.Penelitian
import domain.repository.PenelitianRepository
import domain.repository.DosenRepository
import application.usecase.media.ManageMediaUseCase

class ManagePenelitianUseCase(
    private val repository: PenelitianRepository,
    private val dosenRepository: DosenRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getAll(): List<Penelitian> {
        return repository.getAll().map { 
            it.copy(media = manageMediaUseCase.getMediaByEntity("penelitian", it.id!!))
        }
    }

    suspend fun getById(id: String): Penelitian? {
        val research = repository.getById(id) ?: return null
        return research.copy(media = manageMediaUseCase.getMediaByEntity("penelitian", id))
    }

    suspend fun createWithMedia(userId: String, penelitian: Penelitian, files: List<Pair<String, ByteArray>>): Penelitian {
        val dosen = getDosenByUserId(userId)
        val created = repository.create(penelitian.copy(dosenId = dosen.id!!))
        
        files.forEach { (name, bytes) ->
            manageMediaUseCase.uploadMediaBytes("penelitian", created.id!!, name, bytes)
        }
        
        return getById(created.id!!)!!
    }

    suspend fun updateWithMedia(
        id: String, userId: String, role: String?, 
        judul: String?, tahun: Int?, deskripsi: String?, 
        files: List<Pair<String, ByteArray>>
    ): Penelitian {
        val existing = repository.getById(id) ?: throw Exception("Not Found")
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN")
        }

        val updated = existing.copy(
            judul = judul ?: existing.judul,
            tahun = tahun ?: existing.tahun,
            deskripsi = deskripsi ?: existing.deskripsi
        )
        repository.update(updated)

        files.forEach { (name, bytes) ->
            manageMediaUseCase.uploadMediaBytes("penelitian", id, name, bytes)
        }

        return getById(id)!!
    }

    suspend fun delete(id: String, userId: String, role: String?) {
        val existing = repository.getById(id) ?: throw Exception("Not Found")
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN")
        }
        manageMediaUseCase.deleteMediaByEntity("penelitian", id)
        repository.delete(id)
    }

    private suspend fun getDosenByUserId(userId: String) =
        dosenRepository.getByUserId(userId) ?: throw Exception("Dosen not found")
}
