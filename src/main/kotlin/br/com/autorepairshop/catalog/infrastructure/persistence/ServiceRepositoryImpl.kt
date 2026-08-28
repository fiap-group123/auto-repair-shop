package br.com.autorepairshop.catalog.infrastructure.persistence

import br.com.autorepairshop.catalog.domain.aggregate.Service
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.catalog.domain.valueobject.ServiceStatus
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Repository
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class OfferedServiceRepositoryImpl(private val jpa: OfferedServiceJpaRepository) : ServiceRepository {

    override fun save(service: Service) {
        jpa.save(service.toEntity())
    }

    override fun findById(id: ServiceId): Service? = jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun existsByName(
        name: ServiceName,
        serviceOrderId: UUID,
    ): Boolean = jpa.existsByNameAndServiceOrderId(
        name = name.value,
        serviceOrderId = serviceOrderId,
    )

    override fun findAll(): List<Service> = jpa.findAll().map { it.toDomain() }

    override fun findByIds(ids: Collection<UUID>): List<Service> {
        if (ids.isEmpty()) return emptyList()
        return jpa.findAllById(ids).map { it.toDomain() }
    }

    private fun Service.toEntity() = OfferedServiceEntity(
        id = id.value,
        serviceOrderId = serviceOrderId,
        name = name.value,
        price = basePrice.amount,
        status = ServiceStatusColumn.valueOf(value = status.name),
        active = active,
        registeredAt = registeredAt.toJavaInstant(),
        openedAt = openedAt?.toJavaInstant(),
        finishedAt = finishedAt?.toJavaInstant(),
        estimatedTimeSeconds = estimatedTime?.inWholeSeconds,
    )

    private fun OfferedServiceEntity.toDomain() = Service.rehydrate(
        id = ServiceId(value = id),
        serviceOrderId = serviceOrderId,
        name = ServiceName.of(raw = name),
        price = Money.of(raw = price),
        active = active,
        registeredAt = registeredAt.toKotlinInstant(),
        status = ServiceStatus.valueOf(value = status.name),
        openedAt = openedAt?.toKotlinInstant(),
        finishedAt = finishedAt?.toKotlinInstant(),
        estimatedTime = estimatedTimeSeconds?.seconds,
    )
}
