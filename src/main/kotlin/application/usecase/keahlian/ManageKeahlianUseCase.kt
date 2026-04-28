package application.usecase.keahlian

import domain.model.Keahlian
import domain.model.DosenKeahlian
import domain.repository.KeahlianRepository
import domain.repository.DosenRepository

class ManageKeahlianUseCase(
    private val keahlianRepository: KeahlianRepository,
    private val dosenRepository: DosenRepository
) {
    // --- Master Data (Admin) ---
    suspend fun getAllKeahlian(): List<Keahlian> = keahlianRepository.getAll()

    suspend fun createKeahlian(nama: String, role: String?): Keahlian {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang bisa menambah master keahlian")
        if (nama.isBlank()) throw Exception("Nama keahlian tidak boleh kosong")
        return keahlianRepository.create(nama)
    }

    suspend fun deleteKeahlian(id: Int, role: String?) {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang bisa menghapus master keahlian")
        keahlianRepository.delete(id)
    }

    // --- Dosen Keahlian (Query) ---
    suspend fun getKeahlianByDosenId(dosenId: String): List<DosenKeahlian> {
        if (dosenId.isBlank()) return emptyList()
        return keahlianRepository.getByDosenId(dosenId)
    }

    suspend fun getKeahlianByDosen(userId: String): List<DosenKeahlian> {
        val dosen = dosenRepository.getByUserId(userId) ?: throw Exception("Data dosen tidak ditemukan")
        return keahlianRepository.getByDosenId(dosen.id!!)
    }

    // --- Dosen Actions ---
    suspend fun addKeahlianToDosen(userId: String, keahlianId: Int) {
        val dosen = dosenRepository.getByUserId(userId) ?: throw Exception("Dosen tidak ditemukan")
        
        // VALIDASI: Cek apakah ID Keahlian ada di master
        val master = keahlianRepository.getAll()
        if (master.none { it.id == keahlianId }) {
            throw Exception("ID Keahlian $keahlianId tidak ditemukan di data master")
        }

        val existing = keahlianRepository.getByDosenId(dosen.id!!)
        if (existing.any { it.keahlian_id == keahlianId }) {
            throw Exception("Keahlian sudah ditambahkan sebelumnya")
        }
        keahlianRepository.addKeahlianToDosen(dosen.id!!, keahlianId)
    }

    // --- Admin Action: Menugaskan keahlian ke Dosen tertentu ---
    suspend fun assignKeahlianToDosen(dosenId: String, keahlianId: Int, role: String?) {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang bisa menugaskan keahlian")
        
        // VALIDASI: Cek apakah ID Keahlian ada di master
        val master = keahlianRepository.getAll()
        if (master.none { it.id == keahlianId }) {
            throw Exception("ID Keahlian $keahlianId tidak ditemukan di data master")
        }

        val existing = keahlianRepository.getByDosenId(dosenId)
        if (existing.any { it.keahlian_id == keahlianId }) {
            throw Exception("Dosen ini sudah memiliki keahlian tersebut")
        }
        keahlianRepository.addKeahlianToDosen(dosenId, keahlianId)
    }

    suspend fun removeKeahlianFromDosen(userId: String, keahlianId: Int) {
        val dosen = dosenRepository.getByUserId(userId) ?: throw Exception("Dosen tidak ditemukan")
        keahlianRepository.removeKeahlianFromDosen(dosen.id!!, keahlianId)
    }
}
