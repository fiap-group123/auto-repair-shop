package br.com.autorepairshop.customer.application.dto.vehicle

import java.util.UUID

data class RegisterVehicleCommand(
    val ownerId: UUID,
    val plate: String,
    val brand: String,
    val model: String,
    val year: Int,
)
