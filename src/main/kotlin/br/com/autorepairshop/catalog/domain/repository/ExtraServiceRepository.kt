package br.com.autorepairshop.catalog.domain.repository

import br.com.autorepairshop.catalog.domain.aggregate.ExtraService
import br.com.autorepairshop.catalog.domain.valueobject.ExtraServiceId
import br.com.autorepairshop.catalog.domain.valueobject.ServiceName
import java.util.UUID

interface ExtraServiceRepository {
    fun save(extra: ExtraService)
    fun findById(id: ExtraServiceId): ExtraService?
    fun existsByName(
        name: ServiceName,
        serviceOrderId: UUID,
    ): Boolean
    fun findByServiceOrderId(serviceOrderId: UUID): List<ExtraService>
}
