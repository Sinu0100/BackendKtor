package presentation.controller

import application.usecase.statistik.ManageStatistikMahasiswaUseCase
import domain.model.StatistikMahasiswa
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import presentation.dto.ApiResponse
import presentation.dto.request.StatistikMahasiswaRequest
import presentation.dto.response.StatistikMahasiswaResponse

class StatistikController(private val manageStatistikUseCase: ManageStatistikMahasiswaUseCase) {

    private fun getRole(call: ApplicationCall): String? {
        val principal = call.principal<JWTPrincipal>()
        return principal?.payload?.getClaim("role")?.asString()
    }

    private fun StatistikMahasiswa.toResponse() = StatistikMahasiswaResponse(
        id = this.id ?: 0,
        tahun = this.tahun,
        jumlah_pendaftar = this.jumlahPendaftar,
        jumlah_diterima = this.jumlahDiterima,
        jumlah_lulusan = this.jumlahLulusan
    )

    suspend fun getAllStatistik(call: ApplicationCall) {
        val result = manageStatistikUseCase.getAllStatistik()
        val response = result.map { it.toResponse() }
        call.respond(HttpStatusCode.OK, ApiResponse(true, "Data statistik berhasil diambil", response))
    }

    suspend fun createStatistik(call: ApplicationCall) {
        val request = call.receive<StatistikMahasiswaRequest>()
        val role = getRole(call)
        
        val statistik = StatistikMahasiswa(
            tahun = request.tahun,
            jumlahPendaftar = request.jumlah_pendaftar,
            jumlahDiterima = request.jumlah_diterima,
            jumlahLulusan = request.jumlah_lulusan
        )
        
        val result = manageStatistikUseCase.createStatistik(statistik, role)
        call.respond(HttpStatusCode.Created, ApiResponse(true, "Statistik berhasil ditambahkan", result.toResponse()))
    }

    suspend fun updateStatistik(call: ApplicationCall) {
        val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("ID statistik diperlukan")
        val request = call.receive<StatistikMahasiswaRequest>()
        val role = getRole(call)
        
        val statistik = StatistikMahasiswa(
            tahun = request.tahun,
            jumlahPendaftar = request.jumlah_pendaftar,
            jumlahDiterima = request.jumlah_diterima,
            jumlahLulusan = request.jumlah_lulusan
        )
        
        val result = manageStatistikUseCase.updateStatistik(id, statistik, role)
        call.respond(HttpStatusCode.OK, ApiResponse(true, "Statistik berhasil diperbarui", result.toResponse()))
    }

    suspend fun deleteStatistik(call: ApplicationCall) {
        val id = call.parameters["id"]?.toIntOrNull() ?: throw IllegalArgumentException("ID statistik diperlukan")
        val role = getRole(call)
        
        manageStatistikUseCase.deleteStatistik(id, role)
        call.respond(HttpStatusCode.OK, ApiResponse<Unit>(true, "Statistik berhasil dihapus"))
    }
}
