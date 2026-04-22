package domain.repository

import domain.model.BukuAjar

interface BukuAjarRepository {
    suspend fun getAll(): List<BukuAjar>
    suspend fun getById(id: String): BukuAjar?
    suspend fun getByDosen(dosenId: String): List<BukuAjar>
    suspend fun create(buku: BukuAjar): BukuAjar
    suspend fun update(buku: BukuAjar): Boolean
    suspend fun delete(id: String): Boolean
}
