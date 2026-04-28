package application.usecase.penelitian

import domain.model.Penelitian
import domain.repository.PenelitianRepository
import domain.repository.DosenRepository
import application.usecase.media.ManageMediaUseCase
import java.util.*

class ManagePenelitianUseCase(
    private val repository: PenelitianRepository,
    private val dosenRepository: DosenRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getAll(): List<Penelitian> = repository.getAll()

    suspend fun getById(id: String): Penelitian? = repository.getById(id)

    suspend fun getMyPenelitian(userId: String): List<Penelitian> {
        val dosen = dosenRepository.getByUserId(userId) ?: throw Exception("Dosen tidak ditemukan")
        return repository.getByDosen(dosen.id!!)
    }

    suspend fun createWithMedia(
        userId: String,
        penelitian: Penelitian,
        files: List<Pair<String, ByteArray>>,
        anggotaDosenIds: List<String>
    ): Penelitian {
        val dosen = dosenRepository.getByUserId(userId) ?: throw Exception("Dosen tidak ditemukan")
        
        // 1. Simpan data utama penelitian
        val created = repository.create(penelitian.copy(dosenId = dosen.id!!))

        // 2. Upload file kalau ada
        files.forEach { (fileName, bytes) ->
            manageMediaUseCase.uploadMediaBytes("penelitian", created.id!!, fileName, bytes)
        }

        // 3. Tambahkan Ketua (Dosen yang login)
        repository.addAnggota(created.id!!, dosen.id!!, "Ketua")

        // 4. Tambahkan Anggota lain
        anggotaDosenIds.forEach { memberId ->
            if (memberId.isNotBlank() && memberId != dosen.id) {
                repository.addAnggota(created.id!!, memberId, "Anggota")
            }
        }

        return repository.getById(created.id!!) ?: created
    }

    suspend fun updateWithMedia(
        id: String,
        userId: String,
        role: String?,
        judul: String?,
        tahun: Int?,
        deskripsi: String?,
        files: List<Pair<String, ByteArray>>
    ): Penelitian {
        val existing = repository.getById(id) ?: throw Exception("Penelitian tidak ditemukan")
        val currentDosen = dosenRepository.getByUserId(userId)
        
        if (role != "admin" && existing.dosenId != currentDosen?.id) {
            throw Exception("Bukan pemilik penelitian")
        }

        val toUpdate = existing.copy(
            judul = judul ?: existing.judul,
            tahun = tahun ?: existing.tahun,
            deskripsi = deskripsi ?: existing.deskripsi
        )
        
        repository.update(toUpdate)

        // Upload file baru kalau ada
        files.forEach { (fileName, bytes) ->
            manageMediaUseCase.uploadMediaBytes("penelitian", id, fileName, bytes)
        }

        return repository.getById(id) ?: toUpdate
    }

    suspend fun delete(id: String, userId: String, role: String?) {
        val existing = repository.getById(id) ?: throw Exception("Penelitian tidak ditemukan")
        val currentDosen = dosenRepository.getByUserId(userId)
        if (role != "admin" && existing.dosenId != currentDosen?.id) {
            throw Exception("Bukan pemilik penelitian")
        }
        
        // Hapus media terkait
        manageMediaUseCase.deleteMediaByEntity("penelitian", id)
        repository.delete(id)
    }
}
