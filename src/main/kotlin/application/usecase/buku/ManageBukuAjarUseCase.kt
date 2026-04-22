package application.usecase.buku

import domain.model.BukuAjar
import domain.repository.BukuAjarRepository
import domain.repository.DosenRepository
import application.usecase.media.ManageMediaUseCase

class ManageBukuAjarUseCase(
    private val bukuRepository: BukuAjarRepository,
    private val dosenRepository: DosenRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getAll(): List<BukuAjar> = bukuRepository.getAll()

    suspend fun getById(id: String): BukuAjar? = bukuRepository.getById(id)

    suspend fun getMyBuku(userId: String): List<BukuAjar> {
        val dosen = dosenRepository.getByUserId(userId) ?: throw Exception("Profil Dosen tidak ditemukan")
        return bukuRepository.getByDosen(dosen.id!!)
    }

    suspend fun create(userId: String, buku: BukuAjar): BukuAjar {
        val dosen = dosenRepository.getByUserId(userId) ?: throw Exception("Profil Dosen tidak ditemukan")
        return bukuRepository.create(buku.copy(dosenId = dosen.id!!))
    }

    suspend fun update(id: String, userId: String, role: String?, judul: String?, tahun: Int?, deskripsi: String?, peran: String?): BukuAjar {
        val existing = bukuRepository.getById(id) ?: throw Exception("Data Buku Ajar tidak ditemukan")
        
        // Cek ownership jika bukan admin
        if (role != "admin") {
            val dosen = dosenRepository.getByUserId(userId) ?: throw Exception("Profil Dosen tidak ditemukan")
            if (existing.dosenId != dosen.id) {
                throw Exception("FORBIDDEN: Anda bukan pemilik data ini")
            }
        }

        val updated = existing.copy(
            judul = judul ?: existing.judul,
            tahun = tahun ?: existing.tahun,
            deskripsi = deskripsi ?: existing.deskripsi,
            peranPenulis = peran ?: existing.peranPenulis
        )

        val success = bukuRepository.update(updated)
        if (!success) throw Exception("Gagal memperbarui data buku ajar")
        return updated
    }

    suspend fun delete(id: String, userId: String, role: String?) {
        val existing = bukuRepository.getById(id) ?: throw Exception("Data Buku Ajar tidak ditemukan")
        
        if (role != "admin") {
            val dosen = dosenRepository.getByUserId(userId) ?: throw Exception("Profil Dosen tidak ditemukan")
            if (existing.dosenId != dosen.id) {
                throw Exception("FORBIDDEN: Anda bukan pemilik data ini")
            }
        }

        bukuRepository.delete(id)
    }
}
