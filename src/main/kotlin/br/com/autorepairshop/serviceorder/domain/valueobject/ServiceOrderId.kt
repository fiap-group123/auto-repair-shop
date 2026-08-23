package br.com.autorepairshop.serviceorder.domain.valueobject

import br.com.autorepairshop.shared.domain.ValueObject
import java.util.UUID

@JvmInline
value class ServiceOrderId(val value: UUID) : ValueObject {
    companion object {
        fun generate() = ServiceOrderId(UUID.randomUUID())
    }
}

@JvmInline value class CustomerId(val value: UUID) : ValueObject
@JvmInline value class VehicleId(val value: UUID) : ValueObject
@JvmInline value class ServiceId(val value: UUID) : ValueObject
@JvmInline value class EstimateId(val value: UUID) : ValueObject