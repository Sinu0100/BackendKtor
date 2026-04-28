package presentation.routes

import infrastructure.security.withRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.DosenController

fun Route.dosenRoutes(dosenController: DosenController) {
    route("/dosen") {
            // Public: Siapa saja bisa lihat daftar dan detail dosen
        get {
            dosenController.getAllDosen(call)
        }
        get("/{id}") {
            dosenController.getDosenById(call)
        }

        // Admin Only (Dikelola oleh Admin)
        authenticate {
            get("/me") {
                dosenController.getProfileMe(call)
            }
            post {
                call.withRole("admin") {
                    dosenController.createDosen(call)
                }
            }
            put("/{id}") {
                call.withRole("admin") {
                    dosenController.updateDosen(call)
                }
            }
            delete("/{id}") {
                call.withRole("admin") {
                    dosenController.deleteDosen(call)
                }
            }
        }
    }
}
