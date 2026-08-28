package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.application.dto.UpdateServiceCommand
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class UpdateOfferedServiceUseCaseTest {
    private val services = mockk<ServiceRepository>()
    private val useCase = UpdateServiceUseCase(services = services)

    @Test
    fun `throws when the service is missing`() {
        val id = UUID.randomUUID()
        every { services.findById(id = ServiceId(value = id)) } returns null

        assertFailsWith<CatalogException.ServiceNotFound> {
            useCase.execute(input = UpdateServiceCommand(serviceId = id))
        }
    }

    @Test
    fun `rejects renaming to an existing name`() {
        val service = CatalogFixtures.activeService()
        every { services.findById(id = service.id) } returns service
        every { services.existsByName(name = any(), serviceOrderId = service.serviceOrderId) } returns true

        assertFailsWith<CatalogException.ServiceAlreadyExists> {
            useCase.execute(
                input = UpdateServiceCommand(
                    serviceId = service.id.value,
                    name = CatalogFixtures.OTHER_NAME,
                ),
            )
        }
        verify(exactly = 0) { services.save(service = any()) }
    }

    @Test
    fun `keeping the same name skips the uniqueness check`() {
        val service = CatalogFixtures.activeService()
        every { services.findById(id = service.id) } returns service
        every { services.save(service = service) } returns Unit

        val response = useCase.execute(
            input = UpdateServiceCommand(
                serviceId = service.id.value,
                name = CatalogFixtures.NAME,
            ),
        )

        assertEquals(
            expected = CatalogFixtures.NAME,
            actual = response.name,
        )
        verify(exactly = 0) { services.existsByName(name = any(), serviceOrderId = any()) }
        verify { services.save(service = service) }
    }

    @Test
    fun `renames and reprices the service`() {
        val service = CatalogFixtures.activeService()
        every { services.findById(id = service.id) } returns service
        every { services.existsByName(name = any(), serviceOrderId = service.serviceOrderId) } returns false
        every { services.save(service = service) } returns Unit

        val response = useCase.execute(
            input = UpdateServiceCommand(
                serviceId = service.id.value,
                name = CatalogFixtures.OTHER_NAME,
                basePrice = BigDecimal("200.00"),
            ),
        )

        assertEquals(
            expected = CatalogFixtures.OTHER_NAME,
            actual = response.name,
        )
        assertEquals(
            expected = "200.00",
            actual = response.basePrice.toPlainString(),
        )
    }

    @Test
    fun `updates only the price`() {
        val service = CatalogFixtures.activeService()
        every { services.findById(id = service.id) } returns service
        every { services.save(service = service) } returns Unit

        val response = useCase.execute(
            input = UpdateServiceCommand(
                serviceId = service.id.value,
                basePrice = BigDecimal("99.90"),
            ),
        )

        assertEquals(
            expected = CatalogFixtures.NAME,
            actual = response.name,
        )
        assertEquals(
            expected = "99.90",
            actual = response.basePrice.toPlainString(),
        )
    }
}
