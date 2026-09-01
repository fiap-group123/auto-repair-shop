package br.com.autorepairshop.accessidentity.domain.valueobject

import br.com.autorepairshop.shared.domain.ValueObject
import java.util.UUID

@JvmInline
value class CustomerInviteId(val value: UUID) : ValueObject {
    companion object {
        fun generate(): CustomerInviteId = CustomerInviteId(value = UUID.randomUUID())
    }
}
