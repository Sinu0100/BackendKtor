package domain.repository

import domain.model.Publikasi

interface PublikasiRepository {
    suspend fun getAll(): List<Publikasi>
    suspend fun getById(id: String): Publikasi?
    suspend fun getByDosen(dosenId: String): List<Publikasi>
    suspend fun create(publikasi: Publikasi): Publikasi
    suspend fun update(publikasi: Publikasi): Boolean
    suspend fun delete(id: String): Boolean
}
