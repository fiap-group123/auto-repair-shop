package br.com.autorepairshop.serviceorder.domain.aggregate

import br.com.autorepairshop.serviceorder.domain.event.DiagnosisFinished
import br.com.autorepairshop.serviceorder.domain.event.DiagnosisStarted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderApproved
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderCompleted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderDelivered
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderOpened
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.shared.domain.AggregateRoot
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class ServiceOrder private constructor(
    id: ServiceOrderId,
    val customerId: UUID,
    val vehicleId: UUID,
    status: ServiceOrderStatus,
    val registeredAt: Instant,
    openedAt: Instant?,
    finishedAt: Instant?,
) : AggregateRoot<ServiceOrderId>(id = id) {

    var status: ServiceOrderStatus = status
        private set

    var openedAt: Instant? = openedAt
        private set

    var finishedAt: Instant? = finishedAt
        private set

    fun requireItemsEditable() {
        val editable = status == ServiceOrderStatus.RECEIVED || status == ServiceOrderStatus.IN_DIAGNOSIS
        if (!editable) {
            throw ServiceOrderException.ItemsLocked(
                message = "Items cannot change in status ${status.name}.",
            )
        }
    }

    fun startDiagnosis(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.RECEIVED)
        status = ServiceOrderStatus.IN_DIAGNOSIS
        openedAt = at
        registerEvent(
            event = DiagnosisStarted(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun finishDiagnosis(
        hasServices: Boolean,
        at: Instant = Clock.System.now(),
    ) {
        requireStatus(expected = ServiceOrderStatus.IN_DIAGNOSIS)
        if (!hasServices) {
            throw ServiceOrderException.EmptyBudget(
                message = "Cannot send an empty budget for approval.",
            )
        }
        status = ServiceOrderStatus.WAITING_APPROVAL
        registerEvent(
            event = DiagnosisFinished(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun approve(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.WAITING_APPROVAL)
        status = ServiceOrderStatus.IN_EXECUTION
        registerEvent(
            event = ServiceOrderApproved(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun finish(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.IN_EXECUTION)
        status = ServiceOrderStatus.FINISHED
        finishedAt = at
        registerEvent(
            event = ServiceOrderCompleted(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun deliver(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.FINISHED)
        status = ServiceOrderStatus.DELIVERED
        registerEvent(
            event = ServiceOrderDelivered(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    private fun requireStatus(expected: ServiceOrderStatus) {
        if (status != expected) {
            throw ServiceOrderException.InvalidStatusTransition(
                message = "Cannot transition from ${status.name}.",
            )
        }
    }

    private fun recordOpened(at: Instant) {
        registerEvent(
            event = ServiceOrderOpened(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    companion object {
        fun open(
            customerId: UUID,
            vehicleId: UUID,
            status: ServiceOrderStatus = ServiceOrderStatus.RECEIVED,
            registeredAt: Instant = Clock.System.now(),
            openedAt: Instant? = null,
            finishedAt: Instant? = null,
        ): ServiceOrder {
            val order = ServiceOrder(
                id = ServiceOrderId.generate(),
                customerId = customerId,
                vehicleId = vehicleId,
                status = status,
                registeredAt = registeredAt,
                openedAt = openedAt,
                finishedAt = finishedAt,
            )
            order.recordOpened(at = registeredAt)
            return order
        }

        internal fun rehydrate(
            id: ServiceOrderId,
            customerId: UUID,
            vehicleId: UUID,
            status: ServiceOrderStatus,
            registeredAt: Instant,
            openedAt: Instant?,
            finishedAt: Instant?,
        ) = ServiceOrder(
            id = id,
            customerId = customerId,
            vehicleId = vehicleId,
            status = status,
            registeredAt = registeredAt,
            openedAt = openedAt,
            finishedAt = finishedAt,
        )
    }
}
