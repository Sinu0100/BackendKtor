package domain.repository

import domain.model.Penelitian

interface PenelitianRepository {
    suspend fun getAll(): List<Penelitian>
    suspend fun getById(id: String): Penelitian?
    suspend fun getByDosen(dosenId: String): List<Penelitian>
    suspend fun create(penelitian: Penelitian): Penelitian
    suspend fun update(penelitian: Penelitian): Boolean
    suspend fun delete(id: String): Boolean
    
    // Anggota management
    suspend fun addAnggota(penelitianId: String, dosenId: String, peran: String): Boolean
    suspend fun removeAnggota(penelitianId: String, dosenId: String): Boolean
}
