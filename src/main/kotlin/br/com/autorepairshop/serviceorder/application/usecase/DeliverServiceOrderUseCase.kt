package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderAssembler
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.application.event.EventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeliverServiceOrderUseCase(
    private val orders: ServiceOrderRepository,
    private val events: EventPublisher,
    private val responses: ServiceOrderAssembler,
) : UseCase<UUID, ServiceOrderResponse> {

    @Transactional
    override fun execute(input: UUID): ServiceOrderResponse {
        val order = orders.findById(id = ServiceOrderId(value = input))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order $input was not found.",
            )
        order.deliver()
        orders.save(order = order)
        events.publish(aggregate = order)
        return responses.toResponse(order = order)
    }
}
