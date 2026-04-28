package presentation.controller

import application.usecase.hki.ManageHKIUseCase
import domain.model.HKI
import infrastructure.security.SecurityUtils
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.jvm.javaio.*
import presentation.dto.ApiResponse
import presentation.dto.response.HKIResponse

class HKIController(private val useCase: ManageHKIUseCase) {

    private fun getUserId(call: ApplicationCall): String = SecurityUtils.getUserId(call) ?: throw Exception("UNAUTHORIZED_USER_ID_MISSING")
    private fun getRole(call: ApplicationCall): String? = SecurityUtils.getRole(call)

    private fun HKI.toResponse() = HKIResponse(
        id = this.id ?: "",
        dosen_id = this.dosenId,
        judul_invensi = this.judulInvensi,
        inventor = this.inventor,
        jenis_hki = this.jenisHki,
        nomor_paten = this.nomorPaten,
        tahun = this.tahun,
        file_url = this.fileUrl,
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

    suspend fun getMyHKI(call: ApplicationCall) {
        try {
            val userId = getUserId(call)
            val result = useCase.getMyHKI(userId)
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
            
            // Cek apakah request beneran multipart
            val contentType = call.request.contentType()
            if (!contentType.match(ContentType.MultiPart.FormData)) {
                throw IllegalArgumentException("Content-Type harus multipart/form-data")
            }

            val multipart = try {
                call.receiveMultipart()
            } catch (e: Exception) {
                println("ERROR RECEIVING MULTIPART: ${e.message}")
                throw Exception("Gagal membaca data form. Pastikan format multipart/form-data benar.")
            }
            
            var judulInvensi = ""
            var inventor: String? = null
            var jenisHki: String? = null
            var nomorPaten: String? = null
            var tahun: Int? = null
            var fileBytes: ByteArray? = null
            var fileName: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        val value = part.value.trim()
                        if (value.isNotEmpty()) {
                            when (part.name) {
                                "judul_invensi" -> judulInvensi = value
                                "inventor" -> inventor = value
                                "jenis_hki" -> jenisHki = value
                                "nomor_paten" -> nomorPaten = value
                                "tahun" -> {
                                    val t = value.toIntOrNull()
                                    if (t == null || t !in 1900..2100) throw IllegalArgumentException("Tahun tidak valid (1900-2100)")
                                    tahun = t
                                }
                            }
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toInputStream().readBytes()
                        if (bytes.isNotEmpty()) {
                            fileBytes = bytes
                            fileName = part.originalFileName
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (judulInvensi.isEmpty()) throw Exception("Judul invensi wajib diisi")

            val hki = HKI(
                dosenId = "", 
                judulInvensi = judulInvensi, 
                inventor = inventor,
                jenisHki = jenisHki,
                nomorPaten = nomorPaten,
                tahun = tahun
            )
            val result = useCase.create(userId, hki, fileBytes, fileName)
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

            var judulInvensi: String? = null
            var inventor: String? = null
            var jenisHki: String? = null
            var nomorPaten: String? = null
            var tahun: Int? = null
            var fileBytes: ByteArray? = null
            var fileName: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "judul_invensi" -> judulInvensi = part.value.trim().takeIf { it.isNotEmpty() }
                            "inventor" -> inventor = part.value.trim().takeIf { it.isNotEmpty() }
                            "jenis_hki" -> jenisHki = part.value.trim().takeIf { it.isNotEmpty() }
                            "nomor_paten" -> nomorPaten = part.value.trim().takeIf { it.isNotEmpty() }
                            "tahun" -> tahun = part.value.trim().toIntOrNull()
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toInputStream().readBytes()
                        if (bytes.isNotEmpty()) {
                            fileBytes = bytes
                            fileName = part.originalFileName
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            val existing = useCase.getById(id) ?: throw Exception("HKI tidak ditemukan")

            val hki = existing.copy(
                judulInvensi = judulInvensi ?: existing.judulInvensi,
                inventor = inventor ?: existing.inventor,
                jenisHki = jenisHki ?: existing.jenisHki,
                nomorPaten = nomorPaten ?: existing.nomorPaten,
                tahun = tahun ?: existing.tahun
            )

            val result = useCase.update(id, userId, role, hki, fileBytes, fileName)
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
            e.message?.contains("UNAUTHORIZED", ignoreCase = true) == true -> HttpStatusCode.Unauthorized
            else -> HttpStatusCode.BadRequest
        }
        call.respond(status, ApiResponse<Unit>(false, e.message ?: "TERJADI_KESALAHAN_INTERNAL"))
    }
}
