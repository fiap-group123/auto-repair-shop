package br.com.autorepairshop.customer.domain.valueobject.contact

import br.com.autorepairshop.shared.domain.ValueObject

data class ContactInfo(
    val email: EmailAddress,
    val phone: PhoneNumber
) : ValueObject {
    companion object {
        fun of(email: String, phone: String): ContactInfo {
            return ContactInfo(
                email = EmailAddress.of(email),
                phone = PhoneNumber.of(phone)
            )
        }
    }
}
