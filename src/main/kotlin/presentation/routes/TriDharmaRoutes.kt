package presentation.routes

import infrastructure.security.withRole
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.TriDharmaController

fun Route.triDharmaRoutes(controller: TriDharmaController) {
    // PUBLIC: Semua orang bisa lihat
    get("/tri-dharma") { controller.getAllTriDharma(call) }
    get("/tri-dharma/{id}") { controller.getById(call) }

    // PROTECTED: Harus Admin untuk Manage Data Prodi
    authenticate {
        route("/tri-dharma") {
            post { 
                call.withRole("admin") { controller.createTriDharma(call) }
            }
            put("/{id}") { 
                call.withRole("admin") { controller.updateTriDharma(call) }
            }
            delete("/{id}") { 
                call.withRole("admin") { controller.deleteTriDharma(call) }
            }
        }
    }
}
