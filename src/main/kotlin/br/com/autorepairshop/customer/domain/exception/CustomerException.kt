package br.com.autorepairshop.customer.domain.exception

import br.com.autorepairshop.shared.domain.exception.DomainException

sealed class CustomerException(message: String) : DomainException(message = message) {
    class CustomerNotFound(message: String) : CustomerException(message = message)
    class CustomerAlreadyExists(message: String) : CustomerException(message = message)
    class CustomerAlreadyActive(message: String) : CustomerException(message = message)
    class InvalidDocument(message: String) : CustomerException(message = message)
    class InvalidPersonName(message: String) : CustomerException(message = message)
    class InvalidPhoneNumber(message: String) : CustomerException(message = message)
    class InvalidEmailAddress(message: String) : CustomerException(message = message)
    class CustomerInactive(message: String): CustomerException(message = message)
}
