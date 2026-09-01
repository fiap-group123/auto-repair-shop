package br.com.autorepairshop.catalog.infrastructure.persistence

import br.com.autorepairshop.catalog.domain.aggregate.ExtraService
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceStatus
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Repository
import java.util.UUID
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class ExtraServiceRepositoryImpl(private val jpa: ExtraServiceJpaRepository) : ExtraServiceRepository {

    override fun save(extra: ExtraService) {
        jpa.save(extra.toEntity())
    }

    override fun findById(id: ExtraServiceId): ExtraService? = jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun existsByName(
        name: ServiceName,
        serviceOrderId: UUID,
    ): Boolean = jpa.existsByNameAndServiceOrderId(
        name = name.value,
        serviceOrderId = serviceOrderId,
    )

    override fun findByServiceOrderId(serviceOrderId: UUID): List<ExtraService> =
        jpa.findAllByServiceOrderId(serviceOrderId).map { it.toDomain() }

    private fun ExtraService.toEntity() = ExtraServiceEntity(
        id = id.value,
        serviceOrderId = serviceOrderId,
        name = name.value,
        price = basePrice.amount,
        status = ExtraServiceStatusColumn.valueOf(value = status.name),
        createdAt = createdAt.toJavaInstant(),
    )

    private fun ExtraServiceEntity.toDomain() = ExtraService.rehydrate(
        id = ExtraServiceId(value = id),
        serviceOrderId = serviceOrderId,
        name = ServiceName.of(raw = name),
        price = Money.of(raw = price),
        status = ExtraServiceStatus.valueOf(value = status.name),
        createdAt = createdAt.toKotlinInstant(),
    )
}
