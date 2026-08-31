package br.com.autorepairshop.serviceorder.application.usecase

import br.com.autorepairshop.authentication.application.security.AccessGuard
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderAssembler
import br.com.autorepairshop.serviceorder.application.dto.ServiceOrderResponse
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListServiceOrdersByCustomerIdUseCase(
    private val orders: ServiceOrderRepository,
    private val responses: ServiceOrderAssembler,
    private val access: AccessGuard,
) : UseCase<UUID, List<ServiceOrderResponse>> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): List<ServiceOrderResponse> {
        access.requireCustomer(customerId = input)
        return responses.toResponses(orders = orders.findByCustomerId(customerId = input))
    }
}
