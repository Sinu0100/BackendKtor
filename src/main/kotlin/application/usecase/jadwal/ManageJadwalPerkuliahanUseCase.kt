package application.usecase.jadwal

import domain.model.JadwalPerkuliahan
import domain.repository.JadwalPerkuliahanRepository
import application.usecase.media.ManageMediaUseCase

class ManageJadwalPerkuliahanUseCase(
    private val repository: JadwalPerkuliahanRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {

    suspend fun getAllJadwal(): List<JadwalPerkuliahan> {
        return repository.getAll()
    }

    suspend fun createJadwal(
        namaJadwal: String, 
        fileName: String,
        fileBytes: ByteArray, 
        role: String?
    ): JadwalPerkuliahan {
        // STRICT ADMIN ONLY
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang dapat menambah jadwal")
        
        if (namaJadwal.length < 3) throw Exception("Nama jadwal minimal 3 karakter")
        
        // Upload media dan dapatkan URL-nya
        val fileUrl = manageMediaUseCase.uploadFile("jadwal", fileName, fileBytes)
        
        val jadwal = JadwalPerkuliahan(
            namaJadwal = namaJadwal,
            fileUrl = fileUrl
        )
        
        return repository.create(jadwal)
    }

    suspend fun updateJadwal(id: Int, jadwal: JadwalPerkuliahan, role: String?): JadwalPerkuliahan {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang dapat mengubah jadwal")
        return repository.update(id, jadwal)
    }

    suspend fun deleteJadwal(id: Int, role: String?) {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang dapat menghapus jadwal")
        
        val existing = repository.getById(id) ?: throw Exception("Jadwal tidak ditemukan")
        
        // Hapus file dari storage menggunakan URL
        manageMediaUseCase.deleteMediaByUrl(existing.fileUrl)

        val success = repository.delete(id)
        if (!success) throw Exception("Gagal menghapus jadwal")
    }
}
