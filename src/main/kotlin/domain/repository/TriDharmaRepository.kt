package domain.repository

import domain.model.TriDharma

interface TriDharmaRepository {
    suspend fun getAll(): List<TriDharma>
    suspend fun getById(id: String): TriDharma?
    suspend fun create(triDharma: TriDharma): TriDharma
    suspend fun update(triDharma: TriDharma): Boolean
    suspend fun delete(id: String): Boolean
}
