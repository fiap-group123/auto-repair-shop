package br.com.autorepairshop.accessidentity.domain.repository

import br.com.autorepairshop.accessidentity.domain.aggregate.CustomerInvite
import java.util.UUID

interface CustomerInviteRepository {
    fun save(invite: CustomerInvite)
    fun findByTokenHash(tokenHash: String): CustomerInvite?
    fun findOpenByCustomerId(customerId: UUID): List<CustomerInvite>
}
