package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class FindServiceUseCaseTest {
    private val services = mockk<ServiceRepository>()
    private val useCase = FindServiceUseCase(services = services)

    @Test
    fun `throws when the service is missing`() {
        val id = UUID.randomUUID()
        every { services.findById(id = ServiceId(value = id)) } returns null

        assertFailsWith<CatalogException.ServiceNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `returns the service when found`() {
        val service = CatalogFixtures.activeService()
        every { services.findById(id = service.id) } returns service

        val response = useCase.execute(input = service.id.value)

        assertEquals(
            expected = service.id.value,
            actual = response.id,
        )
        assertEquals(
            expected = service.serviceOrderId,
            actual = response.serviceOrderId,
        )
        assertEquals(
            expected = "150.00",
            actual = response.basePrice.toPlainString(),
        )
    }
}
