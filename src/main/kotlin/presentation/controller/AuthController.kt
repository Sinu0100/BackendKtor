package presentation.controller

import application.usecase.auth.LoginUseCase
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import presentation.dto.ApiResponse
import presentation.dto.request.LoginRequest
import presentation.dto.response.LoginResponse

class AuthController(private val loginUseCase: LoginUseCase) {
    
    suspend fun login(call: ApplicationCall) {
        val request = call.receive<LoginRequest>()
        
        // UseCase sekarang balikin TokenInfo?
        val tokenInfo = loginUseCase.login(request.email, request.password)
        
        if (tokenInfo != null) {
            call.respond(
                HttpStatusCode.OK, 
                ApiResponse(
                    success = true,
                    message = "Login Berhasil",
                    data = LoginResponse(
                        token = tokenInfo.token,
                        expiresAt = tokenInfo.expiresAt
                    )
                )
            )
        } else {
            // Kita gunakan Forbidden atau pesan khusus supaya tidak kena jebakan StatusPages 401
            call.respond(
                HttpStatusCode.Forbidden,
                ApiResponse<Unit>(
                    success = false,
                    message = "Email atau password salah"
                )
            )
        }
    }
}
