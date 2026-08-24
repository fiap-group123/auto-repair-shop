package br.com.autorepairshop.customer.domain.exception

import br.com.autorepairshop.shared.domain.exception.DomainException

sealed class CustomerException(message: String) : DomainException(message) {
    class CustomerNotFound(message: String) : CustomerException(message)
    class CustomerAlreadyExists(message: String) : CustomerException(message)
    class CustomerAlreadyActive(message: String) : CustomerException(message)
    class InvalidDocument(message: String) : CustomerException(message)
    class InvalidPersonName(message: String) : CustomerException(message)
    class InvalidPhoneNumber(message: String) : CustomerException(message)
    class InvalidEmailAddress(message: String) : CustomerException(message)
}