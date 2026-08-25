package br.com.autorepairshop.authentication.infrastructure.security

import br.com.autorepairshop.authentication.application.security.PasswordHasher
import br.com.autorepairshop.authentication.domain.valueobject.HashedPassword
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BcryptPasswordHasher(private val encoder: PasswordEncoder) : PasswordHasher {
    override fun hash(raw: String): HashedPassword {
        val hash = encoder.encode(raw)
            ?: error(message = "Password encoder returned null hash.")
        return HashedPassword(value = hash)
    }

    override fun matches(
        raw: String,
        hashed: HashedPassword,
    ): Boolean = encoder.matches(raw, hashed.value)
}
