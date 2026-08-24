package br.com.autorepairshop.customer.application.dto.vehicle

import java.util.UUID

data class TransferVehicleCommand(
    val vehicleId: UUID,
    val newOwnerId: UUID,
)
