package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.OfferedServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.OfferedServiceId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ReactivateOfferedServiceUseCase(private val services: OfferedServiceRepository) : UseCase<UUID, Unit> {

    @Transactional
    override fun execute(input: UUID) {
        val service = services.findById(id = OfferedServiceId(value = input))
            ?: throw CatalogException.ServiceNotFound(message = "Service $input was not found.")
        service.reactivate()
        services.save(service = service)
    }
}
