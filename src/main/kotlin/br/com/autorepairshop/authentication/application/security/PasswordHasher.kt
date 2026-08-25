package br.com.autorepairshop.authentication.application.security

import br.com.autorepairshop.authentication.domain.valueobject.HashedPassword

interface PasswordHasher {
    fun hash(raw: String): HashedPassword
    fun matches(
        raw: String,
        hashed: HashedPassword,
    ): Boolean
}
