package br.com.autorepairshop.customer.domain.valueobject.customer

import br.com.autorepairshop.shared.domain.ValueObject
import java.util.UUID

@JvmInline
value class CustomerId(val value: UUID) : ValueObject {
    companion object {
        fun generate(): CustomerId = CustomerId(UUID.randomUUID())
    }
}
