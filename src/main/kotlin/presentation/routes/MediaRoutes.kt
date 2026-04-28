package presentation.routes

import infrastructure.security.withRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.MediaController

fun Route.mediaRoutes(controller: MediaController) {
    route("/media") {
        // Public: Lihat media dari entity tertentu
        get("/entity/{entityType}/{entityId}") {
            controller.getMediaByEntity(call)
        }

        // Protected: Hapus media satu per satu
        authenticate {
            delete("/{mediaId}") {
                call.withRole("admin", "dosen") {
                    controller.deleteMedia(call)
                }
            }
        }
    }
}
