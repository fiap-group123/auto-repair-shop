package br.com.autorepairshop.customer.application.dto.vehicle

import java.util.UUID

data class VehicleResponse(
    val id: UUID,
    val ownerId: UUID,
    val plate: String,
    val plateType: String,
    val brand: String,
    val model: String,
    val year: Int,
)
