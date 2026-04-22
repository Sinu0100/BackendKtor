package presentation.routes

import infrastructure.security.withRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.PublikasiController

fun Route.publikasiRoutes(controller: PublikasiController) {
    route("/publikasi") {
        get { controller.getAllPublikasi(call) }
        get("/{id}") { controller.getById(call) }

        authenticate {
            post {
                call.withRole("admin", "dosen") {
                    controller.createPublikasi(call)
                }
            }
            put("/{id}") {
                call.withRole("admin", "dosen") {
                    controller.updatePublikasi(call)
                }
            }
            delete("/{id}") {
                call.withRole("admin", "dosen") {
                    controller.deletePublikasi(call)
                }
            }
        }
    }
}
