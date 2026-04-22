package presentation.controller

import application.usecase.tri_dharma.ManageTriDharmaUseCase
import domain.model.TriDharma
import domain.model.Media
import infrastructure.security.SecurityUtils
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.jvm.javaio.*
import presentation.dto.ApiResponse
import presentation.dto.response.TriDharmaResponse
import presentation.dto.response.MediaResponse

class TriDharmaController(private val manageUseCase: ManageTriDharmaUseCase) {

    private fun TriDharma.toResponse() = TriDharmaResponse(
        id = this.id ?: "",
        judul = this.judulKegiatan,
        deskripsi = this.deskripsi ?: "",
        tanggal = this.tanggalKegiatan,
        created_at = this.createdAt,
        media = this.media.map { MediaResponse(id = it.id ?: "", file_url = it.fileUrl) }
    )

    // Helper untuk handle input tanggal yang cuma tahun dan TRIM karakter babi
    private fun formatInputDate(input: String?): String? {
        val trimmed = input?.trim() // BUANG SPASI/NEWLINE
        if (trimmed.isNullOrEmpty()) return null
        
        return if (trimmed.length == 4 && trimmed.all { it.isDigit() }) {
            "$trimmed-01-01"
        } else {
            trimmed
        }
    }

    suspend fun getAllTriDharma(call: ApplicationCall) {
        try {
            val result = manageUseCase.getAllTriDharma()
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.map { it.toResponse() }))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ApiResponse<Unit>(false, e.message ?: "Terjadi kesalahan"))
        }
    }

    suspend fun getById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID diperlukan")
            val result = manageUseCase.getTriDharmaById(id)
            if (result == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "Data tidak ditemukan"))
                return
            }
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun createTriDharma(call: ApplicationCall) {
        try {
            val role = SecurityUtils.getRole(call)
            val multipart = call.receiveMultipart()
            var judul = ""
            var deskripsi = ""
            var tanggalInput = ""
            val files = mutableListOf<Pair<String, ByteArray>>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        val value = part.value.trim() // TRIM SEMUA INPUT
                        when (part.name) {
                            "judul", "judul_kegiatan" -> judul = value
                            "deskripsi" -> deskripsi = value
                            "tanggal", "tanggal_kegiatan" -> tanggalInput = value
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toInputStream().readBytes()
                        if (bytes.isNotEmpty()) {
                            files.add((part.originalFileName ?: "tri_dharma.jpg") to bytes)
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (judul.isEmpty()) throw Exception("Judul kegiatan wajib diisi")

            val td = TriDharma(
                judulKegiatan = judul, 
                deskripsi = deskripsi, 
                tanggalKegiatan = formatInputDate(tanggalInput)
            )
            val result = manageUseCase.createTriDharma(td, files, role)
            call.respond(HttpStatusCode.Created, ApiResponse(true, "Berhasil ditambahkan", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun updateTriDharma(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID diperlukan")
            val role = SecurityUtils.getRole(call)
            val multipart = call.receiveMultipart()
            
            var judul: String? = null
            var deskripsi: String? = null
            var tanggalInput: String? = null
            val files = mutableListOf<Pair<String, ByteArray>>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        val value = part.value.trim()
                        when (part.name) {
                            "judul", "judul_kegiatan" -> judul = value
                            "deskripsi" -> deskripsi = value
                            "tanggal", "tanggal_kegiatan" -> tanggalInput = value
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toInputStream().readBytes()
                        if (bytes.isNotEmpty()) {
                            files.add((part.originalFileName ?: "tri_dharma.jpg") to bytes)
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            val result = manageUseCase.updateTriDharma(
                id, 
                judul, 
                deskripsi, 
                formatInputDate(tanggalInput),
                files, 
                role
            )
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil diperbarui", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun deleteTriDharma(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID diperlukan")
            val role = SecurityUtils.getRole(call)
            manageUseCase.deleteTriDharma(id, role)
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
