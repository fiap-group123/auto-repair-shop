package br.com.autorepairshop.catalog.domain.repository

import br.com.autorepairshop.catalog.domain.aggregate.Service
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName

interface OfferedServiceRepository {
    fun save(service: Service)
    fun findById(id: ServiceId): Service?
    fun existsByName(name: ServiceName): Boolean
    fun findAll(): List<Service>
}
