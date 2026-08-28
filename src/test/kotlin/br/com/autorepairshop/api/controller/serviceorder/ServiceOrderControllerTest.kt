package br.com.autorepairshop.api.controller.serviceorder

import br.com.autorepairshop.api.dto.authentication.AuthenticatedUser
import br.com.autorepairshop.api.dto.serviceorder.RegisterServiceOrderRequest
import br.com.autorepairshop.api.security.AuthorizationSupport
import br.com.autorepairshop.api.security.CurrentUser
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.valueobject.Role
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.application.dto.RegisterServiceOrderCommand
import br.com.autorepairshop.serviceorder.application.dto.toResponse
import br.com.autorepairshop.serviceorder.application.usecase.ApproveServiceOrderUseCase
import br.com.autorepairshop.serviceorder.application.usecase.DeliverServiceOrderUseCase
import br.com.autorepairshop.serviceorder.application.usecase.FindServiceOrderUseCase
import br.com.autorepairshop.serviceorder.application.usecase.FinishDiagnosisUseCase
import br.com.autorepairshop.serviceorder.application.usecase.FinishServiceOrderUseCase
import br.com.autorepairshop.serviceorder.application.usecase.ListServiceOrdersByCustomerIdUseCase
import br.com.autorepairshop.serviceorder.application.usecase.ListServiceOrdersUseCase
import br.com.autorepairshop.serviceorder.application.usecase.RegisterServiceOrderUseCase
import br.com.autorepairshop.serviceorder.application.usecase.StartDiagnosisUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class ServiceOrderControllerTest {
    private val openOrder = mockk<RegisterServiceOrderUseCase>()
    private val findOrder = mockk<FindServiceOrderUseCase>()
    private val listOrders = mockk<ListServiceOrdersUseCase>()
    private val listOrdersByCustomerId = mockk<ListServiceOrdersByCustomerIdUseCase>()
    private val startDiagnosisUseCase = mockk<StartDiagnosisUseCase>()
    private val finishDiagnosisUseCase = mockk<FinishDiagnosisUseCase>()
    private val approveOrder = mockk<ApproveServiceOrderUseCase>()
    private val completeOrder = mockk<FinishServiceOrderUseCase>()
    private val deliverOrder = mockk<DeliverServiceOrderUseCase>()
    private val authorization = mockk<AuthorizationSupport>(relaxUnitFun = true)
    private val currentUser = mockk<CurrentUser>()
    private val controller = ServiceOrderController(
        registerOrder = openOrder,
        findOrder = findOrder,
        listOrders = listOrders,
        listOrdersByCustomerId = listOrdersByCustomerId,
        startDiagnosisUseCase = startDiagnosisUseCase,
        finishDiagnosisUseCase = finishDiagnosisUseCase,
        approveOrder = approveOrder,
        completeOrder = completeOrder,
        deliverOrder = deliverOrder,
        authorization = authorization,
        currentUser = currentUser,
    )

    @Test
    fun `open maps the request and returns 201`() {
        val order = ServiceOrderFixtures.received().toResponse()
        every { openOrder.execute(input = any()) } returns order

        withHttpRequest(requestUri = "/service-orders") {
            val response = controller.register(
                request = RegisterServiceOrderRequest(
                    customerId = order.customerId,
                    vehicleId = order.vehicleId,
                ),
            )
            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
        }
        verify {
            openOrder.execute(
                input = RegisterServiceOrderCommand(
                    customerId = order.customerId,
                    vehicleId = order.vehicleId,
                ),
            )
        }
    }

    @Test
    fun `list find and lifecycle endpoints delegate to use cases`() {
        val order = ServiceOrderFixtures.inDiagnosisWithBudget().toResponse()
        every { listOrders.execute(input = Unit) } returns listOf(element = order)
        every { findOrder.execute(input = order.id) } returns order
        every { startDiagnosisUseCase.execute(input = order.id) } returns order
        every { finishDiagnosisUseCase.execute(input = order.id) } returns order
        every { approveOrder.execute(input = order.id) } returns order
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
            expected = order.id,
            actual = controller.startDiagnosis(id = order.id).body?.id,
        )
        assertEquals(
            expected = order.id,
            actual = controller.finishDiagnosis(id = order.id).body?.id,
        )
        assertEquals(
            expected = order.id,
            actual = controller.approve(id = order.id).body?.id,
        )
        assertEquals(
            expected = order.id,
            actual = controller.complete(id = order.id).body?.id,
        )
        assertEquals(
            expected = order.id,
            actual = controller.deliver(id = order.id).body?.id,
        )
        verify {
            authorization.requireCanAccessServiceOrder(customerId = order.customerId)
        }
    }

    @Test
    fun `clients can list orders by customer id and staff cannot`() {
        val customerId = UUID.randomUUID()
        val order = ServiceOrderFixtures.received(customerId = customerId).toResponse()
        every { listOrdersByCustomerId.execute(input = customerId) } returns listOf(element = order)
        every { currentUser.get() } returns AuthenticatedUser(
            userId = UUID.randomUUID(),
            role = Role.CLIENT,
            customerId = customerId,
        )

        assertEquals(
            expected = 1,
            actual = controller.listByCustomerId(customerId = customerId).body?.size,
        )

        every { currentUser.get() } returns AuthenticatedUser(
            userId = UUID.randomUUID(),
            role = Role.MANAGER,
            customerId = null,
        )
        assertFailsWith<AuthenticationException.Forbidden> {
            controller.listByCustomerId(customerId = customerId)
        }
    }
}
