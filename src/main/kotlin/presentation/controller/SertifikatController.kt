package presentation.controller

import application.usecase.sertifikat.ManageSertifikatUseCase
import domain.model.Sertifikat
import infrastructure.security.SecurityUtils
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.jvm.javaio.*
import presentation.dto.ApiResponse
import presentation.dto.response.SertifikatResponse

class SertifikatController(private val useCase: ManageSertifikatUseCase) {

    private fun getUserId(call: ApplicationCall): String = SecurityUtils.getUserId(call) ?: throw Exception("Unauthorized")
    private fun getRole(call: ApplicationCall): String? = SecurityUtils.getRole(call)

    private fun Sertifikat.toResponse() = SertifikatResponse(
        id = this.id ?: "",
        dosen_id = this.dosenId,
        judul_sertifikat = this.judulSertifikat,
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

    suspend fun getMySertifikat(call: ApplicationCall) {
        try {
            val userId = getUserId(call)
            val result = useCase.getMySertifikat(userId)
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.map { it.toResponse() }))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun getById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID diperlukan")
            val result = useCase.getById(id) ?: return call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "Not Found"))
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun create(call: ApplicationCall) {
        try {
            val userId = getUserId(call)
            val multipart = call.receiveMultipart()

            var judulSertifikat = ""
            var tahun: Int? = null
            var fileBytes: ByteArray? = null
            var fileName: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        val value = part.value.trim()
                        if (value.isNotEmpty()) {
                            when (part.name) {
                                "judul_sertifikat" -> judulSertifikat = value
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

            if (judulSertifikat.isEmpty()) throw Exception("Judul sertifikat wajib diisi")

            val sertifikat = Sertifikat(dosenId = "", judulSertifikat = judulSertifikat, tahun = tahun)
            val result = useCase.create(userId, sertifikat, fileBytes, fileName)
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

            var judulSertifikat: String? = null
            var tahun: Int? = null
            var fileBytes: ByteArray? = null
            var fileName: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "judul_sertifikat" -> judulSertifikat = part.value.trim().takeIf { it.isNotEmpty() }
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

            val existing = useCase.getById(id) ?: throw Exception("Data tidak ditemukan")
            val toUpdate = existing.copy(
                judulSertifikat = judulSertifikat ?: existing.judulSertifikat,
                tahun = tahun ?: existing.tahun
            )

            val result = useCase.update(id, userId, role, toUpdate, fileBytes, fileName)
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
