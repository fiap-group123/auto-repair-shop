package br.com.autorepairshop.serviceorder.infrastructure.persistence

import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Repository
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class ServiceOrderRepositoryImpl(private val jpa: ServiceOrderJpaRepository) : ServiceOrderRepository {

    override fun save(order: ServiceOrder) {
        jpa.save(order.toEntity())
    }

    override fun findById(id: ServiceOrderId): ServiceOrder? = jpa.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findAll(): List<ServiceOrder> = jpa.findAll().map { it.toDomain() }

    override fun findByCustomerId(customerId: UUID): List<ServiceOrder> =
        jpa.findAllByCustomerId(customerId = customerId).map { it.toDomain() }

    override fun existsOpenByVehicleId(vehicleId: UUID): Boolean = jpa.existsByVehicleIdAndStatusNot(
        vehicleId = vehicleId,
        status = ServiceOrderStatusColumn.DELIVERED,
    )

    private fun ServiceOrder.toEntity() = ServiceOrderEntity(
        id = id.value,
        customerId = customerId,
        vehicleId = vehicleId,
        status = ServiceOrderStatusColumn.valueOf(value = status.name),
        total = total.amount,
        registeredAt = registeredAt.toJavaInstant(),
        openedAt = openedAt?.toJavaInstant(),
        finishedAt = finishedAt?.toJavaInstant(),
        estimateTimeSeconds = estimateTime?.inWholeSeconds,
    )

    private fun ServiceOrderEntity.toDomain() = ServiceOrder.rehydrate(
        id = ServiceOrderId(value = id),
        customerId = customerId,
        vehicleId = vehicleId,
        status = ServiceOrderStatus.valueOf(value = status.name),
        total = Money.of(raw = total),
        registeredAt = registeredAt.toKotlinInstant(),
        openedAt = openedAt?.toKotlinInstant(),
        finishedAt = finishedAt?.toKotlinInstant(),
        estimateTime = estimateTimeSeconds?.seconds,
    )
}
