package br.com.autorepairshop.customer.infrastructure.persistence.customer

import br.com.autorepairshop.customer.CustomerFixtures
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Tag("unit")
class CustomerRepositoryImplTest {
    private val jpa = mockk<CustomerJpaRepository>()
    private val repo = CustomerRepositoryImpl(jpa = jpa)

    @Test
    fun `maps a customer through save and queries`() {
        val customer = CustomerFixtures.activeCustomer()
        val stored = slot<CustomerEntity>()
        every { jpa.save(capture(stored)) } answers { stored.captured }
        repo.save(customer = customer)

        every { jpa.findById(customer.id.value) } returns Optional.of(stored.captured)
        every { jpa.findByDocumentId(documentId = customer.document.value) } returns stored.captured
        every { jpa.existsByDocumentId(documentId = customer.document.value) } returns true
        every { jpa.findAll() } returns listOf(element = stored.captured)

        assertEquals(
            expected = customer.id,
            actual = repo.findById(id = customer.id)?.id,
        )
        assertEquals(
            expected = customer.id,
            actual = repo.findByDocumentId(id = customer.document)?.id,
        )
        assertTrue(repo.existsByDocumentId(id = customer.document))
        assertEquals(
            expected = 1,
            actual = repo.findAll().size,
        )
    }

    @Test
    fun `returns null when the customer is missing`() {
        every { jpa.findById(any()) } returns Optional.empty()
        every { jpa.findByDocumentId(documentId = any()) } returns null

        assertNull(repo.findById(id = CustomerFixtures.activeCustomer().id))
        assertNull(repo.findByDocumentId(id = CustomerFixtures.activeCustomer().document))
    }
}
