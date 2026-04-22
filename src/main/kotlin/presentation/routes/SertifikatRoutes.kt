package presentation.routes

import infrastructure.security.withRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.SertifikatController

fun Route.sertifikatRoutes(controller: SertifikatController) {
    route("/sertifikat") {
        get { controller.getAll(call) }
        get("/{id}") { controller.getById(call) }

        authenticate {
            post {
                call.withRole("admin", "dosen") {
                    controller.create(call)
                }
            }
            put("/{id}") {
                call.withRole("admin", "dosen") {
                    controller.update(call)
                }
            }
            delete("/{id}") {
                call.withRole("admin", "dosen") {
                    controller.delete(call)
                }
            }
        }
    }
}
