package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.ServiceResponse
import br.com.autorepairshop.catalog.application.dto.UpdateServiceCommand
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateServiceUseCase(private val services: ServiceRepository) :
    UseCase<UpdateServiceCommand, ServiceResponse> {

    @Transactional
    override fun execute(input: UpdateServiceCommand): ServiceResponse {
        val service = services.findById(id = ServiceId(value = input.serviceId))
            ?: throw CatalogException.ServiceNotFound(
                message = "Service ${input.serviceId} was not found.",
            )

        input.name?.let { raw ->
            val newName = ServiceName.of(raw = raw)
            if (newName != service.name &&
                services.existsByName(name = newName, serviceOrderId = service.serviceOrderId)
            ) {
                throw CatalogException.ServiceAlreadyExists(
                    message = "Service ${newName.value} already exists.",
                )
            }
            service.rename(newName = newName)
        }
        input.basePrice?.let { service.changeBasePrice(newBasePrice = Money.of(raw = it)) }

        services.save(service = service)
        return service.toResponse()
    }
}
