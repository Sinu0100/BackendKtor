package presentation.controller

import application.usecase.dosen.GetAllDosenUseCase
import application.usecase.dosen.ManageDosenUseCase
import domain.model.Dosen
import io.ktor.http.*
import io.ktor.utils.io.jvm.javaio.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import presentation.dto.ApiResponse
import presentation.dto.response.DosenResponse

import infrastructure.security.SecurityUtils

class DosenController(
    private val manageDosenUseCase: ManageDosenUseCase,
    private val getAllDosenUseCase: GetAllDosenUseCase
) {
    private fun getRole(call: ApplicationCall): String? {
        return SecurityUtils.getRole(call)
    }

    private fun Dosen.toResponse() = DosenResponse(
        id = this.id ?: "",
        nama = this.nama,
        nidn = this.nidn,
        jabatanFungsional = this.jabatanFungsional,
        pangkatGolongan = this.pangkatGolongan,
        email = this.email,
        noHp = this.noHp,
        fotoUrl = this.fotoUrl
    )

    suspend fun getAllDosen(call: ApplicationCall) {
        val dosenList = getAllDosenUseCase.execute()
        val response = dosenList.map { it.toResponse() }
        call.respond(HttpStatusCode.OK, ApiResponse(true, "Data dosen berhasil diambil", response))
    }

    suspend fun getDosenById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID dosen diperlukan")
            // Kita pinjam repository dari use case atau panggil via use case
            // Untuk detail, kita ambil datanya
            val dosen = manageDosenUseCase.getDosenById(id) 
            if (dosen == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "Dosen tidak ditemukan"))
                return
            }
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil mengambil detail dosen", dosen.toResponse()))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, e.message ?: "Terjadi kesalahan"))
        }
    }

    suspend fun createDosen(call: ApplicationCall) {
        val multipart = call.receiveMultipart()
        var nama = ""
        var nidn: String? = null
        var jabatan: String? = null
        var pangkat: String? = null
        var email: String? = null
        var noHp: String? = null
        var photo: Pair<String, ByteArray>? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> nama = part.value
                        "nidn" -> nidn = part.value
                        "jabatan_fungsional" -> jabatan = part.value
                        "pangkat_golongan" -> pangkat = part.value
                        "email" -> email = part.value
                        "no_hp" -> noHp = part.value
                    }
                }
                is PartData.FileItem -> {
                    val bytes = part.provider().toInputStream().readBytes()
                    if (bytes.size > 0) {
                        photo = (part.originalFileName ?: "dosen.jpg") to bytes
                    }
                }
                else -> {}
            }
            part.dispose()
        }

        val dosen = Dosen(
            nama = nama,
            nidn = nidn,
            jabatanFungsional = jabatan,
            pangkatGolongan = pangkat,
            email = email ?: "",
            noHp = noHp
        )
        
        val role = getRole(call)
        val result = manageDosenUseCase.createDosen(dosen, photo, role)
        call.respond(HttpStatusCode.Created, ApiResponse(true, "Dosen berhasil ditambahkan", result.toResponse()))
    }

    suspend fun updateDosen(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw IllegalArgumentException("ID dosen diperlukan")
        val multipart = call.receiveMultipart()
        
        var nama: String? = null
        var nidn: String? = null
        var jabatan: String? = null
        var pangkat: String? = null
        var email: String? = null
        var noHp: String? = null
        var photo: Pair<String, ByteArray>? = null

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> nama = part.value
                        "nidn" -> nidn = part.value
                        "jabatan_fungsional" -> jabatan = part.value
                        "pangkat_golongan" -> pangkat = part.value
                        "email" -> email = part.value
                        "no_hp" -> noHp = part.value
                    }
                }
                is PartData.FileItem -> {
                    val bytes = part.provider().toInputStream().readBytes()
                    if (bytes.size > 0) {
                        photo = (part.originalFileName ?: "dosen.jpg") to bytes
                    }
                }
                else -> {}
            }
            part.dispose()
        }

        val existing = manageDosenUseCase.updateDosen(id, Dosen(
            nama = nama ?: "",
            nidn = nidn,
            jabatanFungsional = jabatan,
            pangkatGolongan = pangkat,
            email = email ?: "",
            noHp = noHp
        ), photo, getRole(call))
        
        call.respond(HttpStatusCode.OK, ApiResponse(true, "Data dosen berhasil diperbarui", existing.toResponse()))
    }

    suspend fun deleteDosen(call: ApplicationCall) {
        val id = call.parameters["id"] ?: throw IllegalArgumentException("ID dosen diperlukan")
        val role = getRole(call)
        
        manageDosenUseCase.deleteDosen(id, role)
        call.respond(HttpStatusCode.OK, ApiResponse<Unit>(true, "Dosen berhasil dihapus"))
    }

}
