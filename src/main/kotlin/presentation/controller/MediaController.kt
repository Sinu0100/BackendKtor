package presentation.controller

import application.usecase.media.ManageMediaUseCase
import domain.repository.DosenRepository
import domain.repository.PenelitianRepository
import domain.repository.PengabdianRepository
import domain.repository.HKIRepository
import domain.repository.SertifikatRepository
import infrastructure.security.SecurityUtils
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import presentation.dto.ApiResponse
import presentation.dto.response.MediaResponse

class MediaController(
    private val mediaUseCase: ManageMediaUseCase,
    private val dosenRepository: DosenRepository,
    private val penelitianRepository: PenelitianRepository,
    private val pengabdianRepository: PengabdianRepository,
    private val hkiRepository: HKIRepository,
    private val sertifikatRepository: SertifikatRepository
) {
    /**
     * DELETE /api/v1/media/{mediaId}
     * 
     * Hapus satu media/gambar berdasarkan ID.
     * - Admin bisa hapus media apa saja.
     * - Dosen hanya bisa hapus media milik entity mereka sendiri.
     */
    suspend fun deleteMedia(call: ApplicationCall) {
        try {
            val mediaId = call.parameters["mediaId"] ?: throw Exception("ID media diperlukan")
            val userId = SecurityUtils.getUserId(call) ?: throw Exception("Unauthorized")
            val role = SecurityUtils.getRole(call)

            // 1. Cari media-nya dulu
            val media = mediaUseCase.getById(mediaId)
                ?: return call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(false, "Media tidak ditemukan"))

            // 2. Cek ownership (kalau bukan admin)
            if (role != "admin") {
                val entityType = media.entityType
                val entityId = media.entityId

                // Entity milik admin saja
                if (entityType in listOf("fasilitas", "tri_dharma")) {
                    throw Exception("FORBIDDEN: Hanya Admin yang dapat menghapus media ${entityType.replace("_", " ")}")
                }

                // Entity milik dosen — cek apakah dosen yang login adalah pemiliknya
                val dosen = dosenRepository.getByUserId(userId)
                    ?: throw Exception("FORBIDDEN: Profil dosen tidak ditemukan untuk user ini")
                val dosenId = dosen.id ?: throw Exception("FORBIDDEN: Dosen ID tidak valid")

                val isOwner = when (entityType) {
                    "penelitian" -> {
                        val entity = penelitianRepository.getById(entityId)
                        entity?.dosenId == dosenId
                    }
                    "pengabdian" -> {
                        val entity = pengabdianRepository.getById(entityId)
                        entity?.dosenId == dosenId
                    }
                    "hki" -> {
                        val entity = hkiRepository.getById(entityId)
                        entity?.dosenId == dosenId
                    }
                    "sertifikat" -> {
                        val entity = sertifikatRepository.getById(entityId)
                        entity?.dosenId == dosenId
                    }
                    else -> false
                }

                if (!isOwner) {
                    throw Exception("FORBIDDEN: Anda tidak berhak menghapus media ini")
                }
            }

            // 3. Hapus media (file + DB record)
            mediaUseCase.deleteSingleMedia(mediaId)

            call.respond(HttpStatusCode.OK, ApiResponse<Unit>(true, "Media berhasil dihapus"))
        } catch (e: Exception) {
            val status = when {
                e.message?.contains("FORBIDDEN", ignoreCase = true) == true -> HttpStatusCode.Forbidden
                e.message?.contains("Unauthorized", ignoreCase = true) == true -> HttpStatusCode.Unauthorized
                else -> HttpStatusCode.BadRequest
            }
            call.respond(status, ApiResponse<Unit>(false, e.message ?: "Terjadi kesalahan"))
        }
    }

    /**
     * GET /api/v1/media/entity/{entityType}/{entityId}
     * 
     * Lihat semua media dari suatu entity. Public access.
     */
    suspend fun getMediaByEntity(call: ApplicationCall) {
        try {
            val entityType = call.parameters["entityType"] ?: throw Exception("Entity type diperlukan")
            val entityId = call.parameters["entityId"] ?: throw Exception("Entity ID diperlukan")

            val mediaList = mediaUseCase.getMediaByEntity(entityType, entityId)
            val response = mediaList.map { MediaResponse(id = it.id ?: "", file_url = it.fileUrl) }

            call.respond(HttpStatusCode.OK, ApiResponse(true, "Berhasil", response))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(false, e.message ?: "Error"))
        }
    }
}
