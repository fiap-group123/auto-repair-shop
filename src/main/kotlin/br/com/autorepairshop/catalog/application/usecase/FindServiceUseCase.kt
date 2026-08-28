package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.OfferedServiceResponse
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.OfferedServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FindOfferedServiceUseCase(private val services: OfferedServiceRepository) :
    UseCase<UUID, OfferedServiceResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): OfferedServiceResponse {
        val service = services.findById(id = ServiceId(value = input))
            ?: throw CatalogException.ServiceNotFound(message = "Service $input was not found.")
        return service.toResponse()
    }
}
