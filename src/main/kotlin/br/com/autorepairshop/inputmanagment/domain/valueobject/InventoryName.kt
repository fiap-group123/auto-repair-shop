package br.com.autorepairshop.inputmanagment.domain.valueobject

import br.com.autorepairshop.inputmanagment.domain.exception.InventoryException
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class InventoryName private constructor(val value: String) : ValueObject {
    companion object {
        private val WHITESPACE = Regex(pattern = "\\s+")

        fun of(raw: String): InventoryName {
            val normalized = raw.trim().replace(
                regex = WHITESPACE,
                replacement = " ",
            )
            if (normalized.length !in 2..60) {
                throw InventoryException.InvalidInventoryName(
                    message = "Inventory name must be between 2 and 60 characters.",
                )
            }
            return InventoryName(value = normalized)
        }
    }
}
