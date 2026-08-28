package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.OfferedServiceResponse
import br.com.autorepairshop.catalog.application.dto.UpdateOfferedServiceCommand
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.OfferedServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateOfferedServiceUseCase(private val services: OfferedServiceRepository) :
    UseCase<UpdateOfferedServiceCommand, OfferedServiceResponse> {

    @Transactional
    override fun execute(input: UpdateOfferedServiceCommand): OfferedServiceResponse {
        val service = services.findById(id = ServiceId(value = input.serviceId))
            ?: throw CatalogException.ServiceNotFound(
                message = "Service ${input.serviceId} was not found.",
            )

        input.name?.let { raw ->
            val newName = ServiceName.of(raw = raw)
            if (newName != service.name && services.existsByName(name = newName)) {
                throw CatalogException.ServiceAlreadyExists(
                    message = "Service ${newName.value} already exists.",
                )
            }
            service.rename(newName = newName)
        }
        input.price?.let { service.changeBasePrice(newBasePrice = Money.of(raw = it)) }

        services.save(service = service)
        return service.toResponse()
    }
}
