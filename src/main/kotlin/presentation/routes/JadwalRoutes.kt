package presentation.routes

import infrastructure.security.withRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.JadwalPerkuliahanController

fun Route.jadwalRoutes(controller: JadwalPerkuliahanController) {
    route("/jadwal") {
        get {
            controller.getAllJadwal(call)
        }

        authenticate {
            post {
                call.withRole("admin") {
                    controller.createJadwal(call)
                }
            }
            put("/{id}") {
                call.withRole("admin") {
                    controller.updateJadwal(call)
                }
            }
            delete("/{id}") {
                call.withRole("admin") {
                    controller.deleteJadwal(call)
                }
            }
        }
    }
}
