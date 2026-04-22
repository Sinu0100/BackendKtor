package infrastructure.security

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {
    fun hash(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun check(password: String, hash: String): Boolean {
        return BCrypt.checkpw(password, hash)
    }
}
