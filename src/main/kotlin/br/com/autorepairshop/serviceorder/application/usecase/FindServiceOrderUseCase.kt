package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.authentication.application.security.AccessGuard
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderAssembler
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FindServiceOrderUseCase(
    private val orders: ServiceOrderRepository,
    private val responses: ServiceOrderAssembler,
    private val access: AccessGuard,
) : UseCase<UUID, ServiceOrderResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): ServiceOrderResponse {
        val order = orders.findById(id = ServiceOrderId(value = input))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order $input was not found.",
            )
        access.requireCustomer(customerId = order.customerId)
        return responses.toResponse(order = order)
    }
}
