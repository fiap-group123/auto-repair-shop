package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.catalog.application.dto.ServiceResponse
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.repository.ServiceRepository
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListServicesByCustomerIdUseCase(
    private val orders: ServiceOrderRepository,
    private val services: ServiceRepository,
    private val access: AccessGuard,
) : UseCase<UUID, List<ServiceResponse>> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): List<ServiceResponse> {
        access.requireCustomer(customerId = input)
        val orderIds = orders.findByCustomerId(customerId = input).map { it.id.value }
        return services.findByServiceOrderIds(serviceOrderIds = orderIds).map { it.toResponse() }
    }
}
