package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.exception.VehicleException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.serviceorder.application.dto.RegisterServiceOrderCommand
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.shared.application.event.EventPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class OpenServiceOrderUseCaseTest {
    private val customers = mockk<CustomerRepository>()
    private val vehicles = mockk<VehicleRepository>()
    private val orders = mockk<ServiceOrderRepository>()
    private val events = mockk<EventPublisher>()
    private val useCase = RegisterServiceOrderUseCase(
        customers = customers,
        vehicles = vehicles,
        orders = orders,
        events = events,
    )

    @Test
    fun `throws when the document is invalid`() {
        assertFailsWith<CustomerException.InvalidDocument> {
            useCase.execute(input = command(documentId = CustomerFixtures.INVALID_CPF))
        }
    }

    @Test
    fun `throws when customer is missing`() {
        every { customers.findByDocumentId(id = any()) } returns null

        assertFailsWith<CustomerException.CustomerNotFound> {
            useCase.execute(input = command())
        }
    }

    @Test
    fun `throws when customer is inactive`() {
        val customer = CustomerFixtures.inactiveCustomer()
        every { customers.findByDocumentId(id = customer.document) } returns customer

        assertFailsWith<CustomerException.InvalidDocument> {
            useCase.execute(input = command(documentId = CustomerFixtures.VALID_CPF))
        }
    }

    @Test
    fun `throws when the plate is invalid`() {
        val customer = CustomerFixtures.activeCustomer()
        every { customers.findByDocumentId(id = customer.document) } returns customer

        assertFailsWith<VehicleException.InvalidLicensePlate> {
            useCase.execute(input = command(vehiclePlate = "INVALID"))
        }
    }

    @Test
    fun `throws when vehicle is missing`() {
        val customer = CustomerFixtures.activeCustomer()
        every { customers.findByDocumentId(id = customer.document) } returns customer
        every { vehicles.findByPlate(plate = any()) } returns null

        assertFailsWith<VehicleException.VehicleNotFound> {
            useCase.execute(input = command())
        }
    }

    @Test
    fun `throws when vehicle belongs to another customer`() {
        val customer = CustomerFixtures.activeCustomer()
        val other = CustomerFixtures.activeCustomer(documentId = CustomerFixtures.OTHER_CPF)
        val vehicle = CustomerFixtures.vehicle(owner = other)
        every { customers.findByDocumentId(id = customer.document) } returns customer
        every { vehicles.findByPlate(plate = vehicle.plate) } returns vehicle

        assertFailsWith<ServiceOrderException.VehicleNotOwnedByCustomer> {
            useCase.execute(input = command(vehiclePlate = CustomerFixtures.PLATE))
        }
        verify(exactly = 0) { orders.save(order = any()) }
    }

    @Test
    fun `throws when the vehicle already has an open order`() {
        val customer = CustomerFixtures.activeCustomer()
        val vehicle = CustomerFixtures.vehicle(owner = customer)
        every { customers.findByDocumentId(id = customer.document) } returns customer
        every { vehicles.findByPlate(plate = vehicle.plate) } returns vehicle
        every { orders.existsOpenByVehicleId(vehicleId = vehicle.id.value) } returns true

        assertFailsWith<ServiceOrderException.OpenOrderAlreadyExists> {
            useCase.execute(input = command())
        }
        verify(exactly = 0) { orders.save(order = any()) }
    }

    @Test
    fun `opens a received order for an active owner`() {
        val customer = CustomerFixtures.activeCustomer()
        val vehicle = CustomerFixtures.vehicle(owner = customer)
        every { customers.findByDocumentId(id = customer.document) } returns customer
        every { vehicles.findByPlate(plate = vehicle.plate) } returns vehicle
        every { orders.existsOpenByVehicleId(vehicleId = vehicle.id.value) } returns false
        every { orders.save(order = any()) } returns Unit
        every { events.publish(aggregate = any()) } returns Unit

        val response = useCase.execute(input = command())

        assertEquals(
            expected = customer.id.value,
            actual = response.customerId,
        )
        assertEquals(
            expected = vehicle.id.value,
            actual = response.vehicleId,
        )
        assertEquals(
            expected = ServiceOrderStatus.RECEIVED.name,
            actual = response.status,
        )
        verify { orders.save(order = any()) }
        verify { events.publish(aggregate = any()) }
    }

    private fun command(
        documentId: String = CustomerFixtures.VALID_CPF,
        vehiclePlate: String = CustomerFixtures.PLATE,
    ) = RegisterServiceOrderCommand(
        document = documentId,
        vehiclePlate = vehiclePlate,
    )
}
