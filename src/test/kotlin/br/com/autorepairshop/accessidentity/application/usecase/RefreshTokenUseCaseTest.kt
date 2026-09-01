package br.com.autorepairshop.accessidentity.application.usecase

import br.com.autorepairshop.accessidentity.AuthFixtures
import br.com.autorepairshop.accessidentity.application.dto.RefreshTokenCommand
import br.com.autorepairshop.accessidentity.application.security.TokenIssuer
import br.com.autorepairshop.accessidentity.domain.SecureToken
import br.com.autorepairshop.accessidentity.domain.aggregate.RefreshSession
import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.accessidentity.domain.repository.RefreshSessionRepository
import br.com.autorepairshop.accessidentity.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

@Tag("unit")
class RefreshTokenUseCaseTest {
    private val sessions = mockk<RefreshSessionRepository>(relaxUnitFun = true)
    private val users = mockk<UserRepository>()
    private val tokens = mockk<TokenIssuer>()
    private val useCase = RefreshTokenUseCase(
        sessions = sessions,
        users = users,
        tokens = tokens,
        accessTtlSeconds = 900,
        refreshTtlSeconds = 1_209_600,
    )

    @Test
    fun `rotates a valid refresh token`() {
        val user = AuthFixtures.manager()
        val issued = RefreshSession.issue(
            userId = user.id.value,
            ttl = 2.hours,
        )
        every { sessions.findByTokenHash(tokenHash = SecureToken.hash(raw = issued.rawToken)) } returns issued.session
        every { users.findById(id = user.id) } returns user
        every { tokens.issue(user = user) } returns "new-jwt"

        val response = useCase.execute(input = RefreshTokenCommand(refreshToken = issued.rawToken))

        assertTrue(response.accessToken == "new-jwt")
        assertNotEquals(
            illegal = issued.rawToken,
            actual = response.refreshToken,
        )
        verify(exactly = 2) { sessions.save(session = any()) }
    }

    @Test
    fun `revokes the family when a revoked token is reused`() {
        val user = AuthFixtures.manager()
        val issued = RefreshSession.issue(
            userId = user.id.value,
            ttl = 2.hours,
        )
        issued.session.revoke()
        val sibling = RefreshSession.issue(
            userId = user.id.value,
            familyId = issued.session.familyId,
            ttl = 2.hours,
        ).session
        every { sessions.findByTokenHash(tokenHash = SecureToken.hash(raw = issued.rawToken)) } returns issued.session
        every { sessions.findAllByFamilyId(familyId = issued.session.familyId) } returns
            listOf(element = issued.session).plus(element = sibling)

        assertFailsWith<AuthenticationException.RefreshReuse> {
            useCase.execute(input = RefreshTokenCommand(refreshToken = issued.rawToken))
        }
        assertTrue(sibling.revokedAt != null)
    }

    @Test
    fun `unknown refresh fails`() {
        every { sessions.findByTokenHash(tokenHash = any()) } returns null

        assertFailsWith<AuthenticationException.InvalidRefresh> {
            useCase.execute(input = RefreshTokenCommand(refreshToken = "missing"))
        }
    }
}
