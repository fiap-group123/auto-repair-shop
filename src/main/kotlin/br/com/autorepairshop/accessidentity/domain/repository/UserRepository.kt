package br.com.autorepairshop.accessidentity.domain.repository

import br.com.autorepairshop.accessidentity.domain.aggregate.User
import br.com.autorepairshop.accessidentity.domain.valueobject.LoginEmail
import br.com.autorepairshop.accessidentity.domain.valueobject.UserId
import java.util.UUID

interface UserRepository {
    fun save(user: User)
    fun findById(id: UserId): User?
    fun findByEmail(email: LoginEmail): User?
    fun findByCustomerId(customerId: UUID): User?
    fun existsByEmail(email: LoginEmail): Boolean
    fun existsByCustomerId(customerId: UUID): Boolean
    fun existsAny(): Boolean
}
