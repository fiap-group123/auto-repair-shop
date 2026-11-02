package br.com.autorepairshop.api.dto.serviceandexecution

import java.math.BigDecimal
import java.util.UUID

data class RegisterServiceOrderRequest(
    val document: String? = null,
    val vehiclePlate: String? = null,
    val customer: CustomerOpeningRequest? = null,
    val vehicle: VehicleOpeningRequest? = null,
    val services: List<ServiceOpeningRequest> = emptyList(),
    val parts: List<PartOpeningRequest> = emptyList(),
)

data class CustomerOpeningRequest(
    val documentId: String,
    val name: String,
    val email: String,
    val phone: String,
)

data class VehicleOpeningRequest(
    val plate: String,
    val brand: String,
    val model: String,
    val year: Int,
    val color: String,
)

data class ServiceOpeningRequest(
    val name: String,
    val basePrice: BigDecimal,
)

data class PartOpeningRequest(
    val inventoryId: UUID,
    val quantity: Int,
)
