package presentation.routes

import infrastructure.security.withRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.PengabdianController

fun Route.pengabdianRoutes(controller: PengabdianController) {
    route("/pengabdian") {
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
