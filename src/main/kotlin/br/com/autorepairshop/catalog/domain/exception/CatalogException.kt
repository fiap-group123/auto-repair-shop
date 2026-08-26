package br.com.autorepairshop.catalog.domain.exception

import br.com.autorepairshop.shared.domain.exception.DomainException

sealed class CatalogException(message: String) : DomainException(message = message) {
    class ServiceNotFound(message: String) : CatalogException(message = message)
    class ServiceAlreadyExists(message: String) : CatalogException(message = message)
    class ServiceAlreadyActive(message: String) : CatalogException(message = message)
    class ServiceInactive(message: String) : CatalogException(message = message)
    class InvalidServiceName(message: String) : CatalogException(message = message)
}
