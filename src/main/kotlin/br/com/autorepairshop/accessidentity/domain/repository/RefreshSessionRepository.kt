package br.com.autorepairshop.accessidentity.domain.repository

import br.com.autorepairshop.accessidentity.domain.aggregate.RefreshSession
import java.util.UUID

interface RefreshSessionRepository {
    fun save(session: RefreshSession)
    fun findByTokenHash(tokenHash: String): RefreshSession?
    fun findAllByFamilyId(familyId: UUID): List<RefreshSession>
}
