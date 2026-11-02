package br.com.autorepairshop.serviceandexecution.application.usecase

import br.com.autorepairshop.catalog.application.dto.RegisterServiceCommand
import br.com.autorepairshop.catalog.application.usecase.RegisterServiceUseCase
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.application.dto.customer.CustomerResponse
import br.com.autorepairshop.customer.application.dto.customer.RegisterCustomerCommand
import br.com.autorepairshop.customer.application.dto.vehicle.RegisterVehicleCommand
import br.com.autorepairshop.customer.application.usecase.customer.RegisterCustomerUseCase
import br.com.autorepairshop.customer.application.usecase.vehicle.RegisterVehicleUseCase
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.repository.VehicleRepository
import br.com.autorepairshop.inputmanagment.application.dto.RegisterPartCommand
import br.com.autorepairshop.inputmanagment.application.usecase.RegisterPartUseCase
import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import br.com.autorepairshop.serviceandexecution.application.dto.CustomerOpening
import br.com.autorepairshop.serviceandexecution.application.dto.OpenServiceOrderCommand
import br.com.autorepairshop.serviceandexecution.application.dto.PartOpening
import br.com.autorepairshop.serviceandexecution.application.dto.RegisterServiceOrderCommand
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOpening
import br.com.autorepairshop.serviceandexecution.application.dto.VehicleOpening
import br.com.autorepairshop.serviceandexecution.application.dto.toResponse
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class OpenServiceOrderUseCaseTest {
    private val customers = mockk<CustomerRepository>()
    private val vehicles = mockk<VehicleRepository>()
    private val registerCustomer = mockk<RegisterCustomerUseCase>()
    private val registerVehicle = mockk<RegisterVehicleUseCase>()
    private val registerOrder = mockk<RegisterServiceOrderUseCase>()
    private val registerService = mockk<RegisterServiceUseCase>(relaxed = true)
    private val registerPart = mockk<RegisterPartUseCase>(relaxed = true)
    private val findOrder = mockk<FindServiceOrderUseCase>()
    private val useCase = OpenServiceOrderUseCase(
        customers = customers,
        vehicles = vehicles,
        registerCustomer = registerCustomer,
        registerVehicle = registerVehicle,
        registerOrder = registerOrder,
        registerService = registerService,
        registerPart = registerPart,
        findOrder = findOrder,
    )

    @Test
    fun `reuses an existing customer and vehicle`() {
        val customer = CustomerFixtures.activeCustomer()
        val vehicle = CustomerFixtures.vehicle(owner = customer)
        val opened = ServiceOrderFixtures.received(
            customerId = customer.id.value,
            vehicleId = vehicle.id.value,
        ).toResponse()
        every { customers.findByDocumentId(id = any()) } returns customer
        every { vehicles.findByPlate(plate = any()) } returns vehicle
        every { registerOrder.execute(input = any()) } returns opened
        every { findOrder.execute(input = opened.id) } returns opened

        val response = useCase.execute(
            input = OpenServiceOrderCommand(
                document = CustomerFixtures.VALID_CPF,
                vehiclePlate = CustomerFixtures.PLATE,
            ),
        )

        assertEquals(expected = opened.id, actual = response.id)
        verify(exactly = 0) { registerCustomer.execute(input = any()) }
        verify(exactly = 0) { registerVehicle.execute(input = any()) }
        verify {
            registerOrder.execute(
                input = RegisterServiceOrderCommand(
                    document = CustomerFixtures.VALID_CPF,
                    vehiclePlate = CustomerFixtures.PLATE,
                ),
            )
        }
    }

    @Test
    fun `creates the customer vehicle services and parts when they are new`() {
        val customerId = UUID.randomUUID()
        val inventoryId = UUID.randomUUID()
        val opened = ServiceOrderFixtures.received(customerId = customerId).toResponse()
        every { customers.findByDocumentId(id = any()) } returns null
        every { vehicles.findByPlate(plate = any()) } returns null
        every { registerCustomer.execute(input = any()) } returns customerResponse(id = customerId)
        every { registerVehicle.execute(input = any()) } returns mockk(relaxed = true)
        every { registerOrder.execute(input = any()) } returns opened
        every { findOrder.execute(input = opened.id) } returns opened

        useCase.execute(input = fullCommand(inventoryId = inventoryId))

        verify {
            registerCustomer.execute(
                input = RegisterCustomerCommand(
                    documentId = CustomerFixtures.VALID_CPF,
                    name = CustomerFixtures.NAME,
                    email = CustomerFixtures.EMAIL,
                    phone = CustomerFixtures.PHONE,
                ),
            )
        }
        verify {
            registerVehicle.execute(
                input = RegisterVehicleCommand(
                    ownerId = customerId,
                    plate = CustomerFixtures.PLATE,
                    brand = CustomerFixtures.BRAND,
                    model = CustomerFixtures.MODEL,
                    color = CustomerFixtures.COLOR,
                    year = CustomerFixtures.YEAR,
                ),
            )
        }
        verify {
            registerService.execute(
                input = RegisterServiceCommand(
                    serviceOrderId = opened.id,
                    name = "Troca de oleo",
                    basePrice = BigDecimal("150.00"),
                ),
            )
        }
        verify {
            registerPart.execute(
                input = RegisterPartCommand(
                    serviceOrderId = opened.id,
                    inventoryId = inventoryId,
                    quantity = 2,
                ),
            )
        }
    }

    @Test
    fun `rejects an opening without a customer or a vehicle`() {
        assertFailsWith<ServiceOrderException.InvalidOpening> {
            useCase.execute(input = OpenServiceOrderCommand())
        }
    }

    @Test
    fun `rejects an opening without a plate`() {
        assertFailsWith<ServiceOrderException.InvalidOpening> {
            useCase.execute(
                input = OpenServiceOrderCommand(document = CustomerFixtures.VALID_CPF),
            )
        }
    }

    @Test
    fun `rejects a new document without customer details`() {
        every { customers.findByDocumentId(id = any()) } returns null

        assertFailsWith<ServiceOrderException.InvalidOpening> {
            useCase.execute(
                input = OpenServiceOrderCommand(
                    document = CustomerFixtures.VALID_CPF,
                    vehiclePlate = CustomerFixtures.PLATE,
                ),
            )
        }
    }

    @Test
    fun `rejects a new plate without vehicle details`() {
        val customer = CustomerFixtures.activeCustomer()
        every { customers.findByDocumentId(id = any()) } returns customer
        every { vehicles.findByPlate(plate = any()) } returns null

        assertFailsWith<ServiceOrderException.InvalidOpening> {
            useCase.execute(
                input = OpenServiceOrderCommand(
                    document = CustomerFixtures.VALID_CPF,
                    vehiclePlate = CustomerFixtures.PLATE,
                ),
            )
        }
    }

    @Test
    fun `rejects an inactive customer`() {
        every { customers.findByDocumentId(id = any()) } returns CustomerFixtures.inactiveCustomer()

        assertFailsWith<CustomerException.InvalidDocument> {
            useCase.execute(
                input = OpenServiceOrderCommand(
                    document = CustomerFixtures.VALID_CPF,
                    vehiclePlate = CustomerFixtures.PLATE,
                ),
            )
        }
    }

    @Test
    fun `rejects a vehicle owned by someone else`() {
        val customer = CustomerFixtures.activeCustomer()
        val other = CustomerFixtures.activeCustomer(documentId = CustomerFixtures.OTHER_CPF)
        every { customers.findByDocumentId(id = any()) } returns customer
        every { vehicles.findByPlate(plate = any()) } returns CustomerFixtures.vehicle(owner = other)

        assertFailsWith<ServiceOrderException.VehicleNotOwnedByCustomer> {
            useCase.execute(
                input = OpenServiceOrderCommand(
                    document = CustomerFixtures.VALID_CPF,
                    vehiclePlate = CustomerFixtures.PLATE,
                ),
            )
        }
    }

    private fun fullCommand(inventoryId: UUID) = OpenServiceOrderCommand(
        customer = CustomerOpening(
            documentId = CustomerFixtures.VALID_CPF,
            name = CustomerFixtures.NAME,
            email = CustomerFixtures.EMAIL,
            phone = CustomerFixtures.PHONE,
        ),
        vehicle = VehicleOpening(
            plate = CustomerFixtures.PLATE,
            brand = CustomerFixtures.BRAND,
            model = CustomerFixtures.MODEL,
            year = CustomerFixtures.YEAR,
            color = CustomerFixtures.COLOR,
        ),
        services = listOf(
            ServiceOpening(
                name = "Troca de oleo",
                basePrice = BigDecimal("150.00"),
            ),
        ),
        parts = listOf(
            PartOpening(
                inventoryId = inventoryId,
                quantity = 2,
            ),
        ),
    )

    private fun customerResponse(id: UUID) = CustomerResponse(
        id = id,
        documentId = CustomerFixtures.VALID_CPF,
        documentType = "CPF",
        name = CustomerFixtures.NAME,
        email = CustomerFixtures.EMAIL,
        phone = CustomerFixtures.PHONE,
        active = true,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    )
}
