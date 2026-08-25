package br.com.autorepairshop.customer.domain.valueobject.vehicle

import br.com.autorepairshop.shared.domain.ValueObject
import java.util.UUID

@JvmInline
value class VehicleId(val value: UUID) : ValueObject {
    companion object {
        fun generate() = VehicleId(value = UUID.randomUUID())
    }
}
