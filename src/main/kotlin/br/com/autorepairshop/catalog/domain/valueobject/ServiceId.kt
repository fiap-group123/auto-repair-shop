package br.com.autorepairshop.catalog.domain.valueobject

import br.com.autorepairshop.shared.domain.ValueObject
import java.util.UUID

@JvmInline
value class ServiceId(val value: UUID) : ValueObject {
    companion object {
        fun generate(): ServiceId = ServiceId(value = UUID.randomUUID())
    }
}
