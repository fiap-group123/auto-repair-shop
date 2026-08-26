package br.com.autorepairshop.serviceorder.domain.aggregate

import br.com.autorepairshop.serviceorder.domain.event.DiagnosisFinished
import br.com.autorepairshop.serviceorder.domain.event.DiagnosisStarted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderApproved
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderCompleted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderDelivered
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderOpened
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderItem
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderTimeline
import br.com.autorepairshop.shared.domain.AggregateRoot
import br.com.autorepairshop.shared.domain.Money
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
    val openedAt: Instant,
    items: List<ServiceOrderItem>,
    timeline: ServiceOrderTimeline,
) : AggregateRoot<ServiceOrderId>(id = id) {

    private val mutableItems: MutableList<ServiceOrderItem> = items.toMutableList()

    var status: ServiceOrderStatus = status
        private set

    var timeline: ServiceOrderTimeline = timeline
        private set

    val items: List<ServiceOrderItem> get() = mutableItems.toList()

    /** Budget of the order: sum of every requested service. */
    fun total(): Money = mutableItems.fold(initial = Money.ZERO) { acc, item -> acc.plus(item.subtotal()) }

    fun executionDuration(): Duration? = timeline.executionDuration()

    fun addItem(item: ServiceOrderItem) {
        requireItemsEditable()
        if (mutableItems.any { it.offeredServiceId == item.offeredServiceId }) {
            throw ServiceOrderException.ItemAlreadyAdded(
                message = "Service ${item.offeredServiceId} is already in this order.",
            )
        }
        mutableItems.add(element = item)
    }

    fun removeItem(offeredServiceId: UUID) {
        requireItemsEditable()
        val removed = mutableItems.removeAll { it.offeredServiceId == offeredServiceId }
        if (!removed) {
            throw ServiceOrderException.ItemNotFound(
                message = "Service $offeredServiceId is not in this order.",
            )
        }
    }

    fun startDiagnosis(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.RECEIVED)
        status = ServiceOrderStatus.IN_DIAGNOSIS
        timeline = timeline.copy(diagnosisStartedAt = at)
        registerEvent(
            event = DiagnosisStarted(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun finishDiagnosis(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.IN_DIAGNOSIS)
        if (mutableItems.isEmpty()) {
            throw ServiceOrderException.EmptyBudget(
                message = "Cannot send an empty budget for approval.",
            )
        }
        status = ServiceOrderStatus.WAITING_APPROVAL
        timeline = timeline.copy(diagnosisFinishedAt = at)
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
        timeline = timeline.copy(approvedAt = at)
        registerEvent(
            event = ServiceOrderApproved(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun complete(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.IN_EXECUTION)
        status = ServiceOrderStatus.COMPLETED
        timeline = timeline.copy(completedAt = at)
        registerEvent(
            event = ServiceOrderCompleted(
                serviceOrderId = id,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun deliver(at: Instant = Clock.System.now()) {
        requireStatus(expected = ServiceOrderStatus.COMPLETED)
        status = ServiceOrderStatus.DELIVERED
        timeline = timeline.copy(deliveredAt = at)
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

    private fun requireItemsEditable() {
        val editable = status == ServiceOrderStatus.RECEIVED || status == ServiceOrderStatus.IN_DIAGNOSIS
        if (!editable) {
            throw ServiceOrderException.ItemsLocked(
                message = "Items cannot change in status ${status.name}.",
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
            at: Instant = Clock.System.now(),
        ): ServiceOrder {
            val order = ServiceOrder(
                id = ServiceOrderId.generate(),
                customerId = customerId,
                vehicleId = vehicleId,
                status = ServiceOrderStatus.RECEIVED,
                openedAt = at,
                items = emptyList(),
                timeline = ServiceOrderTimeline(),
            )
            order.recordOpened(at = at)
            return order
        }

        internal fun rehydrate(
            id: ServiceOrderId,
            customerId: UUID,
            vehicleId: UUID,
            status: ServiceOrderStatus,
            openedAt: Instant,
            items: List<ServiceOrderItem>,
            timeline: ServiceOrderTimeline,
        ) = ServiceOrder(
            id = id,
            customerId = customerId,
            vehicleId = vehicleId,
            status = status,
            openedAt = openedAt,
            items = items,
            timeline = timeline,
        )
    }
}
