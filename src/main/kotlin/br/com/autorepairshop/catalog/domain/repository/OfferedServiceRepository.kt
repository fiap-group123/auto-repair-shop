package br.com.autorepairshop.catalog.domain.repository

import br.com.autorepairshop.catalog.domain.aggregate.OfferedService
import br.com.autorepairshop.catalog.domain.valueobject.OfferedServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName

interface OfferedServiceRepository {
    fun save(service: OfferedService)
    fun findById(id: OfferedServiceId): OfferedService?
    fun existsByName(name: ServiceName): Boolean
    fun findAll(): List<OfferedService>
}
