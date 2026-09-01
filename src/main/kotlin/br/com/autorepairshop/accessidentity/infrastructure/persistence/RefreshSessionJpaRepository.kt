package br.com.autorepairshop.accessidentity.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RefreshSessionJpaRepository : JpaRepository<RefreshSessionEntity, UUID> {
    fun findByTokenHash(tokenHash: String): RefreshSessionEntity?
    fun findAllByFamilyId(familyId: UUID): List<RefreshSessionEntity>
}
