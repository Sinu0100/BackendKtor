package presentation.routes

import infrastructure.security.withRole
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import presentation.controller.StatistikController

fun Route.statistikRoutes(statistikController: StatistikController) {
    route("/statistik") {
        // Public: Lihat statistik
        get {
            statistikController.getAllStatistik(call)
        }

        // Protected: Admin Only
        authenticate {
            post {
                call.withRole("admin") {
                    statistikController.createStatistik(call)
                }
            }
            put("/{id}") {
                call.withRole("admin") {
                    statistikController.updateStatistik(call)
                }
            }
            delete("/{id}") {
                call.withRole("admin") {
                    statistikController.deleteStatistik(call)
                }
            }
        }
    }
}
