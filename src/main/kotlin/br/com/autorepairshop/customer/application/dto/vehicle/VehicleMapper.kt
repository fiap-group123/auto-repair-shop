package br.com.autorepairshop.customer.application.dto.vehicle

import br.com.autorepairshop.customer.domain.aggregate.Vehicle

fun Vehicle.toResponse() = VehicleResponse(
    id = id.value,
    ownerId = ownerId.value,
    plate = plate.formatted(),
    plateType = plate.type.name,
    brand = brand,
    model = model,
    color = color,
    year = year.value,
)
