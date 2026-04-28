package presentation.routes

import infrastructure.security.withRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.BukuAjarController

fun Route.bukuAjarRoutes(controller: BukuAjarController) {
    route("/buku-ajar") {
        get { controller.getAll(call) }
        get("/{id}") { controller.getById(call) }

        authenticate {
            get("/my") {
                call.withRole("dosen") {
                    controller.getMyBuku(call)
                }
            }
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
