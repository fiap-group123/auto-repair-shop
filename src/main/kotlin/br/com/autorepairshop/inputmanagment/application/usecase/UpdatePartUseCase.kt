package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.application.dto.PartResponse
import br.com.autorepairshop.inputmanagment.application.dto.UpdatePartCommand
import br.com.autorepairshop.inputmanagment.application.dto.toResponse
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.PartId
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.application.event.EventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdatePartUseCase(
    private val serviceOrders: ServiceOrderRepository,
    private val inventories: InventoryRepository,
    private val parts: PartRepository,
    private val events: EventPublisher,
) : UseCase<UpdatePartCommand, PartResponse> {

    @Transactional
    override fun execute(input: UpdatePartCommand): PartResponse {
        val part = parts.findById(id = PartId(value = input.partId))
            ?: throw InventoryException.PartNotFound(message = "Part ${input.partId} was not found.")
        val order = serviceOrders.findById(id = ServiceOrderId(value = part.serviceOrderId))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order ${part.serviceOrderId} was not found.",
            )
        if (order.status !in allowedStatuses) {
            throw InventoryException.InvalidStatusTransition(
                message = "Cannot change a part from ${order.status.name}.",
            )
        }
        val inventory = inventories.findById(id = part.inventoryId)
            ?: throw InventoryException.InventoryNotFound(
                message = "Inventory ${part.inventoryId.value} was not found.",
            )
        inventory.adjustStock(delta = part.quantity - input.quantity)
        part.changeQuantity(newQuantity = input.quantity)
        inventories.save(inventory = inventory)
        parts.save(part = part)
        events.publish(aggregate = part)
        return part.toResponse()
    }

    private companion object {
        val allowedStatuses = setOf(
            ServiceOrderStatus.RECEIVED,
            ServiceOrderStatus.IN_DIAGNOSIS,
            ServiceOrderStatus.WAITING_APPROVAL,
        )
    }
}
