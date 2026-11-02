package br.com.autorepairshop.api.controller.serviceandexecution

import br.com.autorepairshop.api.dto.serviceandexecution.CustomerOpeningRequest
import br.com.autorepairshop.api.dto.serviceandexecution.PartOpeningRequest
import br.com.autorepairshop.api.dto.serviceandexecution.RegisterServiceOrderRequest
import br.com.autorepairshop.api.dto.serviceandexecution.ServiceOpeningRequest
import br.com.autorepairshop.api.dto.serviceandexecution.VehicleOpeningRequest
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import br.com.autorepairshop.serviceandexecution.application.dto.CustomerOpening
import br.com.autorepairshop.serviceandexecution.application.dto.OpenServiceOrderCommand
import br.com.autorepairshop.serviceandexecution.application.dto.PartOpening
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOpening
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOrderStatusResponse
import br.com.autorepairshop.serviceandexecution.application.dto.VehicleOpening
import br.com.autorepairshop.serviceandexecution.application.dto.toResponse
import br.com.autorepairshop.serviceandexecution.application.usecase.DeliverServiceOrderUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.FindServiceOrderUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.FinishDiagnosisUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.FinishServiceOrderUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.GetServiceOrderStatusUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.ListServiceOrdersByCustomerIdUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.ListServiceOrdersUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.OpenServiceOrderUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.StartDiagnosisUseCase
import br.com.autorepairshop.serviceandexecution.application.usecase.StartExecutionUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals

@Tag("unit")
class ServiceOrderControllerTest {
    private val openOrder = mockk<OpenServiceOrderUseCase>()
    private val findOrder = mockk<FindServiceOrderUseCase>()
    private val orderStatus = mockk<GetServiceOrderStatusUseCase>()
    private val listOrders = mockk<ListServiceOrdersUseCase>()
    private val listOrdersByCustomerId = mockk<ListServiceOrdersByCustomerIdUseCase>()
    private val startDiagnosisUseCase = mockk<StartDiagnosisUseCase>()
    private val finishDiagnosisUseCase = mockk<FinishDiagnosisUseCase>()
    private val startExecutionUseCase = mockk<StartExecutionUseCase>()
    private val completeOrder = mockk<FinishServiceOrderUseCase>()
    private val deliverOrder = mockk<DeliverServiceOrderUseCase>()
    private val controller = ServiceOrderController(
        openOrder = openOrder,
        findOrder = findOrder,
        orderStatus = orderStatus,
        listOrders = listOrders,
        listOrdersByCustomerId = listOrdersByCustomerId,
        startDiagnosis = startDiagnosisUseCase,
        finishDiagnosis = finishDiagnosisUseCase,
        startExecution = startExecutionUseCase,
        completeOrder = completeOrder,
        deliverOrder = deliverOrder,
    )

    @Test
    fun `open maps the request and returns 201`() {
        val order = ServiceOrderFixtures.received().toResponse()
        every { openOrder.execute(input = any()) } returns order

        withHttpRequest(requestUri = "/service-orders") {
            val response = controller.register(
                request = RegisterServiceOrderRequest(
                    document = CustomerFixtures.VALID_CPF,
                    vehiclePlate = CustomerFixtures.PLATE,
                ),
            )
            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
        }
        verify {
            openOrder.execute(
                input = OpenServiceOrderCommand(
                    document = CustomerFixtures.VALID_CPF,
                    vehiclePlate = CustomerFixtures.PLATE,
                ),
            )
        }
    }

    @Test
    fun `open maps customer vehicle services and parts`() {
        val inventoryId = UUID.randomUUID()
        val order = ServiceOrderFixtures.received().toResponse()
        every { openOrder.execute(input = any()) } returns order

        withHttpRequest(requestUri = "/service-orders") {
            controller.register(request = fullRequest(inventoryId = inventoryId))
        }

        verify {
            openOrder.execute(input = fullCommand(inventoryId = inventoryId))
        }
    }

    private fun fullRequest(inventoryId: UUID) = RegisterServiceOrderRequest(
        customer = CustomerOpeningRequest(
            documentId = CustomerFixtures.VALID_CPF,
            name = CustomerFixtures.NAME,
            email = CustomerFixtures.EMAIL,
            phone = CustomerFixtures.PHONE,
        ),
        vehicle = VehicleOpeningRequest(
            plate = CustomerFixtures.PLATE,
            brand = CustomerFixtures.BRAND,
            model = CustomerFixtures.MODEL,
            year = CustomerFixtures.YEAR,
            color = CustomerFixtures.COLOR,
        ),
        services = listOf(
            ServiceOpeningRequest(
                name = "Troca de oleo",
                basePrice = BigDecimal("150.00"),
            ),
        ),
        parts = listOf(
            PartOpeningRequest(
                inventoryId = inventoryId,
                quantity = 2,
            ),
        ),
    )

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

    @Test
    fun `list find and lifecycle endpoints delegate to use cases`() {
        val order = ServiceOrderFixtures.inDiagnosis().toResponse()
        every { listOrders.execute(input = Unit) } returns listOf(element = order)
        every { findOrder.execute(input = order.id) } returns order
        every { orderStatus.execute(input = order.id) } returns ServiceOrderStatusResponse(
            id = order.id,
            status = order.status,
            label = "Diagnóstico",
        )
        every { startDiagnosisUseCase.execute(input = order.id) } returns order
        every { finishDiagnosisUseCase.execute(input = order.id) } returns order
        every { startExecutionUseCase.execute(input = order.id) } returns order
        every { completeOrder.execute(input = order.id) } returns order
        every { deliverOrder.execute(input = order.id) } returns order

        assertEquals(
            expected = 1,
            actual = controller.list().body?.size,
        )
        assertEquals(
            expected = order.id,
            actual = controller.findById(id = order.id).body?.id,
        )
        assertEquals(
            expected = "Diagnóstico",
            actual = controller.status(id = order.id).body?.label,
        )
        assertEquals(
            expected = order.id,
            actual = controller.startDiagnosis(id = order.id).body?.id,
        )
        assertEquals(
            expected = order.id,
            actual = controller.finishDiagnosis(id = order.id).body?.id,
        )
        assertEquals(
            expected = order.id,
            actual = controller.startExecution(id = order.id).body?.id,
        )
        assertEquals(
            expected = order.id,
            actual = controller.complete(id = order.id).body?.id,
        )
        assertEquals(
            expected = order.id,
            actual = controller.deliver(id = order.id).body?.id,
        )
    }

    @Test
    fun `listing by customer id delegates to the use case`() {
        val customerId = UUID.randomUUID()
        every { listOrdersByCustomerId.execute(input = customerId) } returns emptyList()

        controller.listByCustomerId(customerId = customerId)

        verify { listOrdersByCustomerId.execute(input = customerId) }
    }
}
