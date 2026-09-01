package br.com.autorepairshop.catalog.application.usecase

import br.com.autorepairshop.authentication.application.security.AccessGuard
import br.com.autorepairshop.catalog.application.dto.ExtraServiceResponse
import br.com.autorepairshop.catalog.application.dto.toResponse
import br.com.autorepairshop.catalog.domain.repository.ExtraServiceRepository
import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceorder.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListExtraServicesByServiceOrderIdUseCase(
    private val extras: ExtraServiceRepository,
    private val orders: ServiceOrderRepository,
    private val access: AccessGuard,
) : UseCase<UUID, List<ExtraServiceResponse>> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): List<ExtraServiceResponse> {
        val order = orders.findById(id = ServiceOrderId(value = input))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order $input was not found.",
            )
        access.requireCustomer(customerId = order.customerId)
        return extras.findByServiceOrderId(serviceOrderId = input).map { it.toResponse() }
    }
}
