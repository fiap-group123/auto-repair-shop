package br.com.autorepairshop.authentication.application.usecase

import br.com.autorepairshop.authentication.AuthFixtures
import br.com.autorepairshop.authentication.application.dto.LoginCommand
import br.com.autorepairshop.authentication.application.security.PasswordHasher
import br.com.autorepairshop.authentication.application.security.TokenIssuer
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("unit")
class LoginUseCaseTest {
    private val users = mockk<UserRepository>()
    private val passwords = mockk<PasswordHasher>()
    private val tokens = mockk<TokenIssuer>()
    private val useCase = LoginUseCase(
        users = users,
        passwords = passwords,
        tokens = tokens,
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
        verify { tokens.issue(user = user) }
    }

    private fun command() = LoginCommand(
        email = AuthFixtures.MANAGER_EMAIL,
        password = AuthFixtures.RAW_PASSWORD,
    )
}
