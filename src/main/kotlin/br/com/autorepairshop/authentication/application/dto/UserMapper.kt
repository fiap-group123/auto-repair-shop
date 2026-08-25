package br.com.autorepairshop.authentication.application.dto

import br.com.autorepairshop.authentication.domain.aggregate.User

fun User.toResponse() = UserResponse(
    id = id.value,
    email = email.value,
    role = role.name,
    active = active,
    customerId = customerId,
)
