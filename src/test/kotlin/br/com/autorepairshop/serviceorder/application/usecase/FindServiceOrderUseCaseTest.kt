package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceorder.serviceOrderAssembler
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class FindServiceOrderUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val useCase = FindServiceOrderUseCase(
        orders = orders,
        responses = serviceOrderAssembler(),
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
    }
}
