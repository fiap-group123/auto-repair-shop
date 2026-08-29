package br.com.autorepairshop.customer.domain.valueobject.contact

import br.com.autorepairshop.customer.domain.exception.CustomerException
import br.com.autorepairshop.shared.domain.Email
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class EmailAddress private constructor(val value: String) : ValueObject {
    companion object {
        fun of(raw: String): EmailAddress = EmailAddress(
            value = Email.of(
                raw = raw,
                invalid = { CustomerException.InvalidEmailAddress(message = it) },
            ).value,
        )
    }
}
