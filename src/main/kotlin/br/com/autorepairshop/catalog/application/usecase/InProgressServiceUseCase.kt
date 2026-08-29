package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.ServiceResponse
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class InProgressServiceUseCase(private val services: ServiceRepository) : UseCase<UUID, ServiceResponse> {

    @Transactional
    override fun execute(input: UUID): ServiceResponse {
        val service = services.findById(id = ServiceId(value = input))
            ?: throw CatalogException.ServiceNotFound(message = "Service $input was not found.")
        service.inProgress()
        services.save(service = service)
        return service.toResponse()
    }
}
