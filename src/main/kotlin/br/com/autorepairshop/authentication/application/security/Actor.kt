package br.com.autorepairshop.authentication.application.security

import br.com.autorepairshop.authentication.domain.valueobject.Role
import java.util.UUID

data class Actor(
    val userId: UUID,
    val role: Role,
    val customerId: UUID?,
)
