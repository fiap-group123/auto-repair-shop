package br.com.autorepairshop.customer.application.usecase.customer

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.application.dto.customer.RegisterCustomerCommand
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class RegisterCustomerUseCaseTest {
    private val customers = mockk<CustomerRepository>()
    private val useCase = RegisterCustomerUseCase(customers = customers)

    @Test
    fun `saves a new customer`() {
        every { customers.existsByDocumentId(id = any()) } returns false
        every { customers.save(customer = any()) } returns Unit

        val response = useCase.execute(input = command())

        assertEquals(
            expected = CustomerFixtures.NAME,
            actual = response.name,
        )
        assertTrue(response.active)
        verify { customers.save(customer = any()) }
    }

    @Test
    fun `rejects duplicate document without saving`() {
        every { customers.existsByDocumentId(id = any()) } returns true

        assertFailsWith<CustomerException.CustomerAlreadyExists> {
            useCase.execute(input = command())
        }
        verify(exactly = 0) { customers.save(customer = any()) }
    }

    private fun command() = RegisterCustomerCommand(
        documentId = CustomerFixtures.VALID_CPF,
        name = CustomerFixtures.NAME,
        email = CustomerFixtures.EMAIL,
        phone = CustomerFixtures.PHONE,
    )
}
