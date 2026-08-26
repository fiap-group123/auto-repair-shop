package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.OfferedServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.OfferedServiceId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class ReactivateOfferedServiceUseCaseTest {
    private val services = mockk<OfferedServiceRepository>()
    private val useCase = ReactivateOfferedServiceUseCase(services = services)

    @Test
    fun `throws when the service is missing`() {
        val id = UUID.randomUUID()
        every { services.findById(id = OfferedServiceId(value = id)) } returns null

        assertFailsWith<CatalogException.ServiceNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `throws when the service is already active`() {
        val service = CatalogFixtures.activeService()
        every { services.findById(id = service.id) } returns service

        assertFailsWith<CatalogException.ServiceAlreadyActive> {
            useCase.execute(input = service.id.value)
        }
    }

    @Test
    fun `saves when reactivating`() {
        val service = CatalogFixtures.inactiveService()
        every { services.findById(id = service.id) } returns service
        every { services.save(service = service) } returns Unit

        useCase.execute(input = service.id.value)

        assertTrue(service.active)
        verify { services.save(service = service) }
    }
}
