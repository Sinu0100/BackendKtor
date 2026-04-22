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
        nama_sertifikat = this.namaSertifikat,
        penerbit = this.penerbit ?: "",
        tahun = this.tahun,
        file_url = this.fileUrl
    )

    suspend fun getAll(call: ApplicationCall) {
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

            var nama = ""
            var penerbit = ""
            var tahun: Int? = null
            var fileBytes: ByteArray? = null
            var fileName = ""

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "nama_sertifikat" -> nama = part.value.trim()
                            "penerbit" -> penerbit = part.value.trim()
                            "tahun" -> tahun = part.value.trim().toIntOrNull()
                        }
                    }
                    is PartData.FileItem -> {
                        fileBytes = part.provider().toInputStream().readBytes()
                        fileName = part.originalFileName ?: "sertifikat.pdf"
                    }
                    else -> {}
                }
                part.dispose()
            }

            val sertifikat = Sertifikat(dosenId = "", namaSertifikat = nama, penerbit = penerbit, tahun = tahun)
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

            var nama: String? = null
            var penerbit: String? = null
            var tahun: Int? = null
            var fileBytes: ByteArray? = null
            var fileName = ""

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "nama_sertifikat" -> nama = part.value.trim()
                            "penerbit" -> penerbit = part.value.trim()
                            "tahun" -> tahun = part.value.trim().toIntOrNull()
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toInputStream().readBytes()
                        if (bytes.size > 0) {
                            fileBytes = bytes
                            fileName = part.originalFileName ?: "sertifikat_update.pdf"
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            val result = useCase.update(id, userId, role, nama, penerbit, tahun, fileBytes, fileName)
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
        call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, e.message ?: "Error"))
    }
}
