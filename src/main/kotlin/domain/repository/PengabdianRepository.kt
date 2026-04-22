package domain.repository

import domain.model.Pengabdian

interface PengabdianRepository {
    suspend fun getAll(): List<Pengabdian>
    suspend fun getById(id: String): Pengabdian?
    suspend fun getByDosen(dosenId: String): List<Pengabdian>
    suspend fun create(pengabdian: Pengabdian): Pengabdian
    suspend fun update(pengabdian: Pengabdian): Boolean
    suspend fun delete(id: String): Boolean
}
