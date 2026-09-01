package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.application.dto.InventoryResponse
import br.com.autorepairshop.inputmanagment.application.dto.AdjustInventoryStockCommand
import br.com.autorepairshop.inputmanagment.application.dto.toResponse
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdjustInventoryStockUseCase(private val inventories: InventoryRepository) :
    UseCase<AdjustInventoryStockCommand, InventoryResponse> {

    @Transactional
    override fun execute(input: AdjustInventoryStockCommand): InventoryResponse {
        val inventory = inventories.findById(id = InventoryId(value = input.inventoryId))
            ?: throw InventoryException.InventoryNotFound(message = "Inventory ${input.inventoryId} was not found.")
        inventory.setStock(quantity = input.quantity)
        inventories.save(inventory = inventory)
        return inventory.toResponse()
    }
}
