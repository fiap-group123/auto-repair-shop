package br.com.autorepairshop.customer.domain.exception

import br.com.autorepairshop.shared.domain.exception.DomainException

sealed class VehicleException(message: String) : DomainException(message = message) {
    class VehicleNotFound(message: String) : VehicleException(message = message)
    class VehicleAlreadyExists(message: String) : VehicleException(message = message)
    class InvalidLicensePlate(message: String) : VehicleException(message = message)
    class InvalidModelYear(message: String) : VehicleException(message = message)
    class InvalidVehicleName(message: String) : VehicleException(message = message)
    class AlreadyOwnedByCustomer(message: String) : VehicleException(message = message)
    class VehicleInactive(message: String) : VehicleException(message = message)
    class VehicleAlreadyActive(message: String) : VehicleException(message = message)
}
