package br.com.autorepairshop.inputmanagment.application.usecase

import br.com.autorepairshop.inputmanagment.application.dto.InventoryResponse
import br.com.autorepairshop.inputmanagment.application.dto.RegisterInventoryCommand
import br.com.autorepairshop.inputmanagment.application.dto.toResponse
import br.com.autorepairshop.inputmanagment.domain.aggregate.Inventory
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.repository.InventoryRepository
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryType
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryName
import br.com.autorepairshop.shared.application.UseCase
import br.com.autorepairshop.shared.domain.Money
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterInventoryUseCase(private val inventories: InventoryRepository) :
    UseCase<RegisterInventoryCommand, InventoryResponse> {

    @Transactional
    override fun execute(input: RegisterInventoryCommand): InventoryResponse {
        val name = InventoryName.of(raw = input.name)
        if (inventories.existsByName(name = name)) {
            throw InventoryException.InventoryAlreadyExists(message = "Inventory ${name.value} already exists.")
        }
        val inventory = Inventory.register(
            name = name,
            type = kindOf(raw = input.kind),
            unitPrice = Money.of(raw = input.unitPrice),
            stock = input.stock,
        )
        inventories.save(inventory = inventory)
        return inventory.toResponse()
    }
}

internal fun kindOf(raw: String): InventoryType =
    InventoryType.entries.find { kind -> kind.name == raw }
        ?: throw InventoryException.InvalidInventoryName(message = "Unknown inventory kind $raw.")
