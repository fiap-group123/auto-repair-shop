package br.com.autorepairshop.serviceorder.domain.aggregate

import br.com.autorepairshop.serviceorder.domain.event.DiagnosisFinished
import br.com.autorepairshop.serviceorder.domain.event.DiagnosisStarted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderApproved
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderBudgetRejected
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderCompleted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderDelivered
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderOpened
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.shared.domain.AggregateRoot
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class ServiceOrder private constructor(
    id: ServiceOrderId,
    val customerId: UUID,
    val vehicleId: UUID,
    status: ServiceOrderStatus,
    val createdAt: Instant,
    startedAt: Instant?,
    finishedAt: Instant?,
    estimatedTime: Duration?,
) : AggregateRoot<ServiceOrderId>(id = id) {

    var status: ServiceOrderStatus = status
        private set

    var startedAt: Instant? = startedAt
        private set

    var finishedAt: Instant? = finishedAt
        private set

    var estimatedTime: Duration? = estimatedTime
        private set

    fun startDiagnosis(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.RECEIVED)
        status = ServiceOrderStatus.IN_DIAGNOSIS
        startedAt = at
        registerEvent(
            event = DiagnosisStarted(
                serviceOrderId = id.value,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun finishDiagnosis(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.IN_DIAGNOSIS)
        status = ServiceOrderStatus.WAITING_APPROVAL
        registerEvent(
            event = DiagnosisFinished(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun budgetApprove(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.WAITING_APPROVAL)
        status = ServiceOrderStatus.BUDGET_APPROVED
        registerEvent(
            event = ServiceOrderApproved(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun budgetReject(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.WAITING_APPROVAL)
        status = ServiceOrderStatus.BUDGET_REJECTED
        registerEvent(
            event = ServiceOrderBudgetRejected(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun startExecution() {
        requireStatus(expected = ServiceOrderStatus.BUDGET_APPROVED)
        status = ServiceOrderStatus.IN_EXECUTION
    }

    fun finish(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.IN_EXECUTION)
        status = ServiceOrderStatus.FINISHED
        finishedAt = at
        startedAt?.let { started ->
            estimateTime(duration = at - started)
        }
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

    private fun estimateTime(duration: Duration) {
        if (duration.isNegative()) {
            throw ServiceOrderException.InvalidDuration(
                message = "Duration cannot be negative.",
            )
        }
        estimatedTime = duration
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
            createdAt: Instant = Clock.System.now(),
            startedAt: Instant? = null,
            finishedAt: Instant? = null,
            estimatedTime: Duration? = null,
        ): ServiceOrder {
            val order = ServiceOrder(
                id = ServiceOrderId.generate(),
                customerId = customerId,
                vehicleId = vehicleId,
                status = status,
                createdAt = createdAt,
                startedAt = startedAt,
                finishedAt = finishedAt,
                estimatedTime = estimatedTime,
            )
            order.recordOpened(at = createdAt)
            return order
        }

        internal fun rehydrate(
            id: ServiceOrderId,
            customerId: UUID,
            vehicleId: UUID,
            status: ServiceOrderStatus,
            createdAt: Instant,
            startedAt: Instant?,
            finishedAt: Instant?,
            estimateTime: Duration?,
        ) = ServiceOrder(
            id = id,
            customerId = customerId,
            vehicleId = vehicleId,
            status = status,
            createdAt = createdAt,
            startedAt = startedAt,
            finishedAt = finishedAt,
            estimatedTime = estimateTime,
        )
    }
}
