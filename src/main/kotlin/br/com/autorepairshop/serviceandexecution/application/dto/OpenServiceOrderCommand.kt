package br.com.autorepairshop.serviceandexecution.application.dto

import java.math.BigDecimal
import java.util.UUID

data class OpenServiceOrderCommand(
    val document: String? = null,
    val vehiclePlate: String? = null,
    val customer: CustomerOpening? = null,
    val vehicle: VehicleOpening? = null,
    val services: List<ServiceOpening> = emptyList(),
    val parts: List<PartOpening> = emptyList(),
)

data class CustomerOpening(
    val documentId: String,
    val name: String,
    val email: String,
    val phone: String,
)

data class VehicleOpening(
    val plate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val color: String,
)

data class ServiceOpening(
    val name: String,
    val basePrice: BigDecimal,
)

data class PartOpening(
    val inventoryId: UUID,
    val quantity: Int,
)
