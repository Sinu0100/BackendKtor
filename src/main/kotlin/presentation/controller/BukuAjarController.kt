package presentation.controller

import application.usecase.buku.ManageBukuAjarUseCase
import domain.model.BukuAjar
import infrastructure.security.SecurityUtils
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import presentation.dto.ApiResponse
import presentation.dto.response.BukuAjarResponse

class BukuAjarController(private val useCase: ManageBukuAjarUseCase) {

    private fun getUserId(call: ApplicationCall): String = SecurityUtils.getUserId(call) ?: throw Exception("Unauthorized")
    private fun getRole(call: ApplicationCall): String? = SecurityUtils.getRole(call)

    private fun BukuAjar.toResponse() = BukuAjarResponse(
        id = this.id ?: "",
        dosen_id = this.dosenId,
        judul = this.judul,
        tahun = this.tahun,
        deskripsi = this.deskripsi ?: "",
        peran_penulis = this.peranPenulis,
        created_at = this.createdAt
    )

    suspend fun getAll(call: ApplicationCall) {
        try {
            val result = useCase.getAll()
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.map { it.toResponse() }))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun getMyBuku(call: ApplicationCall) {
        try {
            val userId = getUserId(call)
            val result = useCase.getMyBuku(userId)
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.map { it.toResponse() }))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun getById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID diperlukan")
            val result = useCase.getById(id) ?: return call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "Data tidak ditemukan"))
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun create(call: ApplicationCall) {
        try {
            val userId = getUserId(call)
            val multipart = call.receiveMultipart()

            var judul = ""
            var tahun: Int? = null
            var deskripsi: String? = null
            var peran = "Anggota"

            multipart.forEachPart { part ->
                if (part is PartData.FormItem) {
                    when (part.name) {
                        "judul" -> judul = part.value.trim()
                        "tahun" -> {
                            val t = part.value.trim().toIntOrNull()
                            if (t == null || t !in 1900..2100) throw IllegalArgumentException("Tahun tidak valid (1900-2100)")
                            tahun = t
                        }
                        "deskripsi" -> deskripsi = part.value.trim()
                        "peran_penulis" -> {
                            val v = part.value.trim()
                            if (v.isNotEmpty()) peran = v
                        }
                    }
                }
                part.dispose()
            }

            if (judul.isEmpty()) throw Exception("Judul wajib diisi")
            
            val buku = BukuAjar(
                dosenId = "", 
                judul = judul, 
                tahun = tahun, 
                deskripsi = deskripsi, 
                peranPenulis = peran
            )
            val result = useCase.create(userId, buku)
            call.respond(HttpStatusCode.Created, ApiResponse(true, "Berhasil", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun update(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID diperlukan")
            val userId = getUserId(call)
            val role = getRole(call)
            val multipart = call.receiveMultipart()

            var judul: String? = null
            var tahun: Int? = null
            var deskripsi: String? = null
            var peran: String? = null

            multipart.forEachPart { part ->
                if (part is PartData.FormItem) {
                    when (part.name) {
                        "judul" -> judul = part.value.trim().takeIf { it.isNotEmpty() }
                        "tahun" -> tahun = part.value.trim().toIntOrNull()
                        "deskripsi" -> deskripsi = part.value.trim().takeIf { it.isNotEmpty() }
                        "peran_penulis" -> peran = part.value.trim().takeIf { it.isNotEmpty() }
                    }
                }
                part.dispose()
            }

            val existing = useCase.getById(id) ?: throw Exception("Data tidak ditemukan")
            val toUpdate = existing.copy(
                judul = judul ?: existing.judul,
                tahun = tahun ?: existing.tahun,
                deskripsi = deskripsi ?: existing.deskripsi,
                peranPenulis = peran ?: existing.peranPenulis
            )

            val result = useCase.update(id, userId, role, toUpdate)
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil diperbarui", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun delete(call: ApplicationCall) {
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
        val status = when {
            e is IllegalArgumentException -> HttpStatusCode.BadRequest
            e.message?.contains("FORBIDDEN", ignoreCase = true) == true -> HttpStatusCode.Forbidden
            e.message?.contains("Unauthorized", ignoreCase = true) == true -> HttpStatusCode.Unauthorized
            else -> HttpStatusCode.BadRequest
        }
        call.respond(status, ApiResponse<Unit>(false, e.message ?: "Terjadi kesalahan"))
    }
}
