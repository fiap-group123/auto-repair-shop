package br.com.autorepairshop.api.dto

import java.util.UUID

data class TransferVehicleRequest(
    val newOwnerId: UUID,
)
