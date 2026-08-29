package br.com.autorepairshop.api.dto.serviceorder

import java.util.UUID

data class RegisterServiceOrderRequest(
    val customerId: UUID,
    val vehicleId: UUID,
)
