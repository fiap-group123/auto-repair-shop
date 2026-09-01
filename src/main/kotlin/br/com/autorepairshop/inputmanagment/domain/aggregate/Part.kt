package br.com.autorepairshop.inputmanagment.domain.aggregate

import br.com.autorepairshop.inputmanagment.domain.event.PartQuantityChanged
import br.com.autorepairshop.inputmanagment.domain.event.PartRegistered
import br.com.autorepairshop.inputmanagment.domain.event.PartRemoved
import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryId
import br.com.autorepairshop.inputmanagment.domain.valueobject.PartId
import br.com.autorepairshop.shared.domain.AggregateRoot
import br.com.autorepairshop.shared.domain.Money
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class Part private constructor(
    id: PartId,
    val serviceOrderId: UUID,
    val inventoryId: InventoryId,
    quantity: Int,
    unitPrice: Money,
    val createdAt: Instant,
) : AggregateRoot<PartId>(id = id) {

    var quantity: Int = quantity
        private set

    var unitPrice: Money = unitPrice
        private set

    fun lineTotal(): Money = unitPrice.times(quantity = quantity)

    fun changeQuantity(
        newQuantity: Int,
        at: Instant = Clock.System.now(),
    ) {
        requirePositive(quantity = newQuantity)
        quantity = newQuantity
        registerEvent(
            event = PartQuantityChanged(
                partId = id,
                serviceOrderId = serviceOrderId,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    fun remove(at: Instant = Clock.System.now()) {
        registerEvent(
            event = PartRemoved(
                partId = id,
                serviceOrderId = serviceOrderId,
                occurredOn = at.toJavaInstant(),
            ),
        )
    }

    private fun recordRegistered() {
        registerEvent(
            event = PartRegistered(
                partId = id,
                serviceOrderId = serviceOrderId,
                occurredOn = createdAt.toJavaInstant(),
            ),
        )
    }

    companion object {
        fun register(
            serviceOrderId: UUID,
            inventoryId: InventoryId,
            quantity: Int,
            unitPrice: Money,
            createdAt: Instant = Clock.System.now(),
        ): Part {
            requirePositive(quantity = quantity)
            val part = Part(
                id = PartId.generate(),
                serviceOrderId = serviceOrderId,
                inventoryId = inventoryId,
                quantity = quantity,
                unitPrice = unitPrice,
                createdAt = createdAt,
            )
            part.recordRegistered()
            return part
        }

        internal fun rehydrate(
            id: PartId,
            serviceOrderId: UUID,
            inventoryId: InventoryId,
            quantity: Int,
            unitPrice: Money,
            createdAt: Instant,
        ) = Part(
            id = id,
            serviceOrderId = serviceOrderId,
            inventoryId = inventoryId,
            quantity = quantity,
            unitPrice = unitPrice,
            createdAt = createdAt,
        )

        private fun requirePositive(quantity: Int) {
            if (quantity < 1) {
                throw InventoryException.InvalidQuantity(message = "Quantity must be at least 1.")
            }
        }
    }
}
