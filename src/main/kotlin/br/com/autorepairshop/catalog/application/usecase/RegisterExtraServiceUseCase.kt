package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.catalog.application.dto.ExtraServiceResponse
import br.com.autorepairshop.catalog.application.dto.RegisterExtraServiceCommand
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.aggregate.ExtraService
import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.application.event.EventPublisher
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterExtraServiceUseCase(
    private val serviceOrders: ServiceOrderRepository,
    private val services: ServiceRepository,
    private val extras: ExtraServiceRepository,
    private val events: EventPublisher,
) : UseCase<RegisterExtraServiceCommand, ExtraServiceResponse> {

    @Transactional
    override fun execute(input: RegisterExtraServiceCommand): ExtraServiceResponse {
        val name = ServiceName.of(raw = input.name)
        val order = serviceOrders.findById(id = ServiceOrderId(value = input.serviceOrderId))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order ${input.serviceOrderId} was not found.",
            )
        if (order.status !in allowedStatuses) {
            throw CatalogException.InvalidStatusTransition(
                message = "Cannot register an extra service from ${order.status.name}.",
            )
        }
        if (services.existsByName(name = name, serviceOrderId = order.id.value) ||
            extras.existsByName(name = name, serviceOrderId = order.id.value)
        ) {
            throw CatalogException.ServiceAlreadyExists(
                message = "Service ${name.value} already exists.",
            )
        }
        val extra = ExtraService.register(
            serviceOrderId = order.id.value,
            name = name,
            price = Money.of(raw = input.basePrice),
        )
        extras.save(extra = extra)
        events.publish(aggregate = extra)
        return extra.toResponse()
    }

    private companion object {
        val allowedStatuses = setOf(
            ServiceOrderStatus.BUDGET_APPROVED,
            ServiceOrderStatus.IN_EXECUTION,
        )
    }
}
