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
import kotlin.test.assertTrue

@Tag("unit")
class ServiceOrderTest {

    @Test
    fun `open creates an empty received order and records an event`() {
        val order = ServiceOrderFixtures.received()

        assertEquals(
            expected = ServiceOrderStatus.RECEIVED,
            actual = order.status,
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

        order.updateBudgetTotal(total = ServiceOrderFixtures.TOTAL)
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

        order.finish()
        assertEquals(
            expected = ServiceOrderStatus.FINISHED,
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
    fun `rejects transitions from the wrong status`() {
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.received().finishDiagnosis()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.received().approve()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.received().finish()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.received().deliver()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.inDiagnosis().startDiagnosis()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.waitingApproval().finish()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.inExecution().approve()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.delivered().deliver()
        }
    }

    @Test
    fun `rehydrate restores status without domain events`() {
        val original = ServiceOrderFixtures.completed()
        val restored = ServiceOrder.rehydrate(
            id = original.id,
            customerId = original.customerId,
            vehicleId = original.vehicleId,
            status = original.status,
            total = original.total,
            registeredAt = original.registeredAt,
            openedAt = original.openedAt,
            finishedAt = original.finishedAt,
        )

        assertEquals(
            expected = ServiceOrderStatus.FINISHED,
            actual = restored.status,
        )
        assertTrue(restored.domainEvents.isEmpty())
        restored.deliver()
        assertEquals(
            expected = ServiceOrderStatus.DELIVERED,
            actual = restored.status,
        )
    }
}
