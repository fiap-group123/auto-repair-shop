package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.serviceorder.application.dto.RemoveServiceOrderItemCommand
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceorder.application.dto.toResponse
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RemoveServiceOrderItemUseCase(private val orders: ServiceOrderRepository) :
    UseCase<RemoveServiceOrderItemCommand, ServiceOrderResponse> {

    @Transactional
    override fun execute(input: RemoveServiceOrderItemCommand): ServiceOrderResponse {
        val order = orders.findById(id = ServiceOrderId(value = input.serviceOrderId))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order ${input.serviceOrderId} was not found.",
            )
        order.removeItem(offeredServiceId = input.offeredServiceId)
        orders.save(order = order)
        return order.toResponse()
    }
}
