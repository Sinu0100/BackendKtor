package presentation.controller

import application.usecase.fasilitas.ManageFasilitasUseCase
import domain.model.Fasilitas
import domain.model.Media
import infrastructure.security.SecurityUtils
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.jvm.javaio.*
import presentation.dto.ApiResponse
import presentation.dto.response.FasilitasResponse
import presentation.dto.response.MediaResponse

class FasilitasController(private val manageUseCase: ManageFasilitasUseCase) {

    private fun Fasilitas.toResponse() = FasilitasResponse(
        id = this.id ?: "",
        nama_fasilitas = this.judulFasilitas,
        deskripsi = this.deskripsi ?: "",
        created_at = this.createdAt,
        media = this.media.map { it.toMediaResponse() }
    )

    private fun Media.toMediaResponse() = MediaResponse(
        id = this.id ?: "",
        file_url = this.fileUrl
    )

    suspend fun getAll(call: ApplicationCall) {
        try {
            val result = manageUseCase.getAllFasilitas()
            val response = result.map { it.toResponse() }
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil mengambil data fasilitas", response))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun getById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID Fasilitas diperlukan")
            val result = manageUseCase.getFasilitasById(id)
            if (result == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "Fasilitas tidak ditemukan"))
                return
            }
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun create(call: ApplicationCall) {
        try {
            val role = SecurityUtils.getRole(call)
            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(false, "FORBIDDEN: Hanya Admin yang dapat menambah fasilitas"))
                return
            }

            val multipart = call.receiveMultipart()
            var judulFasilitas = ""
            var deskripsi = ""
            val files = mutableListOf<Pair<String, ByteArray>>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "judul_fasilitas" -> judulFasilitas = part.value
                            "nama_fasilitas" -> judulFasilitas = part.value
                            "deskripsi" -> deskripsi = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toInputStream().readBytes()
                        if (bytes.size > 0) {
                            val name = part.originalFileName ?: "fasilitas.jpg"
                            files.add(name to bytes)
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (judulFasilitas.isEmpty()) throw Exception("Judul fasilitas wajib diisi")

            val fasilitas = Fasilitas(
                judulFasilitas = judulFasilitas, 
                deskripsi = deskripsi
            )
            val result = manageUseCase.createFasilitas(fasilitas, files, role)
            call.respond(HttpStatusCode.Created, ApiResponse(true, "Fasilitas berhasil ditambahkan", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun update(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID Fasilitas diperlukan")
            val role = SecurityUtils.getRole(call)
            if (role != "admin") {
                call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(false, "FORBIDDEN: Hanya Admin yang dapat mengubah fasilitas"))
                return
            }

            val multipart = call.receiveMultipart()
            var judulFasilitas: String? = null
            var deskripsi: String? = null
            val files = mutableListOf<Pair<String, ByteArray>>()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "judul_fasilitas" -> judulFasilitas = part.value
                            "nama_fasilitas" -> judulFasilitas = part.value
                            "deskripsi" -> deskripsi = part.value
                        }
                    }
                    is PartData.FileItem -> {
                        val bytes = part.provider().toInputStream().readBytes()
                        if (bytes.size > 0) {
                            val name = part.originalFileName ?: "fasilitas.jpg"
                            files.add(name to bytes)
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            val result = manageUseCase.updateFasilitas(id, judulFasilitas, deskripsi, files, role)
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Fasilitas berhasil diperbarui", result.toResponse()))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun delete(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID Fasilitas diperlukan")
            val role = SecurityUtils.getRole(call)
            
            manageUseCase.deleteFasilitas(id, role)
            call.respond(HttpStatusCode.OK, ApiResponse<Unit>(true, "Fasilitas berhasil dihapus"))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    private suspend fun handleError(call: ApplicationCall, e: Exception) {
        val status = if (e.message?.contains("FORBIDDEN", ignoreCase = true) == true) HttpStatusCode.Forbidden else HttpStatusCode.BadRequest
        call.respond(status, ApiResponse<Unit>(false, e.message ?: "Terjadi kesalahan"))
    }
}
