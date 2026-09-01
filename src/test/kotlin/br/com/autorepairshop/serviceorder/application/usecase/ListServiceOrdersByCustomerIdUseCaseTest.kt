package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.authentication.application.security.AccessGuard
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.serviceOrderAssembler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class ListServiceOrdersByCustomerIdUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = ListServiceOrdersByCustomerIdUseCase(
        orders = orders,
        responses = serviceOrderAssembler(),
        access = access,
    )

    @Test
    fun `returns an empty list when the customer has no orders`() {
        val customerId = UUID.randomUUID()
        every { orders.findByCustomerId(customerId = customerId) } returns emptyList()

        val response = useCase.execute(input = customerId)

        assertTrue(response.isEmpty())
        verify { access.requireCustomer(customerId = customerId) }
    }

    @Test
    fun `throws when the client lists another customer`() {
        val customerId = UUID.randomUUID()
        every { access.requireCustomer(customerId = customerId) } throws
            AuthenticationException.Forbidden(message = "Cannot access another customer.")

        assertFailsWith<AuthenticationException.Forbidden> {
            useCase.execute(input = customerId)
        }
        verify(exactly = 0) { orders.findByCustomerId(customerId = any()) }
    }

    @Test
    fun `lists orders for the given customer`() {
        val customerId = UUID.randomUUID()
        val first = ServiceOrderFixtures.received(customerId = customerId)
        val second = ServiceOrderFixtures.inDiagnosis(customerId = customerId)
        every {
            orders.findByCustomerId(
                customerId = customerId,
            )
        } returns
            listOf(
                element = first,
            ).plus(
                element = second,
            )

        val response = useCase.execute(input = customerId)

        assertEquals(
            expected = 2,
            actual = response.size,
        )
        assertEquals(
            expected = first.id.value,
            actual = response[0].id,
        )
        assertEquals(
            expected = customerId,
            actual = response[0].customerId,
        )
        assertEquals(
            expected = second.id.value,
            actual = response[1].id,
        )
        assertEquals(
            expected = customerId,
            actual = response[1].customerId,
        )
        verify { access.requireCustomer(customerId = customerId) }
    }
}
