package presentation.controller

import application.usecase.jadwal.ManageJadwalPerkuliahanUseCase
import domain.model.JadwalPerkuliahan
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.jvm.javaio.*
import presentation.dto.ApiResponse
import presentation.dto.response.JadwalPerkuliahanResponse
import infrastructure.security.SecurityUtils

class JadwalPerkuliahanController(private val manageUseCase: ManageJadwalPerkuliahanUseCase) {

    private fun getRole(call: ApplicationCall): String? {
        return SecurityUtils.getRole(call)
    }

    private fun JadwalPerkuliahan.toResponse() = JadwalPerkuliahanResponse(
        id = this.id ?: 0,
        nama_jadwal = this.namaJadwal,
        tanggal_upload = this.tanggalUpload,
        file_url = this.fileUrl
    )

    suspend fun getAllJadwal(call: ApplicationCall) {
        try {
            val result = manageUseCase.getAllJadwal()
            val response = result.map { it.toResponse() }
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Data jadwal berhasil diambil", response))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun createJadwal(call: ApplicationCall) {
        try {
            val role = getRole(call)
            // CEK LAGI DI CONTROLLER BIAR DOUBLE SECURE
            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(false, "FORBIDDEN: Hanya Admin yang dapat menambah jadwal"))
                return
            }

            val multipart = call.receiveMultipart()
            var namaJadwal = ""
            var fileBytes: ByteArray? = null
            var fileName = ""

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "nama_jadwal") namaJadwal = part.value
                    }
                    is PartData.FileItem -> {
                        if (part.name == "file") {
                            fileName = part.originalFileName ?: "jadwal.pdf"
                            val bytes = part.provider().toInputStream().readBytes()
                            if (bytes.isNotEmpty()) {
                                fileBytes = bytes
                            }
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (fileBytes == null || fileBytes.isEmpty()) throw Exception("File jadwal wajib diunggah")
            
            val result = manageUseCase.createJadwal(namaJadwal, fileName, fileBytes, role)
            call.respond(HttpStatusCode.Created, ApiResponse(true, "Jadwal berhasil ditambahkan", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun updateJadwal(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw Exception("ID jadwal diperlukan")
            val role = getRole(call)
            
            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(false, "FORBIDDEN: Hanya Admin yang dapat mengubah jadwal"))
                return
            }

            val multipart = call.receiveMultipart()
            var namaJadwal: String? = null
            var fileBytes: ByteArray? = null
            var fileName: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "nama_jadwal") {
                            namaJadwal = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        if (part.name == "file") {
                            val bytes = part.provider().toInputStream().readBytes()
                            if (bytes.isNotEmpty()) {
                                fileBytes = bytes
                                fileName = part.originalFileName ?: "jadwal.pdf"
                            }
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }
            
            val result = manageUseCase.updateJadwal(
                id, 
                JadwalPerkuliahan(namaJadwal = namaJadwal ?: "", fileUrl = ""), 
                role,
                fileBytes,
                fileName
            )
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Jadwal berhasil diperbarui", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun deleteJadwal(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]?.toIntOrNull() ?: throw Exception("ID jadwal diperlukan")
            val role = getRole(call)
            
            manageUseCase.deleteJadwal(id, role)
            call.respond(HttpStatusCode.OK, ApiResponse<Unit>(true, "Jadwal berhasil dihapus"))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    private suspend fun handleError(call: ApplicationCall, e: Exception) {
        val status = if (e.message?.contains("FORBIDDEN", ignoreCase = true) == true) HttpStatusCode.Forbidden else HttpStatusCode.BadRequest
        call.respond(status, ApiResponse<Unit>(false, e.message ?: "Terjadi kesalahan"))
    }
}
