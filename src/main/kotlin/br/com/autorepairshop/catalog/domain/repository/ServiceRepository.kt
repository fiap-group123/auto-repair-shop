package br.com.autorepairshop.catalog.domain.repository

import br.com.autorepairshop.catalog.domain.aggregate.Service
import br.com.autorepairshop.catalog.domain.valueobject.ServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import java.util.UUID

interface ServiceRepository {
    fun save(service: Service)
    fun findById(id: ServiceId): Service?
    fun existsByName(
        name: ServiceName,
        serviceOrderId: UUID,
    ): Boolean
    fun findAll(): List<Service>
    fun findByServiceOrderId(serviceOrderId: UUID): List<Service>
    fun findByServiceOrderIds(serviceOrderIds: Collection<UUID>): List<Service>
    fun existsByServiceOrderId(serviceOrderId: UUID): Boolean
}
