package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.inputmanagment.application.dto.PartResponse
import br.com.autorepairshop.inputmanagment.application.dto.toResponse
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ListPartsByServiceOrderIdUseCase(
    private val parts: PartRepository,
    private val orders: ServiceOrderRepository,
    private val access: AccessGuard,
) : UseCase<UUID, List<PartResponse>> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): List<PartResponse> {
        val order = orders.findById(id = ServiceOrderId(value = input))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order $input was not found.",
            )
        access.requireCustomer(customerId = order.customerId)
        return parts.findByServiceOrderId(serviceOrderId = input).map { it.toResponse() }
    }
}
