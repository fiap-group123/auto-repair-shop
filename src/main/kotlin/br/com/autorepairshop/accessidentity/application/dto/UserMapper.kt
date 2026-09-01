package br.com.autorepairshop.accessidentity.application.dto

import br.com.autorepairshop.accessidentity.domain.aggregate.User
import kotlin.time.toJavaInstant

fun User.toResponse() = UserResponse(
    id = id.value,
    email = email.value,
    role = role.name,
    active = active,
    customerId = customerId,
    createdAt = createdAt.toJavaInstant(),
)
