package presentation.routes

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.routing.*
import presentation.controller.AuthController

fun Route.authRoutes(controller: AuthController) {
    route("/auth") {
        rateLimit(RateLimitName("login-limiter")) {
            post("/login") {
                controller.login(call)
            }
        }
    }
}
