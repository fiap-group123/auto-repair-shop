package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.serviceorder.ServiceOrderFixtures
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Tag("unit")
class ListServiceOrdersUseCaseTest {
    private val orders = mockk<ServiceOrderRepository>()
    private val useCase = ListServiceOrdersUseCase(orders = orders)

    @Test
    fun `lists all orders when no customer filter is given`() {
        val first = ServiceOrderFixtures.received()
        val second = ServiceOrderFixtures.inDiagnosis()
        every { orders.findAll() } returns listOf(element = first).plus(element = second)

        val response = useCase.execute(input = Unit)

        assertEquals(
            expected = 2,
            actual = response.size,
        )
        assertEquals(
            expected = first.id.value,
            actual = response[0].id,
        )
    }
}
