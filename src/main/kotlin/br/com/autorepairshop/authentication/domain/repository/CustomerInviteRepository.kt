package br.com.autorepairshop.authentication.domain.repository

import br.com.autorepairshop.authentication.domain.aggregate.CustomerInvite
import java.util.UUID

interface CustomerInviteRepository {
    fun save(invite: CustomerInvite)
    fun findByTokenHash(tokenHash: String): CustomerInvite?
    fun findOpenByCustomerId(customerId: UUID): List<CustomerInvite>
}
