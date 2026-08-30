package br.com.autorepairshop.authentication.application.usecase

import br.com.autorepairshop.authentication.application.dto.RefreshTokenCommand
import br.com.autorepairshop.authentication.domain.SecureToken
import br.com.autorepairshop.authentication.domain.aggregate.RefreshSession
import br.com.autorepairshop.authentication.domain.repository.RefreshSessionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.hours

@Tag("unit")
class LogoutUseCaseTest {
    private val sessions = mockk<RefreshSessionRepository>(relaxUnitFun = true)
    private val useCase = LogoutUseCase(sessions = sessions)

    @Test
    fun `revokes an open session`() {
        val issued = RefreshSession.issue(
            userId = UUID.randomUUID(),
            ttl = 2.hours,
        )
        every { sessions.findByTokenHash(tokenHash = SecureToken.hash(raw = issued.rawToken)) } returns issued.session

        useCase.execute(input = RefreshTokenCommand(refreshToken = issued.rawToken))

        assertNotNull(issued.session.revokedAt)
        verify { sessions.save(session = issued.session) }
    }

    @Test
    fun `unknown token is a no-op`() {
        every { sessions.findByTokenHash(tokenHash = any()) } returns null

        useCase.execute(input = RefreshTokenCommand(refreshToken = "missing"))

        verify(exactly = 0) { sessions.save(session = any()) }
    }
}
