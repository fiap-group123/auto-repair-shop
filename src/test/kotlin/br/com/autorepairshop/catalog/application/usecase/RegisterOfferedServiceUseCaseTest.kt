package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.application.dto.RegisterOfferedServiceCommand
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.OfferedServiceRepository
import br.com.autorepairshop.shared.domain.exception.DomainException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class RegisterOfferedServiceUseCaseTest {
    private val services = mockk<OfferedServiceRepository>()
    private val useCase = RegisterOfferedServiceUseCase(services = services)

    @Test
    fun `rejects a duplicate name`() {
        every { services.existsByName(name = any()) } returns true

        assertFailsWith<CatalogException.ServiceAlreadyExists> {
            useCase.execute(input = command())
        }
        verify(exactly = 0) { services.save(service = any()) }
    }

    @Test
    fun `rejects a negative price`() {
        every { services.existsByName(name = any()) } returns false

        assertFailsWith<DomainException> {
            useCase.execute(input = command(price = "-1.00"))
        }
        verify(exactly = 0) { services.save(service = any()) }
    }

    @Test
    fun `registers an active service`() {
        every { services.existsByName(name = any()) } returns false
        every { services.save(service = any()) } returns Unit

        val response = useCase.execute(input = command())

        assertEquals(
            expected = CatalogFixtures.NAME,
            actual = response.name,
        )
        assertEquals(
            expected = "150.00",
            actual = response.price.toPlainString(),
        )
        assertTrue(response.active)
        verify { services.save(service = any()) }
    }

    private fun command(
        name: String = CatalogFixtures.NAME,
        price: String = CatalogFixtures.PRICE,
    ) = RegisterOfferedServiceCommand(
        name = name,
        price = BigDecimal(price),
    )
}
