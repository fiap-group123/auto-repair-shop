package br.com.autorepairshop.inputmanagment

import br.com.autorepairshop.inputmanagment.domain.aggregate.Inventory
import br.com.autorepairshop.inputmanagment.domain.aggregate.Part
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryType
import br.com.autorepairshop.inputmanagment.domain.valueobject.InventoryName
import br.com.autorepairshop.shared.domain.Money
import java.math.BigDecimal
import java.util.UUID

object InventoryFixtures {
    const val NAME = "Filtro de oleo"
    const val OTHER_NAME = "Oleo 5W30"
    const val PRICE = "45.00"
    const val STOCK = 10

    fun money(raw: String = PRICE): Money = Money.of(raw = BigDecimal(raw))

    fun inventory(
        name: String = NAME,
        kind: InventoryType = InventoryType.PART,
        price: String = PRICE,
        stock: Int = STOCK,
    ): Inventory = Inventory.register(
        name = InventoryName.of(raw = name),
        type = kind,
        unitPrice = money(raw = price),
        stock = stock,
    )

    fun part(
        serviceOrderId: UUID = UUID.randomUUID(),
        inventory: Inventory = inventory(),
        quantity: Int = 2,
    ): Part = Part.register(
        serviceOrderId = serviceOrderId,
        inventoryId = inventory.id,
        quantity = quantity,
        unitPrice = inventory.unitPrice,
    )
}
