package br.com.autorepairshop.budget.application.usecase

import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.domain.exception.BudgetException
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.inputmanagment.InventoryFixtures
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
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
class CalculateBudgetTotalUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val services = mockk<ServiceRepository>()
    private val extras = mockk<ExtraServiceRepository>()
    private val parts = mockk<PartRepository>()
    private val budgets = mockk<BudgetRepository>()
    private val useCase = CalculateBudgetTotalUseCase(
        orders = orders,
        services = services,
        extras = extras,
        parts = parts,
        budgets = budgets,
    )

    @Test
    fun `throws when the order is missing`() {
        val id = UUID.randomUUID()
        every { orders.findById(id = ServiceOrderId(value = id)) } returns null

        assertFailsWith<BudgetException.ServiceOrderNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `does nothing when the budget does not exist yet`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns null

        useCase.execute(input = order.id.value)

        verify(exactly = 0) { budgets.save(budget = any()) }
    }

    @Test
    fun `persists zero when every service was removed`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        val budget = BudgetFixtures.waitingApproval(serviceOrderId = order.id.value)
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns budget
        every { services.findByServiceOrderId(serviceOrderId = order.id.value) } returns emptyList()
        every { extras.findByServiceOrderId(serviceOrderId = order.id.value) } returns emptyList()
        every { parts.findByServiceOrderId(serviceOrderId = order.id.value) } returns emptyList()
        every { budgets.save(budget = budget) } returns Unit

        useCase.execute(input = order.id.value)

        assertEquals(
            expected = Money.ZERO,
            actual = budget.total,
        )
        verify { budgets.save(budget = budget) }
    }

    @Test
    fun `sums catalog prices into the budget`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        val budget = BudgetFixtures.waitingApproval(serviceOrderId = order.id.value, total = Money.ZERO)
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns budget
        every { services.findByServiceOrderId(serviceOrderId = order.id.value) } returns listOf(
            CatalogFixtures.activeService(price = "80.00", serviceOrderId = order.id.value),
            CatalogFixtures.activeService(name = CatalogFixtures.OTHER_NAME, price = "20.00", serviceOrderId = order.id.value),
        )
        every { extras.findByServiceOrderId(serviceOrderId = order.id.value) } returns emptyList()
        every { parts.findByServiceOrderId(serviceOrderId = order.id.value) } returns emptyList()
        every { budgets.save(budget = budget) } returns Unit

        useCase.execute(input = order.id.value)

        assertEquals(
            expected = "100.00",
            actual = budget.total.amount.toPlainString(),
        )
    }

    @Test
    fun `adds only approved extras to the budget total`() {
        val order = ServiceOrderFixtures.budgetApproved()
        val budget = BudgetFixtures.waitingApproval(serviceOrderId = order.id.value, total = Money.ZERO)
        val pending = CatalogFixtures.extraService(name = "Pendente", serviceOrderId = order.id.value)
        val approved = CatalogFixtures.extraService(name = "Aprovado", price = "30.00", serviceOrderId = order.id.value)
        approved.approve()
        approved.inProgress()
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns budget
        every { services.findByServiceOrderId(serviceOrderId = order.id.value) } returns listOf(
            CatalogFixtures.activeService(price = "80.00", serviceOrderId = order.id.value),
        )
        every { extras.findByServiceOrderId(serviceOrderId = order.id.value) } returns
            listOf(element = pending).plus(element = approved)
        every { parts.findByServiceOrderId(serviceOrderId = order.id.value) } returns emptyList()
        every { budgets.save(budget = budget) } returns Unit

        useCase.execute(input = order.id.value)

        assertEquals(
            expected = "110.00",
            actual = budget.total.amount.toPlainString(),
        )
    }

    @Test
    fun `adds parts line totals to the budget`() {
        val order = ServiceOrderFixtures.inDiagnosis()
        val budget = BudgetFixtures.waitingApproval(serviceOrderId = order.id.value, total = Money.ZERO)
        val inventory = InventoryFixtures.inventory(price = "10.00")
        every { orders.findById(id = order.id) } returns order
        every { budgets.findByServiceOrderId(serviceOrderId = order.id.value) } returns budget
        every { services.findByServiceOrderId(serviceOrderId = order.id.value) } returns listOf(
            CatalogFixtures.activeService(price = "80.00", serviceOrderId = order.id.value),
        )
        every { extras.findByServiceOrderId(serviceOrderId = order.id.value) } returns emptyList()
        every { parts.findByServiceOrderId(serviceOrderId = order.id.value) } returns listOf(
            InventoryFixtures.part(
                serviceOrderId = order.id.value,
                inventory = inventory,
                quantity = 2,
            ),
        )
        every { budgets.save(budget = budget) } returns Unit

        useCase.execute(input = order.id.value)

        assertEquals(
            expected = "100.00",
            actual = budget.total.amount.toPlainString(),
        )
    }
}
