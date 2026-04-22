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
    suspend fun getMediaByEntity(entityType: String, entityId: String): List<Media> {
        return mediaRepository.getByEntity(entityType, entityId)
    }

    suspend fun uploadMedia(entityType: String, entityId: String, filePart: PartData.FileItem): Media {
        val bytes = filePart.provider().readRemaining().readByteArray()
        return uploadMediaBytes(entityType, entityId, filePart.originalFileName ?: "file", bytes)
    }

    suspend fun uploadMediaBytes(entityType: String, entityId: String, originalName: String, bytes: ByteArray): Media {
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
