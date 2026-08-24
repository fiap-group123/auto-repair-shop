package br.com.autorepairshop.customer.application.dto.vehicle

import java.util.UUID

data class UpdateVehicleSpecCommand(
    val vehicleId: UUID,
    val brand: String,
    val model: String,
    val year: Int,
)
