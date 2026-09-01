package br.com.autorepairshop.accessidentity.infrastructure.persistence

import br.com.autorepairshop.accessidentity.domain.aggregate.CustomerInvite
import br.com.autorepairshop.accessidentity.domain.repository.CustomerInviteRepository
import br.com.autorepairshop.accessidentity.domain.valueobject.CustomerInviteId
import org.springframework.stereotype.Repository
import java.util.UUID
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class CustomerInviteRepositoryImpl(private val jpa: CustomerInviteJpaRepository) : CustomerInviteRepository {

    override fun save(invite: CustomerInvite) {
        jpa.save(invite.toEntity())
    }

    override fun findByTokenHash(tokenHash: String): CustomerInvite? =
        jpa.findByTokenHash(tokenHash = tokenHash)?.toDomain()

    override fun findOpenByCustomerId(customerId: UUID): List<CustomerInvite> =
        jpa.findAllByCustomerIdAndConsumedAtIsNull(customerId = customerId).map { it.toDomain() }

    private fun CustomerInvite.toEntity() = CustomerInviteEntity(
        id = id.value,
        customerId = customerId,
        tokenHash = tokenHash,
        expiresAt = expiresAt.toJavaInstant(),
        consumedAt = consumedAt?.toJavaInstant(),
        createdAt = createdAt.toJavaInstant(),
    )

    private fun CustomerInviteEntity.toDomain() = CustomerInvite.rehydrate(
        id = CustomerInviteId(value = id),
        customerId = customerId,
        tokenHash = tokenHash,
        expiresAt = expiresAt.toKotlinInstant(),
        consumedAt = consumedAt?.toKotlinInstant(),
        createdAt = createdAt.toKotlinInstant(),
    )
}
