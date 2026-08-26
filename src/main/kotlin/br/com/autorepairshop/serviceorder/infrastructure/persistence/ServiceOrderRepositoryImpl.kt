package br.com.autorepairshop.serviceorder.infrastructure.persistence

import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderItem
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderTimeline
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Repository
import java.util.UUID
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

    override fun findExecuted(): List<ServiceOrder> =
        jpa.findAllByApprovedAtNotNullAndCompletedAtNotNull().map { it.toDomain() }

    private fun ServiceOrder.toEntity() = ServiceOrderEntity(
        id = id.value,
        customerId = customerId,
        vehicleId = vehicleId,
        status = ServiceOrderStatusColumn.valueOf(value = status.name),
        openedAt = openedAt.toJavaInstant(),
        items = items.map { it.toColumn() }.toMutableList(),
        diagnosisStartedAt = timeline.diagnosisStartedAt?.toJavaInstant(),
        diagnosisFinishedAt = timeline.diagnosisFinishedAt?.toJavaInstant(),
        approvedAt = timeline.approvedAt?.toJavaInstant(),
        completedAt = timeline.completedAt?.toJavaInstant(),
        deliveredAt = timeline.deliveredAt?.toJavaInstant(),
    )

    private fun ServiceOrderEntity.toDomain() = ServiceOrder.rehydrate(
        id = ServiceOrderId(value = id),
        customerId = customerId,
        vehicleId = vehicleId,
        status = ServiceOrderStatus.valueOf(value = status.name),
        openedAt = openedAt.toKotlinInstant(),
        items = items.map { it.toDomain() },
        timeline = ServiceOrderTimeline(
            diagnosisStartedAt = diagnosisStartedAt?.toKotlinInstant(),
            diagnosisFinishedAt = diagnosisFinishedAt?.toKotlinInstant(),
            approvedAt = approvedAt?.toKotlinInstant(),
            completedAt = completedAt?.toKotlinInstant(),
            deliveredAt = deliveredAt?.toKotlinInstant(),
        ),
    )

    private fun ServiceOrderItem.toColumn() = ServiceOrderItemColumn(
        offeredServiceId = offeredServiceId,
        description = description,
        unitPrice = unitPrice.amount,
        quantity = quantity,
    )

    private fun ServiceOrderItemColumn.toDomain() = ServiceOrderItem(
        offeredServiceId = offeredServiceId,
        description = description,
        unitPrice = Money.of(raw = unitPrice),
        quantity = quantity,
    )
}
