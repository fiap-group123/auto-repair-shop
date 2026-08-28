package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderAssembler
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ListServiceOrdersByCustomerIdUseCase(
    private val orders: ServiceOrderRepository,
    private val responses: ServiceOrderAssembler,
) : UseCase<UUID, List<ServiceOrderResponse>> {
    override fun execute(input: UUID): List<ServiceOrderResponse> {
        val found = orders.findByCustomerId(customerId = input)
        if (found.isEmpty()) {
            throw ServiceOrderException.ServiceOrderNotFound(
                message = "No service orders found for customer ID: $input",
            )
        }
        return responses.toResponses(orders = found)
    }
}
