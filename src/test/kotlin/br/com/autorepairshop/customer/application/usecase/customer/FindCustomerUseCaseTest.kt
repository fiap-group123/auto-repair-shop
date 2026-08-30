package br.com.autorepairshop.customer.application.usecase.customer

import br.com.autorepairshop.authentication.application.security.AccessGuard
import br.com.autorepairshop.customer.CustomerFixtures
import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.customer.domain.repository.CustomerRepository
import br.com.autorepairshop.customer.domain.valueobject.customer.CustomerId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class FindCustomerUseCaseTest {
    private val customers = mockk<CustomerRepository>()
    private val access = mockk<AccessGuard>(relaxUnitFun = true)
    private val useCase = FindCustomerUseCase(
        customers = customers,
        access = access,
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
    fun `returns the customer when found`() {
        val customer = CustomerFixtures.activeCustomer()
        every { customers.findById(id = customer.id) } returns customer

        val response = useCase.execute(input = customer.id.value)

        assertEquals(
            expected = customer.id.value,
            actual = response.id,
        )
        assertEquals(
            expected = "529.982.247-25",
            actual = response.documentId,
        )
        assertTrue(response.active)
    }
}
