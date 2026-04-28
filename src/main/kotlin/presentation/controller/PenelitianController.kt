package presentation.controller

import application.usecase.penelitian.ManagePenelitianUseCase
import domain.model.Penelitian
import domain.model.PenelitianAnggota
import infrastructure.security.SecurityUtils
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.jvm.javaio.*
import presentation.dto.ApiResponse
import presentation.dto.response.PenelitianResponse
import presentation.dto.response.MediaResponse

class PenelitianController(private val useCase: ManagePenelitianUseCase) {

    private fun getUserId(call: ApplicationCall): String = SecurityUtils.getUserId(call) ?: throw Exception("Unauthorized")
    private fun getRole(call: ApplicationCall): String? = SecurityUtils.getRole(call)

    private fun Penelitian.toResponse() = PenelitianResponse(
        id = this.id ?: "",
        dosen_id = this.dosenId,
        judul = this.judul,
        tahun = this.tahun,
        deskripsi = this.deskripsi ?: "",
        created_at = this.createdAt,
        media = this.media.map { MediaResponse(it.id ?: "", it.fileUrl) },
        anggota = this.anggota.map { 
            presentation.dto.response.AnggotaResponse(
                dosen_id = it.dosenId,
                nama_dosen = it.namaDosen,
                peran = it.peran
            )
        }
    )

    suspend fun getAll(call: ApplicationCall) {
        try {
            val result = useCase.getAll()
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.map { it.toResponse() }))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun getMyPenelitian(call: ApplicationCall) {
        try {
            val userId = getUserId(call)
            val result = useCase.getMyPenelitian(userId)
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

            var judul = ""
            var tahun: Int? = null
            var deskripsi: String? = null
            val files = mutableListOf<Pair<String, ByteArray>>()
            val anggotaIds = mutableListOf<String>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                                "judul" -> judul = part.value.trim()
                                "tahun" -> {
                                    val t = part.value.trim().toIntOrNull()
                                    if (t == null || t !in 1900..2100) throw IllegalArgumentException("Tahun tidak valid (1900-2100)")
                                    tahun = t
                                }
                                "deskripsi", "deksripsi" -> deskripsi = part.value.trim()
                            "anggota_dosen_id" -> {
                                val value = part.value.trim()
                                if (value.isNotBlank()) {
                                    // Handle many IDs in one line or multiple keys
                                    if (value.contains(",")) {
                                        anggotaIds.addAll(value.split(",").map { it.trim() })
                                    } else {
                                        anggotaIds.add(value)
                                    }
                                }
                            }
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toInputStream().readBytes()
                        if (bytes.size > 0) {
                            files.add((part.originalFileName ?: "file.jpg") to bytes)
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            // Pastikan anggota unik
            val finalAnggotaIds = anggotaIds.distinct()
            println("DEBUG CONTROLLER: Anggota terdeteksi: $finalAnggotaIds")
            
            val penelitian = Penelitian(dosenId = "", judul = judul, tahun = tahun, deskripsi = deskripsi)
            val result = useCase.createWithMedia(userId, penelitian, files, finalAnggotaIds)

            call.respond(HttpStatusCode.Created, ApiResponse(true, "Berhasil membuat penelitian dengan tim", result.toResponse()))
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
            val files = mutableListOf<Pair<String, ByteArray>>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "judul" -> judul = part.value.trim()
                            "tahun" -> tahun = part.value.trim().toIntOrNull()
                            "deskripsi" -> deskripsi = part.value.trim()
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toInputStream().readBytes()
                        if (bytes.size > 0) {
                            files.add((part.originalFileName ?: "file.jpg") to bytes)
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            val result = useCase.updateWithMedia(id, userId, role, judul, tahun, deskripsi, files)
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.toResponse()))
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
            call.respond(HttpStatusCode.OK, ApiResponse<Unit>(true, "Berhasil"))
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
