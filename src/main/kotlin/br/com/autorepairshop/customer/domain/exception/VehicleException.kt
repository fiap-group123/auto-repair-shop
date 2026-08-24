package br.com.autorepairshop.customer.domain.exception

import br.com.autorepairshop.shared.domain.exception.DomainException

sealed class VehicleException(message: String) : DomainException(message) {
    class VehicleNotFound(message: String) : VehicleException(message)
    class VehicleAlreadyExists(message: String) : VehicleException(message)
    class InvalidLicensePlate(message: String) : VehicleException(message)
    class InvalidModelYear(message: String) : VehicleException(message)
    class InvalidVehicleName(message: String) : VehicleException(message)
    class AlreadyOwnedByCustomer(message: String) : VehicleException(message)
}