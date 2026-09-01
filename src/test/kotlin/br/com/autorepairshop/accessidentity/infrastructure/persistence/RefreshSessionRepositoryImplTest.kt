package br.com.autorepairshop.accessidentity.infrastructure.persistence

import br.com.autorepairshop.accessidentity.domain.aggregate.RefreshSession
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours

@Tag("unit")
class RefreshSessionRepositoryImplTest {
    private val jpa = mockk<RefreshSessionJpaRepository>()
    private val repo = RefreshSessionRepositoryImpl(jpa = jpa)

    @Test
    fun `maps a session through save and queries`() {
        val issued = RefreshSession.issue(
            userId = UUID.randomUUID(),
            ttl = 2.hours,
        )
        val stored = slot<RefreshSessionEntity>()
        every { jpa.save(capture(stored)) } answers { stored.captured }
        repo.save(session = issued.session)

        every { jpa.findByTokenHash(tokenHash = issued.session.tokenHash) } returns stored.captured
        every { jpa.findAllByFamilyId(familyId = issued.session.familyId) } returns
            listOf(element = stored.captured)

        assertEquals(
            expected = issued.session.id,
            actual = repo.findByTokenHash(tokenHash = issued.session.tokenHash)?.id,
        )
        assertEquals(
            expected = 1,
            actual = repo.findAllByFamilyId(familyId = issued.session.familyId).size,
        )
    }

    @Test
    fun `returns null when nothing is stored`() {
        every { jpa.findByTokenHash(tokenHash = any()) } returns null
        every { jpa.findAllByFamilyId(familyId = any()) } returns emptyList()

        assertNull(repo.findByTokenHash(tokenHash = "missing"))
        assertEquals(
            expected = 0,
            actual = repo.findAllByFamilyId(familyId = UUID.randomUUID()).size,
        )
    }
}
