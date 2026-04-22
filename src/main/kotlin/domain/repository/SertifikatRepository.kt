package domain.repository

import domain.model.Sertifikat

interface SertifikatRepository {
    suspend fun getAll(): List<Sertifikat>
    suspend fun getById(id: String): Sertifikat?
    suspend fun getByDosen(dosenId: String): List<Sertifikat>
    suspend fun create(sertifikat: Sertifikat): Sertifikat
    suspend fun update(sertifikat: Sertifikat): Boolean
    suspend fun delete(id: String): Boolean
}
