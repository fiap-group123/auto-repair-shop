package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
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
class RegisterBudgetUseCaseTest {
    private val budgets = mockk<BudgetRepository>()
    private val orders = mockk<ServiceOrderRepository>()
    private val services = mockk<ServiceRepository>()
    private val useCase = RegisterBudgetUseCase(
        budgets = budgets,
        orders = orders,
        services = services,
    )

    @Test
    fun `throws when the order is missing`() {
        val id = UUID.randomUUID()
        every { orders.findById(id = any()) } returns null

        assertFailsWith<BudgetException.ServiceOrderNotFound> {
            useCase.execute(input = id)
        }
        verify(exactly = 0) { budgets.save(budget = any()) }
    }

    @Test
    fun `throws when a budget already exists`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns
            BudgetFixtures.waitingApproval(serviceOrderId = order.id.value)

        assertFailsWith<BudgetException.BudgetAlreadyExists> {
            useCase.execute(input = order.id.value)
        }
        verify(exactly = 0) { budgets.save(budget = any()) }
    }

    @Test
    fun `registers a zero budget when the order has no services`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns null
        every { services.findByServiceOrderId(serviceOrderId = order.id.value) } returns emptyList()
        every { budgets.save(budget = any()) } returns Unit

        val response = useCase.execute(input = order.id.value)

        assertEquals(
            expected = Money.ZERO.amount,
            actual = response.total,
        )
        verify { budgets.save(budget = any()) }
    }

    @Test
    fun `sums existing services when the budget is created`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns null
        every { services.findByServiceOrderId(serviceOrderId = order.id.value) } returns listOf(
            CatalogFixtures.activeService(price = "120.00", serviceOrderId = order.id.value),
            CatalogFixtures.activeService(name = CatalogFixtures.OTHER_NAME, price = "30.50", serviceOrderId = order.id.value),
        )
        every { budgets.save(budget = any()) } returns Unit

        val response = useCase.execute(input = order.id.value)

        assertEquals(
            expected = "150.50",
            actual = response.total.toPlainString(),
        )
    }
}
