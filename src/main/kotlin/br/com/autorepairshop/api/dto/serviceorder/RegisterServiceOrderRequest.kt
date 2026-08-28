package br.com.autorepairshop.api.dto.serviceorder

import java.util.UUID

data class OpenServiceOrderRequest(
    val customerId: UUID,
    val vehicleId: UUID,
)
