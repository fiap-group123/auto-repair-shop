package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.application.dto.InventoryResponse
import br.com.autorepairshop.inputmanagment.application.dto.toResponse
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import br.com.autorepairshop.shared.application.UseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FindInventoryUseCase(private val inventories: InventoryRepository) : UseCase<UUID, InventoryResponse> {

    @Transactional(readOnly = true)
    override fun execute(input: UUID): InventoryResponse {
        val inventory = inventories.findById(id = InventoryId(value = input))
            ?: throw InventoryException.InventoryNotFound(message = "Inventory $input was not found.")
        return inventory.toResponse()
    }
}
