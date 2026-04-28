package presentation.controller

import application.usecase.pengabdian.ManagePengabdianUseCase
import domain.model.Pengabdian
import infrastructure.security.SecurityUtils
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.jvm.javaio.*
import presentation.dto.ApiResponse
import presentation.dto.response.MediaResponse
import presentation.dto.response.PengabdianResponse

class PengabdianController(private val useCase: ManagePengabdianUseCase) {

    private fun getUserId(call: ApplicationCall): String = SecurityUtils.getUserId(call) ?: throw Exception("Unauthorized")
    private fun getRole(call: ApplicationCall): String? = SecurityUtils.getRole(call)

    private fun Pengabdian.toResponse() = PengabdianResponse(
        id = this.id ?: "",
        dosen_id = this.dosenId,
        judul_pengabdian = this.judulPengabdian,
        deskripsi = this.deskripsi ?: "",
        tahun = this.tahun,
        created_at = this.createdAt,
        media = this.media.map { MediaResponse(it.id ?: "", it.fileUrl) }
    )

    suspend fun getAll(call: ApplicationCall) {
        try {
            val result = useCase.getAll()
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.map { it.toResponse() }))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun getMyPengabdian(call: ApplicationCall) {
        try {
            val userId = getUserId(call)
            val result = useCase.getMyPengabdian(userId)
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

            var judulPengabdian = ""
            var deskripsi: String? = null
            var tahun: Int? = null
            val files = mutableListOf<Pair<String, ByteArray>>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "judul_pengabdian" -> judulPengabdian = part.value.trim()
                            "deskripsi" -> deskripsi = part.value.trim()
                            "tahun" -> {
                                val t = part.value.trim().toIntOrNull()
                                if (t == null || t !in 1900..2100) throw IllegalArgumentException("Tahun tidak valid (1900-2100)")
                                tahun = t
                            }
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toInputStream().readBytes()
                        if (bytes.isNotEmpty()) {
                            files.add((part.originalFileName ?: "file.pdf") to bytes)
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (judulPengabdian.isEmpty()) throw Exception("Judul pengabdian wajib diisi")

            val pengabdian = Pengabdian(dosenId = "", judulPengabdian = judulPengabdian, deskripsi = deskripsi, tahun = tahun)
            val result = useCase.createWithMedia(userId, pengabdian, files)
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

            var judulPengabdian: String? = null
            var deskripsi: String? = null
            var tahun: Int? = null
            val files = mutableListOf<Pair<String, ByteArray>>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "judul_pengabdian" -> judulPengabdian = part.value.trim().takeIf { it.isNotEmpty() }
                            "deskripsi" -> deskripsi = part.value.trim().takeIf { it.isNotEmpty() }
                            "tahun" -> tahun = part.value.trim().toIntOrNull()
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toInputStream().readBytes()
                        if (bytes.isNotEmpty()) {
                            files.add((part.originalFileName ?: "file.pdf") to bytes)
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            val existing = useCase.getById(id) ?: throw Exception("Data tidak ditemukan")
            val toUpdate = existing.copy(
                judulPengabdian = judulPengabdian ?: existing.judulPengabdian,
                deskripsi = deskripsi ?: existing.deskripsi,
                tahun = tahun ?: existing.tahun
            )

            val result = useCase.updateWithMedia(id, userId, role, toUpdate, files)
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
