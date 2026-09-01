package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.application.dto.InventoryResponse
import br.com.autorepairshop.inputmanagment.application.dto.UpdateInventoryCommand
import br.com.autorepairshop.inputmanagment.application.dto.toResponse
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryName
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UpdateInventoryUseCase(private val inventories: InventoryRepository) :
    UseCase<UpdateInventoryCommand, InventoryResponse> {

    @Transactional
    override fun execute(input: UpdateInventoryCommand): InventoryResponse {
        val inventory = inventories.findById(id = InventoryId(value = input.inventoryId))
            ?: throw InventoryException.InventoryNotFound(message = "Inventory ${input.inventoryId} was not found.")
        input.name?.let { raw ->
            val newName = InventoryName.of(raw = raw)
            if (newName != inventory.name && inventories.existsByName(name = newName)) {
                throw InventoryException.InventoryAlreadyExists(message = "Inventory ${newName.value} already exists.")
            }
            inventory.rename(newName = newName)
        }
        input.unitPrice?.let { inventory.reprice(newUnitPrice = Money.of(raw = it)) }
        input.kind?.let { inventory.changeKind(newType = kindOf(raw = it)) }
        inventories.save(inventory = inventory)
        return inventory.toResponse()
    }
}
