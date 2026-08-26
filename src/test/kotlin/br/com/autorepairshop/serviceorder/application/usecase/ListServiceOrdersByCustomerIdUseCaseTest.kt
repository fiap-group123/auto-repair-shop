package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class ListServiceOrdersByCustomerIdUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val useCase = ListServiceOrdersByCustomerIdUseCase(orders = orders)

    @Test
    fun `throws when the customer has no orders`() {
        val customerId = UUID.randomUUID()
        every { orders.findByCustomerId(customerId = customerId) } returns emptyList()

        assertFailsWith<ServiceOrderException.ServiceOrderNotFound> {
            useCase.execute(input = customerId)
        }
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
    }
}
