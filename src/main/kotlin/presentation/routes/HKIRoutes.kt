package presentation.routes

import infrastructure.security.withRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.HKIController

fun Route.hkiRoutes(controller: HKIController) {
    route("/hki") {
        get { controller.getAll(call) }
        
        authenticate {
            get("/my") {
                call.withRole("admin", "dosen") {
                    controller.getMyHKI(call)
                }
            }
            get("/{id}") { controller.getById(call) }
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
