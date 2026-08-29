package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ServiceStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@Tag("unit")
class StartServiceUseCaseTest {
    private val services = mockk<ServiceRepository>()
    private val useCase = InProgressServiceUseCase(services = services)

    @Test
    fun `throws when the service is missing`() {
        val id = UUID.randomUUID()
        every { services.findById(id = ServiceId(value = id)) } returns null

        assertFailsWith<CatalogException.ServiceNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `throws when the service already started`() {
        val service = CatalogFixtures.activeService()
        service.inProgress()
        every { services.findById(id = service.id) } returns service

        assertFailsWith<CatalogException.InvalidStatusTransition> {
            useCase.execute(input = service.id.value)
        }
        verify(exactly = 0) { services.save(service = any()) }
    }

    @Test
    fun `starts a waiting service`() {
        val service = CatalogFixtures.activeService()
        every { services.findById(id = service.id) } returns service
        every { services.save(service = service) } returns Unit

        val response = useCase.execute(input = service.id.value)

        assertEquals(
            expected = ServiceStatus.IN_PROGRESS.name,
            actual = response.status,
        )
        assertNotNull(response.startedAt)
        verify { services.save(service = service) }
    }
}
