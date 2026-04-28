package presentation.routes

import infrastructure.security.withRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.KeahlianController

fun Route.keahlianRoutes(controller: KeahlianController) {
    // 1. Endpoint Publik: Liat daftar keahlian yang tersedia
    get("/keahlian") { controller.getAllMaster(call) }

    authenticate {
        // 2. Endpoint Admin: Kelola Keahlian
        route("/admin/keahlian") {
            post {
                call.withRole("admin") {
                    controller.createMaster(call)
                }
            }
            // Admin bisa menugaskan keahlian ke dosen tertentu
            post("/assign") {
                call.withRole("admin") {
                    controller.assignByAdmin(call)
                }
            }
        }

        // 3. Endpoint Dosen: Kelola keahlian di profil sendiri
        route("/dosen/keahlian") {
            get { controller.getMyKeahlian(call) }
            post { controller.addKeahlianToMe(call) }
            delete("/{id}") { controller.removeKeahlianFromMe(call) }
        }
    }
}
