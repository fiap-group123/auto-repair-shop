package br.com.autorepairshop.authentication.domain.repository

import br.com.autorepairshop.authentication.domain.aggregate.User
import br.com.autorepairshop.authentication.domain.valueobject.LoginEmail
import br.com.autorepairshop.authentication.domain.valueobject.UserId
import java.util.UUID

interface UserRepository {
    fun save(user: User)
    fun findById(id: UserId): User?
    fun findByEmail(email: LoginEmail): User?
    fun existsByEmail(email: LoginEmail): Boolean
    fun existsByCustomerId(customerId: UUID): Boolean
    fun existsAny(): Boolean
}
