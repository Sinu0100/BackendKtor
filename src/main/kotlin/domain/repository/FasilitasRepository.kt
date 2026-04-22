package domain.repository

import domain.model.Fasilitas

interface FasilitasRepository {
    suspend fun getAll(): List<Fasilitas>
    suspend fun getById(id: String): Fasilitas?
    suspend fun create(fasilitas: Fasilitas): Fasilitas
    suspend fun update(fasilitas: Fasilitas): Boolean
    suspend fun delete(id: String): Boolean
}
