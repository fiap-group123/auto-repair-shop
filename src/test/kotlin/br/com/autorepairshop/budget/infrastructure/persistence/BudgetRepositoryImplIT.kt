package br.com.autorepairshop.budget.infrastructure.persistence

import br.com.autorepairshop.TestcontainersConfiguration
import br.com.autorepairshop.budget.BudgetFixtures
import br.com.autorepairshop.budget.domain.repositories.BudgetRepository
import br.com.autorepairshop.budget.domain.valueObject.BudgetStatus
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.serviceandexecution.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("integration")
@Import(TestcontainersConfiguration::class)
@SpringBootTest
class BudgetRepositoryImplIT {

    @Autowired
    private lateinit var customers: CustomerRepository

    @Autowired
    private lateinit var vehicles: VehicleRepository

    @Autowired
    private lateinit var orders: ServiceOrderRepository

    @Autowired
    private lateinit var budgets: BudgetRepository

    @Test
    fun `persists and reloads a budget`() {
        val customer = CustomerFixtures.activeCustomer(documentId = CustomerFixtures.OTHER_CPF)
        val vehicle = CustomerFixtures.vehicle(owner = customer, plate = "DEF2E34")
        customers.save(customer = customer)
        vehicles.save(vehicle = vehicle)
        val order = ServiceOrder.open(
            customerId = customer.id.value,
            vehicleId = vehicle.id.value,
        )
        orders.save(order = order)

        val budget = BudgetFixtures.waitingApproval(serviceOrderId = order.id.value)
        budgets.save(budget)

        val loaded = budgets.findByServiceOrderId(serviceOrderId = order.id.value)
        assertEquals(
            expected = budget.id,
            actual = loaded?.id,
        )
        assertEquals(
            expected = BudgetFixtures.TOTAL,
            actual = loaded?.total,
        )
        assertEquals(
            expected = BudgetStatus.WAITING_APPROVAL,
            actual = loaded?.status,
        )

        budgets.deleteByServiceOrderId(serviceOrderId = order.id.value)
        assertNull(budgets.findByServiceOrderId(serviceOrderId = order.id.value))
    }
}
