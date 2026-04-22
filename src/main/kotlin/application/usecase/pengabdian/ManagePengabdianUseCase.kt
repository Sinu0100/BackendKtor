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
    suspend fun getAll(): List<Pengabdian> {
        return repository.getAll().map { 
            it.copy(media = manageMediaUseCase.getMediaByEntity("pengabdian", it.id!!))
        }
    }

    suspend fun getById(id: String): Pengabdian? {
        val data = repository.getById(id) ?: return null
        return data.copy(media = manageMediaUseCase.getMediaByEntity("pengabdian", id))
    }

    suspend fun createWithMedia(userId: String, pengabdian: Pengabdian, files: List<Pair<String, ByteArray>>): Pengabdian {
        val dosen = getDosenByUserId(userId)
        val created = repository.create(pengabdian.copy(dosenId = dosen.id!!))
        
        files.forEach { (name, bytes) ->
            manageMediaUseCase.uploadMediaBytes("pengabdian", created.id!!, name, bytes)
        }
        
        return getById(created.id!!)!!
    }

    suspend fun updateWithMedia(
        id: String, userId: String, role: String?, 
        judul: String?, deskripsi: String?, tahun: Int?, 
        files: List<Pair<String, ByteArray>>
    ): Pengabdian {
        val existing = repository.getById(id) ?: throw Exception("Not Found")
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN")
        }

        val updated = existing.copy(
            judul = judul ?: existing.judul,
            deskripsi = deskripsi ?: existing.deskripsi,
            tahun = tahun ?: existing.tahun
        )
        repository.update(updated)

        files.forEach { (name, bytes) ->
            manageMediaUseCase.uploadMediaBytes("pengabdian", id, name, bytes)
        }

        return getById(id)!!
    }

    suspend fun delete(id: String, userId: String, role: String?) {
        val existing = repository.getById(id) ?: throw Exception("Not Found")
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN")
        }
        manageMediaUseCase.deleteMediaByEntity("pengabdian", id)
        repository.delete(id)
    }

    private suspend fun getDosenByUserId(userId: String) =
        dosenRepository.getByUserId(userId) ?: throw Exception("Dosen not found")
}
