package br.com.autorepairshop.inputmanagment.domain.aggregate

import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryName
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryType
import br.com.autorepairshop.shared.domain.AggregateRoot
import br.com.autorepairshop.shared.domain.Money
import kotlin.time.Clock
import kotlin.time.Instant

class Inventory private constructor(
    id: InventoryId,
    name: InventoryName,
    type: InventoryType,
    unitPrice: Money,
    stock: Int,
    active: Boolean,
    val createdAt: Instant,
) : AggregateRoot<InventoryId>(id = id) {

    var name: InventoryName = name
        private set

    var type: InventoryType = type
        private set

    var unitPrice: Money = unitPrice
        private set

    var stock: Int = stock
        private set

    var active: Boolean = active
        private set

    fun rename(newName: InventoryName) {
        requireActive()
        name = newName
    }

    fun reprice(newUnitPrice: Money) {
        requireActive()
        unitPrice = newUnitPrice
    }

    fun changeKind(newType: InventoryType) {
        requireActive()
        type = newType
    }

    fun setStock(quantity: Int) {
        requireActive()
        if (quantity < 0) {
            throw InventoryException.InsufficientStock(message = "Stock cannot be negative.")
        }
        stock = quantity
    }

    fun adjustStock(delta: Int) {
        if (delta < 0) {
            requireActive()
        }
        val next = stock + delta
        if (next < 0) {
            throw InventoryException.InsufficientStock(
                message = "Insufficient stock for ${name.value}.",
            )
        }
        stock = next
    }

    fun deactivate() {
        requireActive()
        active = false
    }

    fun reactivate() {
        if (active) {
            throw InventoryException.InventoryAlreadyActive(message = "Inventory item is already active.")
        }
        active = true
    }

    private fun requireActive() {
        if (!active) {
            throw InventoryException.InventoryInactive(message = "Inventory item ${name.value} is inactive.")
        }
    }

    companion object {
        fun register(
            name: InventoryName,
            type: InventoryType,
            unitPrice: Money,
            stock: Int,
            createdAt: Instant = Clock.System.now(),
        ): Inventory {
            if (stock < 0) {
                throw InventoryException.InsufficientStock(message = "Stock cannot be negative.")
            }
            return Inventory(
                id = InventoryId.generate(),
                name = name,
                type = type,
                unitPrice = unitPrice,
                stock = stock,
                active = true,
                createdAt = createdAt,
            )
        }

        internal fun rehydrate(
            id: InventoryId,
            name: InventoryName,
            type: InventoryType,
            unitPrice: Money,
            stock: Int,
            active: Boolean,
            createdAt: Instant,
        ) = Inventory(
            id = id,
            name = name,
            type = type,
            unitPrice = unitPrice,
            stock = stock,
            active = active,
            createdAt = createdAt,
        )
    }
}
