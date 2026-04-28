package presentation.controller

import application.usecase.dosen.GetAllDosenUseCase
import application.usecase.dosen.ManageDosenUseCase
import application.usecase.keahlian.ManageKeahlianUseCase
import domain.model.Dosen
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import presentation.dto.ApiResponse
import presentation.dto.response.DosenResponse
import infrastructure.security.SecurityUtils
import io.ktor.utils.io.jvm.javaio.*

class DosenController(
    private val manageDosenUseCase: ManageDosenUseCase,
    private val getAllDosenUseCase: GetAllDosenUseCase,
    private val keahlianUseCase: ManageKeahlianUseCase
) {
    private fun getRole(call: ApplicationCall): String? = SecurityUtils.getRole(call)

    private suspend fun Dosen.toResponse(): DosenResponse {
        val listKeahlian = if (this.id != null) {
            val items = keahlianUseCase.getKeahlianByDosenId(this.id)
            items.map { 
                domain.model.Keahlian(
                    id = it.keahlian_id, 
                    nama_keahlian = it.nama_keahlian ?: "Unknown"
                ) 
            }
        } else {
            emptyList()
        }

        return DosenResponse(
            id = this.id ?: "",
            nama = this.nama,
            nidn = this.nidn,
            jabatanFungsional = this.jabatanFungsional,
            pangkatGolongan = this.pangkatGolongan,
            email = this.email,
            noHp = this.noHp,
            fotoUrl = this.fotoUrl,
            created_at = this.createdAt,
            updated_at = this.updatedAt,
            role = this.role,
            keahlian = listKeahlian
        )
    }

    suspend fun getAllDosen(call: ApplicationCall) {
        try {
            val dosenList = getAllDosenUseCase.execute()
            val response = dosenList.map { it.toResponse() }
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Data dosen berhasil diambil", response))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, ApiResponse<Unit>(false, e.message ?: "Terjadi kesalahan"))
        }
    }

    suspend fun getDosenById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"] ?: throw Exception("ID dosen diperlukan")
            val dosen = manageDosenUseCase.getDosenById(id) 
            if (dosen == null) {
                call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "Dosen tidak ditemukan"))
                return
            }
            
            // Konversi ke response (termasuk narik keahlian)
            val responseData = dosen.toResponse()
            
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil mengambil detail dosen", responseData))
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
        var password = "" // Tambahkan password
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
                        "password" -> password = part.value // Ambil password dari form
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

        if (password.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, "Password wajib diisi"))
            return
        }

        val dosen = Dosen(
            nama = nama,
            nidn = nidn,
            jabatanFungsional = jabatan,
            pangkatGolongan = pangkat,
            email = email ?: "",
            noHp = noHp
        )
        
        try {
            val role = getRole(call)
            val result = manageDosenUseCase.createDosenWithUser(dosen, password, photo, role)
            call.respond(HttpStatusCode.Created, ApiResponse(true, "Dosen dan Akun berhasil ditambahkan", result.toResponse()))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, e.message ?: "Gagal membuat dosen"))
        }
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

    suspend fun getProfileMe(call: ApplicationCall) {
        try {
            val userId = SecurityUtils.getUserId(call) ?: throw Exception("Token tidak valid")
            val dosen = manageDosenUseCase.getProfileMe(userId)
            
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil mengambil profil saya", dosen.toResponse()))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ApiResponse<Unit>(false, e.message ?: "Terjadi kesalahan"))
        }
    }
}
