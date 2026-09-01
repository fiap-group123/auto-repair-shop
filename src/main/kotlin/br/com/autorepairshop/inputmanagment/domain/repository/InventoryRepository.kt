package br.com.autorepairshop.inputmanagment.domain.repository

import br.com.autorepairshop.inputmanagment.domain.aggregate.Inventory
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryName

interface InventoryRepository {
    fun save(inventory: Inventory)
    fun findById(id: InventoryId): Inventory?
    fun findAll(): List<Inventory>
    fun existsByName(name: InventoryName): Boolean
}
