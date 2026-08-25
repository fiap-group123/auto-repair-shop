package br.com.autorepairshop.authentication.domain.repository

import br.com.autorepairshop.authentication.domain.aggregate.User
import br.com.autorepairshop.authentication.domain.valueobject.LoginEmail
import br.com.autorepairshop.authentication.domain.valueobject.UserId

interface UserRepository {
    fun save(user: User)
    fun findById(id: UserId): User?
    fun findByEmail(email: LoginEmail): User?
    fun existsByEmail(email: LoginEmail): Boolean
    fun existsAny(): Boolean
}
