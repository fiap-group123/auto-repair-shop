package br.com.autorepairshop.serviceorder.domain.repository

import br.com.autorepairshop.serviceorder.domain.aggregate.ServiceOrder
import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import java.util.UUID

interface ServiceOrderRepository {
    fun save(order: ServiceOrder)
    fun findById(id: ServiceOrderId): ServiceOrder?
    fun findAll(): List<ServiceOrder>
    fun findByCustomerId(customerId: UUID): List<ServiceOrder>
    fun existsOpenByVehicleId(vehicleId: UUID): Boolean
}
