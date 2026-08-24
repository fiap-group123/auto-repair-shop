package br.com.autorepairshop.customer.application.dto.vehicle

import java.util.UUID

data class ChangeVehiclePlateCommand(
    val vehicleId: UUID,
    val plate: String,
)
