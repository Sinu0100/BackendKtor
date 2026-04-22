package application.usecase.auth

import domain.repository.DosenRepository
import infrastructure.security.JwtService
import infrastructure.security.TokenInfo
import org.mindrot.jbcrypt.BCrypt

class LoginUseCase(
    private val dosenRepository: DosenRepository,
    private val jwtService: JwtService
) {
    suspend fun login(email: String, password: String): TokenInfo? {
        // Cari user di database (gabungan tabel users & dosen)
        val user = dosenRepository.getByEmail(email) ?: return null

        // Ambil password hash yang sudah ditarik dari tabel users
        val passwordHash = user.passwordHash ?: return null
        
        // Verifikasi password
        val isPasswordValid = BCrypt.checkpw(password, passwordHash)
        if (!isPasswordValid) return null

        // Generate token JWT
        return jwtService.generateToken(
            userId = user.userId ?: "",
            email = user.email,
            role = user.role ?: "user"
        )
    }
}
