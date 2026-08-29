package br.com.autorepairshop.customer.application.usecase.customer

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class FindCustomerByDocumentUseCaseTest {
    private val customers = mockk<CustomerRepository>()
    private val useCase = FindCustomerByDocumentUseCase(customers = customers)

    @Test
    fun `throws when customer is missing`() {
        every { customers.findByDocumentId(id = any()) } returns null

        assertFailsWith<CustomerException.CustomerNotFound> {
            useCase.execute(input = CustomerFixtures.VALID_CPF)
        }
    }

    @Test
    fun `returns the customer when the document matches`() {
        val customer = CustomerFixtures.activeCustomer()
        every { customers.findByDocumentId(id = customer.document) } returns customer

        val response = useCase.execute(input = CustomerFixtures.VALID_CPF)

        assertEquals(
            expected = customer.id.value,
            actual = response.id,
        )
        assertEquals(
            expected = CustomerFixtures.NAME,
            actual = response.name,
        )
    }
}
