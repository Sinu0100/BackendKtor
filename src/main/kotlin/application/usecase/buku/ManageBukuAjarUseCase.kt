package application.usecase.buku

import domain.model.BukuAjar
import domain.repository.BukuAjarRepository
import domain.repository.DosenRepository
import application.usecase.media.ManageMediaUseCase

class ManageBukuAjarUseCase(
    private val repository: BukuAjarRepository,
    private val dosenRepository: DosenRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getAll(): List<BukuAjar> = repository.getAll()

    suspend fun getById(id: String): BukuAjar? = repository.getById(id)

    suspend fun getMyBuku(userId: String): List<BukuAjar> {
        val dosen = getDosenByUserId(userId)
        return repository.getByDosen(dosen.id!!)
    }

    private fun normalizePeran(peran: String): String {
        val p = peran.lowercase().trim()
        return when {
            p.contains("ketua") -> "Penulis Ketua"
            p.contains("anggota") -> "Anggota"
            else -> "Anggota" // Default aman
        }
    }

    suspend fun create(userId: String, buku: BukuAjar): BukuAjar {
        val dosen = getDosenByUserId(userId)
        
        val created = repository.create(buku.copy(
            dosenId = dosen.id!!,
            peranPenulis = normalizePeran(buku.peranPenulis)
        ))
        return repository.getById(created.id!!) ?: created
    }

    suspend fun update(id: String, userId: String, role: String?, buku: BukuAjar): BukuAjar {
        val existing = repository.getById(id) ?: throw Exception("Buku tidak ditemukan")
        
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN: Bukan milik Anda")
        }

        val toUpdate = existing.copy(
            judul = buku.judul,
            tahun = buku.tahun,
            deskripsi = buku.deskripsi,
            peranPenulis = normalizePeran(buku.peranPenulis)
        )
        
        repository.update(toUpdate)
        return repository.getById(id) ?: toUpdate
    }

    suspend fun delete(id: String, userId: String, role: String?) {
        val existing = repository.getById(id) ?: throw Exception("Buku tidak ditemukan")
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN: Bukan milik Anda")
        }
        repository.delete(id)
    }

    private suspend fun getDosenByUserId(userId: String) =
        dosenRepository.getByUserId(userId) ?: throw Exception("Dosen not found")
}
