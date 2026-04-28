package domain.repository

import domain.model.Keahlian
import domain.model.DosenKeahlian

interface KeahlianRepository {
    suspend fun getAll(): List<Keahlian>
    suspend fun create(nama: String): Keahlian
    suspend fun delete(id: Int)
    
    suspend fun getByDosenId(dosenId: String): List<DosenKeahlian>
    suspend fun addKeahlianToDosen(dosenId: String, keahlianId: Int)
    suspend fun removeKeahlianFromDosen(dosenId: String, keahlianId: Int)
}
