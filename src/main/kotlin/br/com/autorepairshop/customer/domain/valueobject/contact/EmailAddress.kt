package br.com.autorepairshop.customer.domain.valueobject.contact

import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class EmailAddress private constructor(val value: String) : ValueObject {
    companion object {
        fun of(raw: String): EmailAddress {
            val address = raw.trim().replace(Regex("\\s+"), " ").lowercase()
            if (address.length !in 5..60) {
                throw CustomerException.InvalidEmailAddress(
                    message = "Email address must be between 5 and 60 characters.",
                )
            }
            if (!Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$").matches(address)) {
                throw CustomerException.InvalidEmailAddress(message = "Invalid email address format.")
            }
            return EmailAddress(value = address)
        }
    }
}
