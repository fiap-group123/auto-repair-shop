package br.com.autorepairshop.customer.application.usecase.customer

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@Tag("unit")
class DeactivateCustomerUseCaseTest {
    private val customers = mockk<CustomerRepository>()
    private val useCase = DeactivateCustomerUseCase(
        customers = customers,
    )

    @Test
    fun `throws when customer is missing`() {
        val id = UUID.randomUUID()
        every { customers.findById(id = CustomerId(value = id)) } returns null

        assertFailsWith<CustomerException.CustomerNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `saves when deactivating`() {
        val customer = CustomerFixtures.activeCustomer()
        every { customers.findById(id = customer.id) } returns customer
        every { customers.save(customer = customer) } returns Unit

        useCase.execute(input = customer.id.value)

        assertFalse(customer.active)
        verify { customers.save(customer = customer) }
    }
}
