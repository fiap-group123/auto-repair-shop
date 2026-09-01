package br.com.autorepairshop.serviceandexecution.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOrderAssembler
import br.com.autorepairshop.serviceandexecution.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
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
