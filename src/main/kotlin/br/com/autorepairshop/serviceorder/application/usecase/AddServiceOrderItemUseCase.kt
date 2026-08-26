package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.catalog.domain.exception.CatalogException
import br.com.autorepairshop.catalog.domain.repository.OfferedServiceRepository
import br.com.autorepairshop.catalog.domain.valueobject.OfferedServiceId
import br.com.autorepairshop.serviceorder.application.dto.AddServiceOrderItemCommand
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceorder.application.dto.toResponse
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderItem
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AddServiceOrderItemUseCase(
    private val orders: ServiceOrderRepository,
    private val services: OfferedServiceRepository,
) : UseCase<AddServiceOrderItemCommand, ServiceOrderResponse> {

    @Transactional
    override fun execute(input: AddServiceOrderItemCommand): ServiceOrderResponse {
        val order = orders.findById(id = ServiceOrderId(value = input.serviceOrderId))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order ${input.serviceOrderId} was not found.",
            )
        val service = services.findById(id = OfferedServiceId(value = input.offeredServiceId))
            ?: throw CatalogException.ServiceNotFound(
                message = "Service ${input.offeredServiceId} was not found.",
            )
        if (!service.active) {
            throw CatalogException.ServiceInactive(
                message = "Service ${service.name.value} is inactive.",
            )
        }
        order.addItem(
            item = ServiceOrderItem(
                offeredServiceId = service.id.value,
                description = service.name.value,
                unitPrice = service.price,
                quantity = input.quantity,
            ),
        )
        orders.save(order = order)
        return order.toResponse()
    }
}
