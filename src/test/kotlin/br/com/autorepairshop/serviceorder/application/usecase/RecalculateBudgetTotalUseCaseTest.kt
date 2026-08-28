package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.domain.Money
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class RecalculateBudgetTotalUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val services = mockk<ServiceRepository>()
    private val useCase = RecalculateBudgetTotalUseCase(
        orders = orders,
        services = services,
    )

    @Test
    fun `throws when the order is missing`() {
        val id = UUID.randomUUID()
        every { orders.findById(id = ServiceOrderId(value = id)) } returns null

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `sums the prices of the services the order owns`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { services.findByServiceOrderId(serviceOrderId = order.id.value) } returns listOf(
            element = CatalogFixtures.activeService(price = "10.00"),
        ).plus(element = CatalogFixtures.activeService(price = "20.50"))
        every { orders.save(order = order) } returns Unit

        useCase.execute(input = order.id.value)

        assertEquals(
            expected = "30.50",
            actual = order.total.amount.toPlainString(),
        )
        verify { orders.save(order = order) }
    }

    /** Recomputing must converge, not accumulate, so replaying the same event changes nothing. */
    @Test
    fun `replaying the event keeps the same total`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { services.findByServiceOrderId(serviceOrderId = order.id.value) } returns listOf(
            element = CatalogFixtures.activeService(price = "10.00"),
        )
        every { orders.save(order = order) } returns Unit

        useCase.execute(input = order.id.value)
        useCase.execute(input = order.id.value)

        assertEquals(
            expected = "10.00",
            actual = order.total.amount.toPlainString(),
        )
    }

    @Test
    fun `leaves the total untouched once the budget was sent for approval`() {
        val order = ServiceOrderFixtures.waitingApproval()
        every { orders.findById(id = order.id) } returns order

        useCase.execute(input = order.id.value)

        assertEquals(
            expected = ServiceOrderFixtures.TOTAL,
            actual = order.total,
        )
        verify(exactly = 0) { orders.save(order = any()) }
        verify(exactly = 0) { services.findByServiceOrderId(serviceOrderId = any()) }
    }

    @Test
    fun `an order without services is worth zero`() {
        val order = ServiceOrderFixtures.inDiagnosisWithBudget()
        every { orders.findById(id = order.id) } returns order
        every { services.findByServiceOrderId(serviceOrderId = order.id.value) } returns emptyList()
        every { orders.save(order = order) } returns Unit

        useCase.execute(input = order.id.value)

        assertEquals(
            expected = Money.ZERO,
            actual = order.total,
        )
    }
}
