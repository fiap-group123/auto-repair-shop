package br.com.autorepairshop.customer.application.usecase.customer

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("unit")
class ListCustomersUseCaseTest {
    private val customers = mockk<CustomerRepository>()
    private val useCase = ListCustomersUseCase(customers = customers)

    @Test
    fun `maps persisted customers to responses`() {
        val active = CustomerFixtures.activeCustomer()
        val inactive = CustomerFixtures.inactiveCustomer()
        every { customers.findAll() } returns listOf(element = active).plus(element = inactive)

        val response = useCase.execute(input = Unit)

        assertEquals(
            expected = 2,
            actual = response.size,
        )
        assertEquals(
            expected = active.id.value,
            actual = response[0].id,
        )
        assertEquals(
            expected = "529.982.247-25",
            actual = response[0].documentId,
        )
        assertTrue(response[0].active)
        assertEquals(
            expected = false,
            actual = response[1].active,
        )
    }

    @Test
    fun `returns empty list when there are no customers`() {
        every { customers.findAll() } returns emptyList()

        val response = useCase.execute(input = Unit)

        assertTrue(response.isEmpty())
    }
}
