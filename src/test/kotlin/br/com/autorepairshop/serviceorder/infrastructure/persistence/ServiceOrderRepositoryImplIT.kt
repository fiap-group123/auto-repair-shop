package br.com.autorepairshop.serviceorder.infrastructure.persistence

import br.com.autorepairshop.TestcontainersConfiguration
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Tag("integration")
@Import(TestcontainersConfiguration::class)
@SpringBootTest
class ServiceOrderRepositoryImplIT {

    @Autowired
    private lateinit var customers: CustomerRepository

    @Autowired
    private lateinit var vehicles: VehicleRepository

    @Autowired
    private lateinit var orders: ServiceOrderRepository

    /** Every status must survive a round trip, otherwise the enum column and the domain enum drifted apart. */
    @Test
    fun `persists every status of the service order lifecycle`() {
        val customer = CustomerFixtures.activeCustomer()
        val vehicle = CustomerFixtures.vehicle(owner = customer)
        customers.save(customer = customer)
        vehicles.save(vehicle = vehicle)

        val order = ServiceOrder.open(
            customerId = customer.id.value,
            vehicleId = vehicle.id.value,
        )
        assertStatusAfterReload(
            order = order,
            expected = ServiceOrderStatus.RECEIVED,
        )

        order.startDiagnosis()
        assertStatusAfterReload(
            order = order,
            expected = ServiceOrderStatus.IN_DIAGNOSIS,
        )
        assertNotNull(orders.findById(id = order.id)?.startedAt)

        order.finishDiagnosis()
        assertStatusAfterReload(
            order = order,
            expected = ServiceOrderStatus.WAITING_APPROVAL,
        )

        order.budgetApprove()
        assertStatusAfterReload(
            order = order,
            expected = ServiceOrderStatus.BUDGET_APPROVED,
        )

        order.startExecution()
        assertStatusAfterReload(
            order = order,
            expected = ServiceOrderStatus.IN_EXECUTION,
        )

        order.finish()
        assertStatusAfterReload(
            order = order,
            expected = ServiceOrderStatus.FINISHED,
        )
        assertNotNull(orders.findById(id = order.id)?.finishedAt)

        order.deliver()
        assertStatusAfterReload(
            order = order,
            expected = ServiceOrderStatus.DELIVERED,
        )

        val rejectedVehicle = CustomerFixtures.vehicle(
            owner = customer,
            plate = CustomerFixtures.NATIONAL_PLATE,
        )
        vehicles.save(vehicle = rejectedVehicle)
        val rejected = ServiceOrder.open(
            customerId = customer.id.value,
            vehicleId = rejectedVehicle.id.value,
        )
        rejected.startDiagnosis()
        rejected.finishDiagnosis()
        rejected.budgetReject()
        assertStatusAfterReload(
            order = rejected,
            expected = ServiceOrderStatus.BUDGET_REJECTED,
        )
    }

    private fun assertStatusAfterReload(
        order: ServiceOrder,
        expected: ServiceOrderStatus,
    ) {
        orders.save(order = order)
        assertEquals(
            expected = expected,
            actual = orders.findById(id = order.id)?.status,
        )
    }
}
