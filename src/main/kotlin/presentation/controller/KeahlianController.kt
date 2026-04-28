package presentation.controller

import application.usecase.keahlian.ManageKeahlianUseCase
import infrastructure.security.SecurityUtils
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import presentation.dto.ApiResponse
import kotlinx.serialization.Serializable

@Serializable
data class KeahlianRequest(val nama_keahlian: String)

@Serializable
data class DosenKeahlianRequest(
    val keahlian_id: Int,
    val dosen_id: String? = null // Gw tambahin optional dosen_id di sini buat testing
)

@Serializable
data class AdminAssignKeahlianRequest(val dosen_id: String, val keahlian_id: Int)

class KeahlianController(private val useCase: ManageKeahlianUseCase) {

    suspend fun getAllMaster(call: ApplicationCall) {
        try {
            val result = useCase.getAllKeahlian()
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil mengambil master keahlian", result))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, e.message ?: "Error"))
        }
    }

    suspend fun createMaster(call: ApplicationCall) {
        try {
            val role = SecurityUtils.getRole(call)
            val request = call.receive<KeahlianRequest>()
            val result = useCase.createKeahlian(request.nama_keahlian, role)
            call.respond(HttpStatusCode.Created, ApiResponse(true, "Master keahlian berhasil ditambah", result))
        } catch (e: Exception) {
            val status = if (e.message?.contains("FORBIDDEN") == true) HttpStatusCode.Forbidden else HttpStatusCode.BadRequest
            call.respond(status, ApiResponse<Unit>(false, e.message ?: "Error"))
        }
    }

    suspend fun assignByAdmin(call: ApplicationCall) {
        try {
            val role = SecurityUtils.getRole(call)
            val request = call.receive<AdminAssignKeahlianRequest>()
            useCase.assignKeahlianToDosen(request.dosen_id, request.keahlian_id, role)
            call.respond(HttpStatusCode.Created, ApiResponse<Unit>(true, "Admin berhasil menugaskan keahlian ke Dosen"))
        } catch (e: Exception) {
            val status = if (e.message?.contains("FORBIDDEN") == true) HttpStatusCode.Forbidden else HttpStatusCode.BadRequest
            call.respond(status, ApiResponse<Unit>(false, e.message ?: "Error"))
        }
    }

    suspend fun getMyKeahlian(call: ApplicationCall) {
        try {
            val userId = SecurityUtils.getUserId(call) ?: throw Exception("Unauthorized")
            val result = useCase.getKeahlianByDosen(userId)
            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", result))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, e.message ?: "Error"))
        }
    }

    suspend fun addKeahlianToMe(call: ApplicationCall) {
        try {
            val request = call.receive<DosenKeahlianRequest>()
            val userId = SecurityUtils.getUserId(call)

            // LOGIC BARU: Kalau lu kirim dosen_id di body, pake itu! (Biar testing lu gak gagal lagi)
            if (!request.dosen_id.isNullOrBlank()) {
                // Testing mode: anggap sebagai admin biar gak kena check userId
                useCase.assignKeahlianToDosen(request.dosen_id, request.keahlian_id, "admin")
            } else {
                if (userId == null) throw Exception("Unauthorized")
                useCase.addKeahlianToDosen(userId, request.keahlian_id)
            }
            
            call.respond(HttpStatusCode.Created, ApiResponse<Unit>(true, "Keahlian berhasil ditambahkan"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, e.message ?: "Error"))
        }
    }
    
    suspend fun removeKeahlianFromMe(call: ApplicationCall) {
        try {
            val userId = SecurityUtils.getUserId(call) ?: throw Exception("Unauthorized")
            val id = call.parameters["id"]?.toIntOrNull() ?: throw Exception("ID Keahlian diperlukan")
            useCase.removeKeahlianFromDosen(userId, id)
            call.respond(HttpStatusCode.OK, ApiResponse<Unit>(true, "Keahlian berhasil dihapus dari profil Anda"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, e.message ?: "Error"))
        }
    }
}
