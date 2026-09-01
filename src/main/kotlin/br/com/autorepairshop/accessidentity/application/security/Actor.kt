package br.com.autorepairshop.accessidentity.application.security

import br.com.autorepairshop.accessidentity.domain.valueobject.Role
import java.util.UUID

data class Actor(
    val userId: UUID,
    val role: Role,
    val customerId: UUID?,
)
