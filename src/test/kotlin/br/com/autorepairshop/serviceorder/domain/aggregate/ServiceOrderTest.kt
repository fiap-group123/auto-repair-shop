package br.com.autorepairshop.serviceorder.domain.aggregate

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.event.DiagnosisFinished
import br.com.autorepairshop.serviceorder.domain.event.DiagnosisStarted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderApproved
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderBudgetRejected
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderCompleted
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderDelivered
import br.com.autorepairshop.serviceorder.domain.event.ServiceOrderExecutionStarted
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

        order.finishDiagnosis()
        assertEquals(
            expected = ServiceOrderStatus.WAITING_APPROVAL,
            actual = order.status,
        )
        assertTrue(order.domainEvents.last() is DiagnosisFinished)

        order.budgetApprove()
        assertEquals(
            expected = ServiceOrderStatus.BUDGET_APPROVED,
            actual = order.status,
        )
        assertTrue(order.domainEvents.last() is ServiceOrderApproved)

        order.startExecution()
        assertEquals(
            expected = ServiceOrderStatus.IN_EXECUTION,
            actual = order.status,
        )
        assertTrue(order.domainEvents.last() is ServiceOrderExecutionStarted)

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
            ServiceOrderFixtures.received().budgetApprove()
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
            ServiceOrderFixtures.waitingApproval().startExecution()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.inExecution().budgetApprove()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.inExecution().budgetReject()
        }
        assertFailsWith<ServiceOrderException.InvalidStatusTransition> {
            ServiceOrderFixtures.delivered().deliver()
        }
    }

    @Test
    fun `budgetReject records the event and marks the order as rejected`() {
        val order = ServiceOrderFixtures.waitingApproval()

        order.budgetReject()

        assertEquals(
            expected = ServiceOrderStatus.BUDGET_REJECTED,
            actual = order.status,
        )
        assertTrue(order.domainEvents.last() is ServiceOrderBudgetRejected)
    }

    @Test
    fun `rehydrate restores status without domain events`() {
        val original = ServiceOrderFixtures.completed()
        val restored = ServiceOrder.rehydrate(
            id = original.id,
            customerId = original.customerId,
            vehicleId = original.vehicleId,
            status = original.status,
            createdAt = original.createdAt,
            startedAt = original.startedAt,
            finishedAt = original.finishedAt,
            estimateTime = original.estimatedTime,
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
