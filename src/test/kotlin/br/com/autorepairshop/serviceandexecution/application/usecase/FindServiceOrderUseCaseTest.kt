package br.com.autorepairshop.serviceandexecution.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.serviceandexecution.ServiceOrderFixtures
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceandexecution.serviceOrderAssembler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class FindServiceOrderUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = FindServiceOrderUseCase(
        orders = orders,
        responses = serviceOrderAssembler(),
        access = access,
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
    fun `returns the order when found`() {
        val order = ServiceOrderFixtures.received()
        every { orders.findById(id = order.id) } returns order

        val response = useCase.execute(input = order.id.value)

        assertEquals(
            expected = order.id.value,
            actual = response.id,
        )
        assertEquals(
            expected = order.customerId,
            actual = response.customerId,
        )
        verify { access.requireCustomer(customerId = order.customerId) }
    }

    @Test
    fun `throws when the client does not own the order`() {
        val order = ServiceOrderFixtures.received()
        every { orders.findById(id = order.id) } returns order
        every { access.requireCustomer(customerId = order.customerId) } throws
            AuthenticationException.Forbidden(message = "Cannot access another customer.")

        assertFailsWith<AuthenticationException.Forbidden> {
            useCase.execute(input = order.id.value)
        }
    }
}
