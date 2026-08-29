package br.com.autorepairshop.serviceorder.domain.exception

import br.com.autorepairshop.shared.domain.exception.DomainException

sealed class ServiceOrderException(message: String) : DomainException(message = message) {
    class ServiceOrderNotFound(message: String) : ServiceOrderException(message = message)
    class OpenOrderAlreadyExists(message: String) : ServiceOrderException(message = message)
    class VehicleNotOwnedByCustomer(message: String) : ServiceOrderException(message = message)
    class InvalidStatusTransition(message: String) : ServiceOrderException(message = message)
    class EmptyBudget(message: String) : ServiceOrderException(message = message)
}
