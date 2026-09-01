package br.com.autorepairshop.inputmanagment.domain.valueobject

import br.com.autorepairshop.shared.domain.ValueObject
import java.util.UUID

@JvmInline
value class InventoryId(val value: UUID) : ValueObject {
    companion object {
        fun generate(): InventoryId = InventoryId(value = UUID.randomUUID())
    }
}
