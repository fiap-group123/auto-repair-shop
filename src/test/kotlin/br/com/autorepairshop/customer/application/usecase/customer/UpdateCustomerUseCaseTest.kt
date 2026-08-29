package br.com.autorepairshop.customer.application.usecase.customer

import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.application.dto.customer.UpdateCustomerCommand
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class UpdateCustomerUseCaseTest {
    private val customers = mockk<CustomerRepository>()
    private val useCase = UpdateCustomerUseCase(customers = customers)

    @Test
    fun `throws when customer is missing`() {
        val id = UUID.randomUUID()
        every { customers.findById(id = CustomerId(value = id)) } returns null

        assertFailsWith<CustomerException.CustomerNotFound> {
            useCase.execute(
                input = UpdateCustomerCommand(
                    customerId = id,
                    name = "Jane Doe",
                ),
            )
        }
    }

    @Test
    fun `throws when customer is inactive`() {
        val customer = CustomerFixtures.inactiveCustomer()
        every { customers.findById(id = customer.id) } returns customer

        assertFailsWith<CustomerException.CustomerInactive> {
            useCase.execute(
                input = UpdateCustomerCommand(
                    customerId = customer.id.value,
                    name = "Jane Doe",
                ),
            )
        }
    }

    @Test
    fun `renames and updates contact of an active customer`() {
        val customer = CustomerFixtures.activeCustomer()
        every { customers.findById(id = customer.id) } returns customer
        every { customers.save(customer = any()) } returns Unit

        val response = useCase.execute(
            input = UpdateCustomerCommand(
                customerId = customer.id.value,
                name = "Jane Doe",
                email = "jane.doe@email.com",
                phone = "21987654321",
            ),
        )

        assertEquals(
            expected = "Jane Doe",
            actual = response.name,
        )
        assertEquals(
            expected = "jane.doe@email.com",
            actual = response.email,
        )
        verify { customers.save(customer = customer) }
    }

    @Test
    fun `renames without changing contact`() {
        val customer = CustomerFixtures.activeCustomer()
        every { customers.findById(id = customer.id) } returns customer
        every { customers.save(customer = customer) } returns Unit

        val response = useCase.execute(
            input = UpdateCustomerCommand(
                customerId = customer.id.value,
                name = "Jane Doe",
            ),
        )

        assertEquals(
            expected = "Jane Doe",
            actual = response.name,
        )
        assertEquals(
            expected = CustomerFixtures.EMAIL,
            actual = response.email,
        )
        assertEquals(
            expected = CustomerFixtures.PHONE,
            actual = response.phone,
        )
    }

    @Test
    fun `updates only email and keeps phone`() {
        val customer = CustomerFixtures.activeCustomer()
        every { customers.findById(id = customer.id) } returns customer
        every { customers.save(customer = customer) } returns Unit

        val response = useCase.execute(
            input = UpdateCustomerCommand(
                customerId = customer.id.value,
                email = "jane.doe@email.com",
            ),
        )

        assertEquals(
            expected = "jane.doe@email.com",
            actual = response.email,
        )
        assertEquals(
            expected = CustomerFixtures.PHONE,
            actual = response.phone,
        )
        assertEquals(
            expected = CustomerFixtures.NAME,
            actual = response.name,
        )
    }

    @Test
    fun `updates only phone and keeps email`() {
        val customer = CustomerFixtures.activeCustomer()
        every { customers.findById(id = customer.id) } returns customer
        every { customers.save(customer = customer) } returns Unit

        val response = useCase.execute(
            input = UpdateCustomerCommand(
                customerId = customer.id.value,
                phone = "21987654321",
            ),
        )

        assertEquals(
            expected = "21987654321",
            actual = response.phone,
        )
        assertEquals(
            expected = CustomerFixtures.EMAIL,
            actual = response.email,
        )
    }
}
