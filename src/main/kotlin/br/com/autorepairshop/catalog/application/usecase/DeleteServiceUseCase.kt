package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.application.event.EventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteServiceUseCase(
    private val services: ServiceRepository,
    private val events: EventPublisher,
) : UseCase<UUID, Unit> {

    @Transactional
    override fun execute(input: UUID) {
        val service = services.findById(id = ServiceId(value = input))
            ?: throw CatalogException.ServiceNotFound(
                message = "Service $input was not found.",
            )
        service.remove()
        events.publish(aggregate = service)
        services.delete(service = service)
    }
}
