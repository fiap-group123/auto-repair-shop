package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.application.dto.PartResponse
import br.com.autorepairshop.inputmanagment.application.dto.RegisterPartCommand
import br.com.autorepairshop.inputmanagment.application.dto.toResponse
import br.com.autorepairshop.inputmanagment.domain.aggregate.Part
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.repository.PartRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import br.com.autorepairshop.serviceandexecution.domain.exception.ServiceOrderException
import br.com.autorepairshop.serviceandexecution.domain.repository.ServiceOrderRepository
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.serviceandexecution.domain.valueobject.ServiceOrderStatus
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.application.event.EventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterPartUseCase(
    private val serviceOrders: ServiceOrderRepository,
    private val inventories: InventoryRepository,
    private val parts: PartRepository,
    private val events: EventPublisher,
) : UseCase<RegisterPartCommand, PartResponse> {

    @Transactional
    override fun execute(input: RegisterPartCommand): PartResponse {
        val order = serviceOrders.findById(id = ServiceOrderId(value = input.serviceOrderId))
            ?: throw ServiceOrderException.ServiceOrderNotFound(
                message = "Service order ${input.serviceOrderId} was not found.",
            )
        if (order.status !in allowedStatuses) {
            throw InventoryException.InvalidStatusTransition(
                message = "Cannot register a part from ${order.status.name}.",
            )
        }
        val inventory = inventories.findById(id = InventoryId(value = input.inventoryId))
            ?: throw InventoryException.InventoryNotFound(message = "Inventory ${input.inventoryId} was not found.")
        if (parts.existsByInventoryId(inventoryId = inventory.id, serviceOrderId = order.id.value)) {
            throw InventoryException.PartAlreadyExists(
                message = "Part ${inventory.name.value} already exists on this order.",
            )
        }
        inventory.adjustStock(delta = -input.quantity)
        val part = Part.register(
            serviceOrderId = order.id.value,
            inventoryId = inventory.id,
            quantity = input.quantity,
            unitPrice = inventory.unitPrice,
        )
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
