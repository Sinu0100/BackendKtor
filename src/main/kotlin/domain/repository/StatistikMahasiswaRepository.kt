package domain.repository

import domain.model.StatistikMahasiswa

interface StatistikMahasiswaRepository {
    suspend fun getAll(): List<StatistikMahasiswa>
    suspend fun getByTahun(tahun: Int): StatistikMahasiswa?
    suspend fun create(statistik: StatistikMahasiswa): StatistikMahasiswa
    suspend fun update(id: Int, statistik: StatistikMahasiswa): StatistikMahasiswa
    suspend fun delete(id: Int): Boolean
}
