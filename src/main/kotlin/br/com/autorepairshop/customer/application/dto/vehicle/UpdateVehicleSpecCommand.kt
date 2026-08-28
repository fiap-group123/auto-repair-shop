package br.com.autorepairshop.customer.application.dto.vehicle

import java.util.UUID

data class UpdateVehicleSpecCommand(
    val vehicleId: UUID,
    val brand: String? = null,
    val model: String? = null,
    val color: String? = null,
    val year: Int? = null,
)
