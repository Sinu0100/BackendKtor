package infrastructure.storage

import infrastructure.config.SupabaseConfig
import io.github.jan.supabase.storage.storage
import java.util.*

class StorageService {
    private val client = SupabaseConfig.getClient()
    private val bucketName = "prodi-files"

    /**
     * Upload file ke Supabase Storage
     * @param folder folder tujuan (misal: "fasilitas", "jadwal")
     * @param fileName nama file asli
     * @param fileBytes isi file dalam bentuk byte array
     * @return URL publik file yang diupload
     */
    suspend fun uploadFile(folder: String, fileName: String, fileBytes: ByteArray): String {
        val extension = fileName.substringAfterLast(".", "")
        val uniqueFileName = "${UUID.randomUUID()}.$extension"
        val path = "$folder/$uniqueFileName"

        val bucket = client.storage[bucketName]
        bucket.upload(path, fileBytes) {
            upsert = true
        }

        return bucket.publicUrl(path)
    }

    suspend fun deleteFile(path: String) {
        val bucket = client.storage[bucketName]
        bucket.delete(path)
    }
}
