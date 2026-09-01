package br.com.autorepairshop.accessidentity.application.security

import br.com.autorepairshop.accessidentity.domain.valueobject.HashedPassword

interface PasswordHasher {
    fun hash(raw: String): HashedPassword
    fun matches(
        raw: String,
        hashed: HashedPassword,
    ): Boolean
}
