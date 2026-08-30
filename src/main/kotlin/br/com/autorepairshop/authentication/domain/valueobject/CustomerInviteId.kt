package br.com.autorepairshop.authentication.domain.valueobject

import br.com.autorepairshop.shared.domain.ValueObject
import java.util.UUID

@JvmInline
value class CustomerInviteId(val value: UUID) : ValueObject {
    companion object {
        fun generate(): CustomerInviteId = CustomerInviteId(value = UUID.randomUUID())
    }
}
