package br.com.autorepairshop.customer.valueobject.contact

import br.com.autorepairshop.customer.exception.CustomerException
import br.com.autorepairshop.shared.domain.ValueObject

@JvmInline
value class PhoneNumber private constructor(val value: String) : ValueObject {
    companion object {
        fun of(raw: String): PhoneNumber {
            val digits = raw.filter { it.isDigit() }
            if (digits.length !in 10..11) {
                throw CustomerException.InvalidPhoneNumber(message = "Phone number must have 10 or 11 digits including area code")
            }
            return PhoneNumber(value = digits)
        }
    }
}
