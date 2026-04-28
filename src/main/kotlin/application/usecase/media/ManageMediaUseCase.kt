package application.usecase.media

import domain.model.Media
import domain.repository.MediaRepository
import infrastructure.storage.StorageService
import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray

class ManageMediaUseCase(
    private val mediaRepository: MediaRepository,
    private val storageService: StorageService
) {
    suspend fun getById(id: String): Media? {
        return mediaRepository.getById(id)
    }

    suspend fun getMediaByEntity(entityType: String, entityId: String): List<Media> {
        return mediaRepository.getByEntity(entityType, entityId)
    }

    suspend fun uploadMedia(entityType: String, entityId: String, filePart: PartData.FileItem): Media {
        val bytes = filePart.provider().readRemaining().readByteArray()
        return uploadMediaBytes(entityType, entityId, filePart.originalFileName ?: "file", bytes)
    }

    suspend fun uploadMediaBytes(entityType: String, entityId: String, originalName: String, bytes: ByteArray): Media {
        // Validasi entityType agar sesuai dengan CHECK constraint di database
        val allowedTypes = listOf("penelitian", "pengabdian", "fasilitas", "tri_dharma", "hki", "publikasi", "buku_ajar", "sertifikat")
        if (entityType.lowercase() !in allowedTypes) {
            throw IllegalArgumentException("Entity type '$entityType' tidak didukung oleh sistem media.")
        }

        val url = storageService.uploadFile(entityType, originalName, bytes)

        val media = Media(
            entityType = entityType,
            entityId = entityId,
            fileUrl = url,
            tipeFile = "image"
        )
        return mediaRepository.create(media)
    }

    suspend fun uploadFile(entityType: String, originalName: String, bytes: ByteArray): String {
        return storageService.uploadFile(entityType, originalName, bytes)
    }

    /**
     * Hapus satu media berdasarkan ID.
     * Menghapus file dari Supabase Storage DAN record dari database.
     * @return Media yang dihapus, atau null jika tidak ditemukan
     */
    suspend fun deleteSingleMedia(mediaId: String): Media? {
        val media = mediaRepository.getById(mediaId) ?: return null
        
        // Hapus file dari storage
        try {
            storageService.deleteFile(media.fileUrl)
        } catch (e: Exception) {
            println("WARNING: Gagal hapus file dari storage: ${e.message}")
            // Lanjut hapus record DB meskipun storage gagal
        }
        
        // Hapus record dari database
        mediaRepository.delete(mediaId)
        return media
    }

    suspend fun deleteMediaByEntity(entityType: String, entityId: String) {
        // Hapus file dari storage satu per satu jika storage service mendukung hapus by URL
        val mediaList = mediaRepository.getByEntity(entityType, entityId)
        mediaList.forEach { media ->
            storageService.deleteFile(media.fileUrl)
        }
        mediaRepository.deleteByEntity(entityType, entityId)
    }

    suspend fun deleteMediaByUrl(url: String) {
        storageService.deleteFile(url)
        // Opsional: hapus record dari database media jika URL-nya ada di sana
    }
}

