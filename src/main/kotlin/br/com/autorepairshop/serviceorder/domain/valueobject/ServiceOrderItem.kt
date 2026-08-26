package br.com.autorepairshop.serviceorder.domain.valueobject

import br.com.autorepairshop.serviceorder.domain.exception.ServiceOrderException
import br.com.autorepairshop.shared.domain.Money
import br.com.autorepairshop.shared.domain.ValueObject
import java.util.UUID

data class ServiceOrderItem(
    val offeredServiceId: UUID,
    val description: String,
    val unitPrice: Money,
    val quantity: Int,
) : ValueObject {

    init {
        if (quantity !in MIN_QUANTITY..MAX_QUANTITY) {
            throw ServiceOrderException.InvalidQuantity(
                message = "Quantity must be between $MIN_QUANTITY and $MAX_QUANTITY.",
            )
        }
    }

    fun subtotal(): Money = unitPrice.times(quantity = quantity)

    companion object {
        private const val MIN_QUANTITY = 1
        private const val MAX_QUANTITY = 99
    }
}
