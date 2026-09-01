package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.accessidentity.application.security.AccessGuard
import br.com.autorepairshop.inputmanagment.application.dto.PartResponse
import br.com.autorepairshop.inputmanagment.application.dto.toResponse
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.PartId
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FindPartUseCase(
    private val parts: PartRepository,
    private val orders: ServiceOrderRepository,
    private val access: AccessGuard,
) : UseCase<UUID, PartResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): PartResponse {
        val part = parts.findById(id = PartId(value = input))
            ?: throw InventoryException.PartNotFound(message = "Part $input was not found.")
        val order = orders.findById(id = ServiceOrderId(value = part.serviceOrderId))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order ${part.serviceOrderId} was not found.",
            )
        access.requireCustomer(customerId = order.customerId)
        return part.toResponse()
    }
}
