package presentation.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.FasilitasController

fun Route.fasilitasRoutes(controller: FasilitasController) {
    route("/fasilitas") {
        // Public: Siapa saja bisa lihat daftar dan detail fasilitas
        get {
            controller.getAll(call)
        }
        get("/{id}") {
            controller.getById(call)
        }

        // Admin Only
        authenticate {
            post {
                controller.create(call)
            }
            put("/{id}") {
                controller.update(call)
            }
            delete("/{id}") {
                controller.delete(call)
            }
        }
    }
}
