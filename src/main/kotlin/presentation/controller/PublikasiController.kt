package presentation.controller

import application.usecase.publikasi.ManagePublikasiUseCase
import domain.model.Publikasi
import infrastructure.security.SecurityUtils
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.jvm.javaio.*
import presentation.dto.ApiResponse
import presentation.dto.response.PublikasiResponse
import presentation.dto.response.MediaResponse

class PublikasiController(private val useCase: ManagePublikasiUseCase) {

    private fun getUserId(call: ApplicationCall): String = SecurityUtils.getUserId(call) ?: throw Exception("Unauthorized")
    private fun getRole(call: ApplicationCall): String? = SecurityUtils.getRole(call)

    private fun Publikasi.toResponse() = PublikasiResponse(
        id = this.id ?: "",
        dosen_id = this.dosenId,
        judul = this.judul,
        nama_jurnal_konferensi = this.namaJurnalKonferensi,
        deskripsi = this.deskripsi ?: "",
        link_tautan = this.linkTautan,
        tahun = this.tahun,
        created_at = this.createdAt,
        media = emptyList()
    )

    suspend fun getAllPublikasi(call: ApplicationCall) {
        try {
            val result = useCase.getAll()
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result.map { it.toResponse() }))
        } catch (e: Exception) {
            handleError(call, e)
        }
    }

    suspend fun getMyPublikasi(call: ApplicationCall) {
        try {
            val userId = getUserId(call)
            val result = useCase.getMyPublikasi(userId)
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

    suspend fun createPublikasi(call: ApplicationCall) {
        try {
            val userId = getUserId(call)
            val multipart = call.receiveMultipart()
            
            var judul = ""
            var namaJurnalKonferensi: String? = null
            var deskripsi: String? = null
            var linkTautan: String? = null
            var tahun: Int? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        val value = part.value.trim()
                        if (value.isNotEmpty()) {
                            when (part.name) {
                                "judul" -> judul = value
                                "nama_jurnal_konferensi" -> namaJurnalKonferensi = value
                                "deskripsi" -> deskripsi = value
                                "link_tautan" -> linkTautan = value
                                "tahun" -> {
                                    val t = value.toIntOrNull()
                                    if (t == null || t !in 1900..2100) throw IllegalArgumentException("Tahun tidak valid (1900-2100)")
                                    tahun = t
                                }
                            }
                        }
                    }
                    is PartData.FileItem -> {
                        // Jika publikasi belum dukung media, abaikan dulu atau simpan logicnya
                        part.dispose()
                    }
                    else -> part.dispose()
                }
            }

            if (judul.isEmpty()) throw Exception("Judul wajib diisi")

            val publikasi = Publikasi(
                dosenId = "", 
                judul = judul,
                namaJurnalKonferensi = namaJurnalKonferensi,
                deskripsi = deskripsi,
                linkTautan = linkTautan,
                tahun = tahun
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
            val multipart = call.receiveMultipart()

            var judul: String? = null
            var namaJurnalKonferensi: String? = null
            var deskripsi: String? = null
            var linkTautan: String? = null
            var tahun: Int? = null

            multipart.forEachPart { part ->
                if (part is PartData.FormItem) {
                    val value = part.value.trim()
                    if (value.isNotEmpty()) {
                        when (part.name) {
                            "judul" -> judul = value
                            "nama_jurnal_konferensi" -> namaJurnalKonferensi = value
                            "deskripsi" -> deskripsi = value
                            "link_tautan" -> linkTautan = value
                            "tahun" -> tahun = value.toIntOrNull()
                        }
                    }
                }
                part.dispose()
            }

            val existing = useCase.getById(id) ?: throw Exception("Data tidak ditemukan")
            
            val updated = existing.copy(
                judul = judul ?: existing.judul,
                namaJurnalKonferensi = namaJurnalKonferensi ?: existing.namaJurnalKonferensi,
                deskripsi = deskripsi ?: existing.deskripsi,
                linkTautan = linkTautan ?: existing.linkTautan,
                tahun = tahun ?: existing.tahun
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
        val status = when {
            e is IllegalArgumentException -> HttpStatusCode.BadRequest
            e.message?.contains("FORBIDDEN", ignoreCase = true) == true -> HttpStatusCode.Forbidden
            e.message?.contains("Unauthorized", ignoreCase = true) == true -> HttpStatusCode.Unauthorized
            else -> HttpStatusCode.BadRequest
        }
        call.respond(status, ApiResponse<Unit>(false, e.message ?: "Terjadi kesalahan"))
    }
}
