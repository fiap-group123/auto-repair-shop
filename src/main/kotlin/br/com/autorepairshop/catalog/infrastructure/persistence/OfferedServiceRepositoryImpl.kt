package br.com.autorepairshop.catalog.infrastructure.persistence

import br.com.autorepairshop.catalog.domain.aggregate.OfferedService
import br.com.autorepairshop.catalog.domain.repository.OfferedServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.OfferedServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Repository
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class OfferedServiceRepositoryImpl(private val jpa: OfferedServiceJpaRepository) : OfferedServiceRepository {

    override fun save(service: OfferedService) {
        jpa.save(service.toEntity())
    }

    override fun findById(id: OfferedServiceId): OfferedService? =
        jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun existsByName(name: ServiceName): Boolean = jpa.existsByName(name = name.value)

    override fun findAll(): List<OfferedService> = jpa.findAll().map { it.toDomain() }

    private fun OfferedService.toEntity() = OfferedServiceEntity(
        id = id.value,
        name = name.value,
        price = price.amount,
        active = active,
        registeredAt = registeredAt.toJavaInstant(),
    )

    private fun OfferedServiceEntity.toDomain() = OfferedService.rehydrate(
        id = OfferedServiceId(value = id),
        name = ServiceName.of(raw = name),
        price = Money.of(raw = price),
        active = active,
        registeredAt = registeredAt.toKotlinInstant(),
    )
}
