package br.com.autorepairshop.serviceandexecution.domain.valueobject

import br.com.autorepairshop.shared.domain.ValueObject
import java.util.UUID

@JvmInline
value class ServiceOrderId(val value: UUID) : ValueObject {
    companion object {
        fun generate(): ServiceOrderId = ServiceOrderId(value = UUID.randomUUID())
    }
}
