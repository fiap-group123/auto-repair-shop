package br.com.autorepairshop.serviceorder.domain

import br.com.autorepairshop.serviceorder.domain.valueobject.ServiceOrderId
import br.com.autorepairshop.shared.domain.AggregateRoot

class ServiceOrder private constructor(
    id: ServiceOrderId,
    val clientId: String,
    val vehicleId: String,
    val services: List<String>,
    val toolsAndSupplies: List<String>,
    val status: String,
) : AggregateRoot<ServiceOrderId>(id) {

}
