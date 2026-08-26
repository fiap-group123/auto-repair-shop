package br.com.autorepairshop.serviceorder.domain.aggregate

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.event.DiagnosisFinished
import br.com.autorepairshop.serviceorder.domain.event.DiagnosisStarted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderApproved
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderCompleted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderDelivered
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderOpened
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Tag("unit")
class ServiceOrderTest {

    @Test
    fun `open creates an empty received order and records an event`() {
        val order = ServiceOrderFixtures.received()

        assertEquals(
            expected = ServiceOrderStatus.RECEIVED,
            actual = order.status,
        )
        assertTrue(order.items.isEmpty())
        assertEquals(
            expected = "0.00",
            actual = order.total().toString(),
        )
        assertTrue(order.domainEvents.single() is ServiceOrderOpened)
    }

    @Test
    fun `advances through the full status cycle`() {
        val order = ServiceOrderFixtures.received()
        order.startDiagnosis()
        assertEquals(
            expected = ServiceOrderStatus.IN_DIAGNOSIS,
            actual = order.status,
        )
        assertTrue(order.domainEvents.last() is DiagnosisStarted)

        order.addItem(item = ServiceOrderFixtures.item())
        order.finishDiagnosis()
        assertEquals(
            expected = ServiceOrderStatus.WAITING_APPROVAL,
            actual = order.status,
        )
        assertTrue(order.domainEvents.last() is DiagnosisFinished)

        order.approve()
        assertEquals(
            expected = ServiceOrderStatus.IN_EXECUTION,
            actual = order.status,
        )
        assertTrue(order.domainEvents.last() is ServiceOrderApproved)

        order.complete()
        assertEquals(
            expected = ServiceOrderStatus.COMPLETED,
            actual = order.status,
        )
        assertTrue(order.domainEvents.last() is ServiceOrderCompleted)

        order.deliver()
        assertEquals(
            expected = ServiceOrderStatus.DELIVERED,
            actual = order.status,
        )
        assertTrue(order.domainEvents.last() is ServiceOrderDelivered)
    }

    @Test
    fun `records a timestamp for every transition`() {
        val order = ServiceOrderFixtures.received()
        order.startDiagnosis(at = at(hour = 1))
        order.addItem(item = ServiceOrderFixtures.item())
        order.finishDiagnosis(at = at(hour = 2))
        order.approve(at = at(hour = 3))
        order.complete(at = at(hour = 5))
        order.deliver(at = at(hour = 6))

        assertEquals(
            expected = at(hour = 1),
            actual = order.timeline.diagnosisStartedAt,
        )
        assertEquals(
            expected = at(hour = 2),
            actual = order.timeline.diagnosisFinishedAt,
        )
        assertEquals(
            expected = at(hour = 3),
            actual = order.timeline.approvedAt,
        )
        assertEquals(
            expected = at(hour = 5),
            actual = order.timeline.completedAt,
        )
        assertEquals(
            expected = at(hour = 6),
            actual = order.timeline.deliveredAt,
        )
        assertEquals(
            expected = 2.hours,
            actual = order.executionDuration(),
        )
    }

    @Test
    fun `execution duration is unknown until the order is completed`() {
        assertNull(ServiceOrderFixtures.received().executionDuration())
        assertNull(ServiceOrderFixtures.inExecution().executionDuration())
    }

    @Test
    fun `rejects transitions from the wrong status`() {
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.received().finishDiagnosis()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.received().approve()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.received().complete()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.received().deliver()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.inDiagnosis().startDiagnosis()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.waitingApproval().complete()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.inExecution().approve()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.delivered().deliver()
        }
    }

    @Test
    fun `rehydrate restores items and timeline without domain events`() {
        val original = ServiceOrderFixtures.completed()
        val restored = ServiceOrder.rehydrate(
            id = original.id,
            customerId = original.customerId,
            vehicleId = original.vehicleId,
            status = original.status,
            openedAt = original.openedAt,
            items = original.items,
            timeline = original.timeline,
        )

        assertEquals(
            expected = ServiceOrderStatus.COMPLETED,
            actual = restored.status,
        )
        assertEquals(
            expected = original.total().toString(),
            actual = restored.total().toString(),
        )
        assertTrue(restored.domainEvents.isEmpty())
        restored.deliver()
        assertEquals(
            expected = ServiceOrderStatus.DELIVERED,
            actual = restored.status,
        )
    }

    private fun at(hour: Int): Instant = Instant.fromEpochSeconds(epochSeconds = hour.toLong() * 3600)
}
