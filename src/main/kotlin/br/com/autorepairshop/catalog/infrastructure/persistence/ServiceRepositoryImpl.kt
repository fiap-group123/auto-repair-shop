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
class ServiceRepositoryImpl(private val jpa: ServiceJpaRepository) : ServiceRepository {

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

    override fun findByServiceOrderId(serviceOrderId: UUID): List<Service> =
        jpa.findAllByServiceOrderId(serviceOrderId).map { it.toDomain() }

    override fun findByServiceOrderIds(serviceOrderIds: Collection<UUID>): List<Service> {
        if (serviceOrderIds.isEmpty()) return emptyList()
        return jpa.findAllByServiceOrderIdIn(serviceOrderIds).map { it.toDomain() }
    }

    override fun existsByServiceOrderId(serviceOrderId: UUID): Boolean = jpa.existsByServiceOrderId(serviceOrderId)

    private fun Service.toEntity() = ServiceEntity(
        id = id.value,
        serviceOrderId = serviceOrderId,
        name = name.value,
        price = basePrice.amount,
        status = ServiceStatusColumn.valueOf(value = status.name),
        registeredAt = registeredAt.toJavaInstant(),
        openedAt = openedAt?.toJavaInstant(),
        finishedAt = finishedAt?.toJavaInstant(),
        estimatedTimeSeconds = estimatedTime?.inWholeSeconds,
    )

    private fun ServiceEntity.toDomain() = Service.rehydrate(
        id = ServiceId(value = id),
        serviceOrderId = serviceOrderId,
        name = ServiceName.of(raw = name),
        price = Money.of(raw = price),
        registeredAt = registeredAt.toKotlinInstant(),
        status = ServiceStatus.valueOf(value = status.name),
        openedAt = openedAt?.toKotlinInstant(),
        finishedAt = finishedAt?.toKotlinInstant(),
        estimatedTime = estimatedTimeSeconds?.seconds,
    )
}
