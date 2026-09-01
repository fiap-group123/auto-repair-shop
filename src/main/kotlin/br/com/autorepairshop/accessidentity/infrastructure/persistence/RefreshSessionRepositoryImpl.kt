package br.com.autorepairshop.accessidentity.infrastructure.persistence

import br.com.autorepairshop.accessidentity.domain.aggregate.RefreshSession
import br.com.autorepairshop.accessidentity.domain.repository.RefreshSessionRepository
import br.com.autorepairshop.accessidentity.domain.valueobject.RefreshSessionId
import org.springframework.stereotype.Repository
import java.util.UUID
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Repository
class RefreshSessionRepositoryImpl(private val jpa: RefreshSessionJpaRepository) : RefreshSessionRepository {

    override fun save(session: RefreshSession) {
        jpa.save(session.toEntity())
    }

    override fun findByTokenHash(tokenHash: String): RefreshSession? =
        jpa.findByTokenHash(tokenHash = tokenHash)?.toDomain()

    override fun findAllByFamilyId(familyId: UUID): List<RefreshSession> =
        jpa.findAllByFamilyId(familyId = familyId).map { it.toDomain() }

    private fun RefreshSession.toEntity() = RefreshSessionEntity(
        id = id.value,
        userId = userId,
        familyId = familyId,
        tokenHash = tokenHash,
        expiresAt = expiresAt.toJavaInstant(),
        revokedAt = revokedAt?.toJavaInstant(),
        replacedBy = replacedBy?.value,
        createdAt = createdAt.toJavaInstant(),
    )

    private fun RefreshSessionEntity.toDomain() = RefreshSession.rehydrate(
        id = RefreshSessionId(value = id),
        userId = userId,
        familyId = familyId,
        tokenHash = tokenHash,
        expiresAt = expiresAt.toKotlinInstant(),
        revokedAt = revokedAt?.toKotlinInstant(),
        replacedBy = replacedBy?.let { RefreshSessionId(value = it) },
        createdAt = createdAt.toKotlinInstant(),
    )
}
