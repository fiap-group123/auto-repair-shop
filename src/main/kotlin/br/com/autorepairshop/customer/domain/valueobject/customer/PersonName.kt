package br.com.autorepairshop.customer.domain.valueobject.customer

import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class PersonName private constructor(val value: String) : ValueObject {
    companion object {
        fun of(raw: String): PersonName {
            val normalized = raw.trim().replace(Regex("\\s+"), " ")
            if (normalized.length !in 2..60) {
                throw CustomerException.InvalidPersonName(message = "Name must be between 2 and 60 characters.")
            }
            return PersonName(value = normalized)
        }
    }
}
