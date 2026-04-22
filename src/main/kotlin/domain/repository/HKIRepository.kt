package domain.repository

import domain.model.HKI

interface HKIRepository {
    suspend fun getAll(): List<HKI>
    suspend fun getAllByDosenId(dosenId: String): List<HKI>
    suspend fun getById(id: String): HKI?
    suspend fun create(hki: HKI): HKI
    suspend fun update(hki: HKI): Boolean
    suspend fun delete(id: String): Boolean
}
