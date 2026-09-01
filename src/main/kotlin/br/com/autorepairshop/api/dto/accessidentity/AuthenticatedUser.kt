package br.com.autorepairshop.api.dto.accessidentity

import br.com.autorepairshop.accessidentity.domain.valueobject.Role
import java.util.UUID

data class AuthenticatedUser(
    val userId: UUID,
    val role: Role,
    val customerId: UUID?,
)
