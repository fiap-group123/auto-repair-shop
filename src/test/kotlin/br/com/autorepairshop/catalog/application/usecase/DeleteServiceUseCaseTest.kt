package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.CatalogFixtures
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.shared.application.event.EventPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFailsWith

@Tag("unit")
class DeleteServiceUseCaseTest {
    private val services = mockk<ServiceRepository>()
    private val events = mockk<EventPublisher>(relaxed = true)
    private val useCase = DeleteServiceUseCase(
        services = services,
        events = events,
    )

    @Test
    fun `throws when the service is missing`() {
        val id = UUID.randomUUID()
        every { services.findById(id = ServiceId(value = id)) } returns null

        assertFailsWith<CatalogException.ServiceNotFound> {
            useCase.execute(input = id)
        }
    }

    @Test
    fun `rejects removing a service that already started`() {
        val service = CatalogFixtures.activeService()
        service.inProgress()
        every { services.findById(id = service.id) } returns service

        assertFailsWith<CatalogException.InvalidStatusTransition> {
            useCase.execute(input = service.id.value)
        }
        verify(exactly = 0) { services.delete(service = any()) }
    }

    @Test
    fun `removes a waiting service and publishes the event`() {
        val service = CatalogFixtures.activeService()
        every { services.findById(id = service.id) } returns service
        every { services.delete(service = service) } returns Unit

        useCase.execute(input = service.id.value)

        verify { events.publish(aggregate = service) }
        verify { services.delete(service = service) }
    }
}
