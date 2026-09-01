package br.com.autorepairshop.inputmanagment.domain.exception

import br.com.autorepairshop.shared.domain.exception.DomainException

sealed class InventoryException(message: String) : DomainException(message = message) {
    class InventoryNotFound(message: String) : InventoryException(message = message)
    class PartNotFound(message: String) : InventoryException(message = message)
    class InventoryAlreadyExists(message: String) : InventoryException(message = message)
    class PartAlreadyExists(message: String) : InventoryException(message = message)
    class InsufficientStock(message: String) : InventoryException(message = message)
    class InventoryInactive(message: String) : InventoryException(message = message)
    class InventoryAlreadyActive(message: String) : InventoryException(message = message)
    class InvalidInventoryName(message: String) : InventoryException(message = message)
    class InvalidQuantity(message: String) : InventoryException(message = message)
    class InvalidStatusTransition(message: String) : InventoryException(message = message)
}
