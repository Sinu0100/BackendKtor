package domain.repository

import domain.model.Dosen

interface DosenRepository {
    suspend fun getAll(): List<Dosen>
    suspend fun getById(id: String): Dosen?
    suspend fun getByUserId(userId: String): Dosen?
    suspend fun getByEmail(email: String): Dosen? // Ini nanti nyari ke tabel users
    suspend fun insert(dosen: Dosen): String
    suspend fun update(dosen: Dosen): Boolean
    suspend fun delete(id: String): Boolean
}
