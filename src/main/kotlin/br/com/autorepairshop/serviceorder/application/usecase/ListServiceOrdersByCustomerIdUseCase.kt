package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceorder.application.dto.toResponse
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ListServiceOrdersByCustomerIdUseCase(private val orders: ServiceOrderRepository) :
    UseCase<UUID, List<ServiceOrderResponse>> {
    override fun execute(input: UUID): List<ServiceOrderResponse> {
        val orders = orders.findByCustomerId(input)
        if (orders.isEmpty()) {
            throw ServiceOrderException.ServiceOrderNotFound("No service orders found for customer ID: $input")
        }
        return orders.map { it.toResponse() }
    }
}
