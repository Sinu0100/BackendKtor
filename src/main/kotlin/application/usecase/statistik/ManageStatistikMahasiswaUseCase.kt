package application.usecase.statistik

import domain.model.StatistikMahasiswa
import domain.repository.StatistikMahasiswaRepository

class ManageStatistikMahasiswaUseCase(private val repository: StatistikMahasiswaRepository) {

    private fun verifyAdmin(role: String?) {
        if (role != "admin") {
            throw Exception("FORBIDDEN: Anda tidak memiliki akses untuk melakukan aksi ini")
        }
    }

    private fun validateStatistik(statistik: StatistikMahasiswa) {
        if (statistik.tahun !in 2000..2100) throw Exception("Tahun harus antara 2000 dan 2100")
        if (statistik.jumlahPendaftar < 0) throw Exception("Jumlah pendaftar tidak boleh negatif")
        if (statistik.jumlahDiterima < 0) throw Exception("Jumlah diterima tidak boleh negatif")
        if (statistik.jumlahLulusan < 0) throw Exception("Jumlah lulusan tidak boleh negatif")
        if (statistik.jumlahDiterima > statistik.jumlahPendaftar) {
            throw Exception("Jumlah diterima tidak boleh melebihi jumlah pendaftar")
        }
    }

    suspend fun getAllStatistik(): List<StatistikMahasiswa> {
        return repository.getAll()
    }

    suspend fun createStatistik(statistik: StatistikMahasiswa, role: String?): StatistikMahasiswa {
        verifyAdmin(role)
        validateStatistik(statistik)
        
        repository.getByTahun(statistik.tahun)?.let {
            throw Exception("Data statistik tahun ${statistik.tahun} sudah ada")
        }
        
        return repository.create(statistik)
    }

    suspend fun updateStatistik(id: Int, statistik: StatistikMahasiswa, role: String?): StatistikMahasiswa {
        verifyAdmin(role)
        validateStatistik(statistik)
        return repository.update(id, statistik)
    }

    suspend fun deleteStatistik(id: Int, role: String?): Boolean {
        verifyAdmin(role)
        return repository.delete(id)
    }
}
