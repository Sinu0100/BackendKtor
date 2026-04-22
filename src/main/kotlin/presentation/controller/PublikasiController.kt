package presentation.controller

import application.usecase.publikasi.ManagePublikasiUseCase
import domain.model.Publikasi
import infrastructure.security.SecurityUtils
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import presentation.dto.ApiResponse
import presentation.dto.response.PublikasiResponse

class PublikasiController(private val useCase: ManagePublikasiUseCase) {

    private fun getUserId(call: ApplicationCall): String {
        return SecurityUtils.getUserId(call) ?: throw Exception("Unauthorized")
    }

    private fun getRole(call: ApplicationCall): String? {
        return SecurityUtils.getRole(call)
    }

    private fun Publikasi.toResponse() = PublikasiResponse(
        id = this.id ?: "",
        dosen_id = this.dosenId,
        judul = this.judul,
        nama_jurnal_konferensi = this.namaJurnalKonferensi,
        deskripsi = this.deskripsi ?: "",
        link_tautan = this.linkTautan,
        tahun = this.tahun,
        created_at = this.createdAt
    )

    suspend fun getAllPublikasi(call: ApplicationCall) {
        try {
            val result = useCase.getAll()
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.map { it.toResponse() }))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun getById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID diperlukan")
            val result = useCase.getById(id)
            if (result == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "Data tidak ditemukan"))
                return
            }
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun createPublikasi(call: ApplicationCall) {
        try {
            val userId = getUserId(call)
            val params = call.receiveParameters()
            
            val publikasi = Publikasi(
                dosenId = "", 
                judul = params["judul"]?.trim() ?: throw Exception("Judul wajib diisi"),
                namaJurnalKonferensi = params["nama_jurnal_konferensi"]?.trim(),
                deskripsi = params["deskripsi"]?.trim(),
                linkTautan = params["link_tautan"]?.trim(),
                tahun = params["tahun"]?.toIntOrNull()
            )

            val result = useCase.create(userId, publikasi)
            call.respond(HttpStatusCode.Created, ApiResponse(true, "Berhasil ditambahkan", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun updatePublikasi(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID diperlukan")
            val userId = getUserId(call)
            val role = getRole(call)
            val params = call.receiveParameters()

            val existing = useCase.getById(id) ?: throw Exception("Data tidak ditemukan")
            
            val updated = existing.copy(
                judul = params["judul"]?.trim() ?: existing.judul,
                namaJurnalKonferensi = params["nama_jurnal_konferensi"]?.trim() ?: existing.namaJurnalKonferensi,
                deskripsi = params["deskripsi"]?.trim() ?: existing.deskripsi,
                linkTautan = params["link_tautan"]?.trim() ?: existing.linkTautan,
                tahun = params["tahun"]?.toIntOrNull() ?: existing.tahun
            )

            val result = useCase.update(id, userId, role, updated)
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil diperbarui", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun deletePublikasi(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID diperlukan")
            val userId = getUserId(call)
            val role = getRole(call)
            useCase.delete(id, userId, role)
            call.respond(HttpStatusCode.OK, ApiResponse<Unit>(true, "Berhasil dihapus"))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    private suspend fun handleError(call: ApplicationCall, e: Exception) {
        val status = if (e.message?.contains("FORBIDDEN", ignoreCase = true) == true) HttpStatusCode.Forbidden else HttpStatusCode.BadRequest
        call.respond(status, ApiResponse<Unit>(false, e.message ?: "Terjadi kesalahan"))
    }
}
