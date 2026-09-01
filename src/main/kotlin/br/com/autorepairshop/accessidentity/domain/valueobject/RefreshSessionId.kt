package br.com.autorepairshop.accessidentity.domain.valueobject

import br.com.autorepairshop.shared.domain.ValueObject
import java.util.UUID

@JvmInline
value class RefreshSessionId(val value: UUID) : ValueObject {
    companion object {
        fun generate(): RefreshSessionId = RefreshSessionId(value = UUID.randomUUID())
    }
}
