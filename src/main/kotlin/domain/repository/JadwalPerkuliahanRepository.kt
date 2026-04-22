package domain.repository

import domain.model.JadwalPerkuliahan

interface JadwalPerkuliahanRepository {
    suspend fun getAll(): List<JadwalPerkuliahan>
    suspend fun getById(id: Int): JadwalPerkuliahan?
    suspend fun create(jadwal: JadwalPerkuliahan): JadwalPerkuliahan
    suspend fun update(id: Int, jadwal: JadwalPerkuliahan): JadwalPerkuliahan
    suspend fun delete(id: Int): Boolean
}
