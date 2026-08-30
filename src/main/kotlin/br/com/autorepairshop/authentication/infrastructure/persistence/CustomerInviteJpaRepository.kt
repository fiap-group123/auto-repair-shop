package br.com.autorepairshop.authentication.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CustomerInviteJpaRepository : JpaRepository<CustomerInviteEntity, UUID> {
    fun findByTokenHash(tokenHash: String): CustomerInviteEntity?
    fun findAllByCustomerIdAndConsumedAtIsNull(customerId: UUID): List<CustomerInviteEntity>
}
