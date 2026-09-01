package br.com.autorepairshop.accessidentity.application.usecase

import br.com.autorepairshop.accessidentity.AuthFixtures
import br.com.autorepairshop.accessidentity.application.dto.LoginCommand
import br.com.autorepairshop.accessidentity.application.security.PasswordHasher
import br.com.autorepairshop.accessidentity.application.security.TokenIssuer
import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.accessidentity.domain.repository.RefreshSessionRepository
import br.com.autorepairshop.accessidentity.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Tag("unit")
class LoginUseCaseTest {
    private val users = mockk<UserRepository>()
    private val passwords = mockk<PasswordHasher>()
    private val tokens = mockk<TokenIssuer>()
    private val sessions = mockk<RefreshSessionRepository>(relaxUnitFun = true)
    private val useCase = LoginUseCase(
        users = users,
        passwords = passwords,
        tokens = tokens,
        sessions = sessions,
        accessTtlSeconds = 900,
        refreshTtlSeconds = 1_209_600,
    )

    @Test
    fun `unknown email yields invalid credentials`() {
        every { users.findByEmail(email = any()) } returns null

        assertFailsWith<AuthenticationException.InvalidCredentials> {
            useCase.execute(input = command())
        }
        verify(exactly = 0) { tokens.issue(user = any()) }
    }

    @Test
    fun `wrong password yields invalid credentials`() {
        val user = AuthFixtures.manager()
        every { users.findByEmail(email = any()) } returns user
        every {
            passwords.matches(
                raw = any(),
                hashed = any(),
            )
        } returns false

        assertFailsWith<AuthenticationException.InvalidCredentials> {
            useCase.execute(input = command())
        }
        verify(exactly = 0) { tokens.issue(user = any()) }
    }

    @Test
    fun `inactive user cannot log in`() {
        every { users.findByEmail(email = any()) } returns AuthFixtures.inactiveManager()

        assertFailsWith<AuthenticationException.UserInactive> {
            useCase.execute(input = command())
        }
    }

    @Test
    fun `issues a token on success`() {
        val user = AuthFixtures.manager()
        every { users.findByEmail(email = any()) } returns user
        every {
            passwords.matches(
                raw = AuthFixtures.RAW_PASSWORD,
                hashed = user.hashedPassword,
            )
        } returns true
        every { tokens.issue(user = user) } returns "jwt-token"

        val response = useCase.execute(input = command())

        assertEquals(
            expected = "jwt-token",
            actual = response.accessToken,
        )
        assertTrue(response.refreshToken.isNotBlank())
        assertEquals(
            expected = 900,
            actual = response.expiresIn,
        )
        verify { tokens.issue(user = user) }
        verify { sessions.save(session = any()) }
    }

    private fun command() = LoginCommand(
        email = AuthFixtures.MANAGER_EMAIL,
        password = AuthFixtures.RAW_PASSWORD,
    )
}
