package br.com.autorepairshop.accessidentity.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun findByCustomerId(customerId: UUID): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun existsByCustomerId(customerId: UUID): Boolean
}
