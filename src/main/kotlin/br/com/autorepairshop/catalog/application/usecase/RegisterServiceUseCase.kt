package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.RegisterServiceCommand
import br.com.autorepairshop.catalog.application.dto.ServiceResponse
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.aggregate.Service
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.domain.Money
import org.springframework.transaction.annotation.Transactional

@org.springframework.stereotype.Service
class RegisterServiceUseCase(
    private val serviceOrders: ServiceOrderRepository,
    private val services: ServiceRepository,
) : UseCase<RegisterServiceCommand, ServiceResponse> {

    @Transactional
    override fun execute(input: RegisterServiceCommand): ServiceResponse {
        val name = ServiceName.of(raw = input.name)
        val order = serviceOrders.findById(id = ServiceOrderId(value = input.serviceOrderId))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order ${input.serviceOrderId} was not found.",
            )
        order.requireItemsEditable()
        if (services.existsByName(name = name, serviceOrderId = order.id.value)) {
            throw CatalogException.ServiceAlreadyExists(
                message = "Service ${name.value} already exists.",
            )
        }
        val service = Service.register(
            serviceOrderId = order.id.value,
            name = name,
            price = Money.of(raw = input.basePrice),
        )
        services.save(service = service)
        return service.toResponse()
    }
}
