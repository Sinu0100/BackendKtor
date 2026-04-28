package application.usecase.hki

import domain.model.HKI
import domain.repository.HKIRepository
import domain.repository.DosenRepository
import application.usecase.media.ManageMediaUseCase
import java.util.UUID

class ManageHKIUseCase(
    private val repository: HKIRepository,
    private val dosenRepository: DosenRepository,
    private val manageMediaUseCase: ManageMediaUseCase
) {
    suspend fun getAll(): List<HKI> = repository.getAll()

    suspend fun getById(id: String): HKI? = repository.getById(id)

    suspend fun getMyHKI(userId: String): List<HKI> {
        val dosen = getDosenByUserId(userId)
        // FIX: Menggunakan nama fungsi yang benar sesuai interface HKIRepository
        return repository.getAllByDosenId(dosen.id!!)
    }

    suspend fun create(userId: String, hki: HKI, fileBytes: ByteArray?, fileName: String?): HKI {
        val dosen = getDosenByUserId(userId)
        
        // Normalisasi jenis_hki agar sesuai dengan CHECK constraint database (Misal: 'hak cipta' -> 'Hak Cipta')
        val normalizedJenisHki = hki.jenisHki?.split(" ")?.joinToString(" ") { it.lowercase().replaceFirstChar { char -> char.uppercase() } }

        val id = UUID.randomUUID().toString()
        var finalFileUrl = hki.fileUrl

        if (fileBytes != null && fileName != null) {
            // Kita gunakan tipe 'hki' (pastikan sudah ditambah di database lewat SQL di atas)
            val media = manageMediaUseCase.uploadMediaBytes("hki", id, fileName, fileBytes)
            finalFileUrl = media.fileUrl
        }

        val created = repository.create(hki.copy(
            id = id, 
            dosenId = dosen.id!!, 
            fileUrl = finalFileUrl,
            jenisHki = normalizedJenisHki
        ))
        return repository.getById(created.id!!) ?: created
    }

    suspend fun update(
        id: String, 
        userId: String, 
        role: String?, 
        hki: HKI,
        fileBytes: ByteArray?, 
        fileName: String?
    ): HKI {
        val existing = repository.getById(id) ?: throw Exception("HKI tidak ditemukan")
        
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN: Bukan milik Anda")
        }

        var finalFileUrl = existing.fileUrl
        if (fileBytes != null && fileName != null) {
            // HAPUS file lama dari Storage + DB sebelum upload baru
            manageMediaUseCase.deleteMediaByEntity("hki", id)
            
            val media = manageMediaUseCase.uploadMediaBytes("hki", id, fileName, fileBytes)
            finalFileUrl = media.fileUrl
        }

        val toUpdate = existing.copy(
            judulInvensi = hki.judulInvensi,
            inventor = hki.inventor,
            jenisHki = hki.jenisHki,
            nomorPaten = hki.nomorPaten,
            tahun = hki.tahun,
            fileUrl = finalFileUrl
        )

        repository.update(toUpdate)
        return repository.getById(id) ?: toUpdate
    }

    suspend fun delete(id: String, userId: String, role: String?) {
        val existing = repository.getById(id) ?: throw Exception("HKI tidak ditemukan")
        if (role != "admin") {
            val dosen = getDosenByUserId(userId)
            if (existing.dosenId != dosen.id) throw Exception("FORBIDDEN: Bukan milik Anda")
        }
        
        manageMediaUseCase.deleteMediaByEntity("hki", id)
        repository.delete(id)
    }

    private suspend fun getDosenByUserId(userId: String) =
        dosenRepository.getByUserId(userId) ?: throw Exception("Dosen not found")
}
