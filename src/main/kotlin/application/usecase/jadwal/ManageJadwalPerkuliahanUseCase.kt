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

    suspend fun updateJadwal(id: Int, jadwal: JadwalPerkuliahan, role: String?, fileBytes: ByteArray? = null, fileName: String? = null): JadwalPerkuliahan {
        if (role != "admin") throw Exception("FORBIDDEN: Hanya Admin yang dapat mengubah jadwal")
        
        val existing = repository.getById(id) ?: throw Exception("Jadwal tidak ditemukan")
        
        var finalFileUrl = existing.fileUrl
        if (fileBytes != null && fileName != null) {
            // HAPUS file lama dari storage
            try {
                manageMediaUseCase.deleteMediaByUrl(existing.fileUrl)
            } catch (e: Exception) {
                println("WARNING: Gagal hapus file jadwal lama: ${e.message}")
            }
            // Upload file baru
            finalFileUrl = manageMediaUseCase.uploadFile("jadwal", fileName, fileBytes)
        }
        
        val toUpdate = JadwalPerkuliahan(
            namaJadwal = if (jadwal.namaJadwal.isNotEmpty()) jadwal.namaJadwal else existing.namaJadwal,
            fileUrl = finalFileUrl
        )
        
        return repository.update(id, toUpdate)
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
