package br.com.autorepairshop.authentication.application.usecase

import br.com.autorepairshop.authentication.AuthFixtures
import br.com.autorepairshop.authentication.application.dto.RegisterUserCommand
import br.com.autorepairshop.authentication.application.security.PasswordHasher
import br.com.autorepairshop.authentication.domain.exception.AuthenticationException
import br.com.autorepairshop.authentication.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Tag("unit")
class RegisterUserUseCaseTest {
    private val users = mockk<UserRepository>()
    private val passwords = mockk<PasswordHasher>()
    private val useCase = RegisterUserUseCase(
        users = users,
        passwords = passwords,
    )

    @Test
    fun `first user must be manager`() {
        every { users.existsByEmail(email = any()) } returns false
        every { users.existsAny() } returns false

        assertFailsWith<AuthenticationException.InvalidRole> {
            useCase.execute(
                input = RegisterUserCommand(
                    email = "recepcao@oficina.com",
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "RECEPTIONIST",
                ),
            )
        }
        verify(exactly = 0) { users.save(user = any()) }
    }

    @Test
    fun `first user can be manager`() {
        every { users.existsByEmail(email = any()) } returns false
        every { users.existsAny() } returns false
        every { passwords.hash(raw = any()) } returns AuthFixtures.hashedPassword()
        every { users.save(user = any()) } returns Unit

        val response = useCase.execute(
            input = RegisterUserCommand(
                email = AuthFixtures.MANAGER_EMAIL,
                password = AuthFixtures.RAW_PASSWORD,
                role = "MANAGER",
            ),
        )

        assertEquals(
            expected = "MANAGER",
            actual = response.role,
        )
        verify { users.save(user = any()) }
    }

    @Test
    fun `rejects duplicate email`() {
        every { users.existsByEmail(email = any()) } returns true

        assertFailsWith<AuthenticationException.UserAlreadyExists> {
            useCase.execute(
                input = RegisterUserCommand(
                    email = AuthFixtures.MANAGER_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "MANAGER",
                ),
            )
        }
    }

    @Test
    fun `rejects unknown role`() {
        every { users.existsByEmail(email = any()) } returns false

        assertFailsWith<AuthenticationException.InvalidRole> {
            useCase.execute(
                input = RegisterUserCommand(
                    email = AuthFixtures.MANAGER_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "FOO",
                ),
            )
        }
    }

    @Test
    fun `client without customer id fails`() {
        stubOpenRegistration()

        assertFailsWith<AuthenticationException.InvalidRole> {
            useCase.execute(
                input = RegisterUserCommand(
                    email = AuthFixtures.CLIENT_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "CLIENT",
                ),
            )
        }
    }

    @Test
    fun `registers client with customer id`() {
        stubOpenRegistration()
        val customerId = UUID.randomUUID()

        val response = useCase.execute(
            input = RegisterUserCommand(
                email = AuthFixtures.CLIENT_EMAIL,
                password = AuthFixtures.RAW_PASSWORD,
                role = "CLIENT",
                customerId = customerId,
            ),
        )

        assertEquals(
            expected = customerId,
            actual = response.customerId,
        )
        verify { users.save(user = any()) }
    }

    @Test
    fun `rejects a second login for the same customer`() {
        stubOpenRegistration()
        val customerId = UUID.randomUUID()
        every { users.existsByCustomerId(customerId = customerId) } returns true

        assertFailsWith<AuthenticationException.CustomerAlreadyHasUser> {
            useCase.execute(
                input = RegisterUserCommand(
                    email = AuthFixtures.CLIENT_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "CLIENT",
                    customerId = customerId,
                ),
            )
        }
        verify(exactly = 0) { users.save(user = any()) }
    }

    @Test
    fun `registers receptionist after the first user exists`() {
        stubOpenRegistration()

        val response = useCase.execute(
            input = RegisterUserCommand(
                email = "recepcao@oficina.com",
                password = AuthFixtures.RAW_PASSWORD,
                role = "RECEPTIONIST",
            ),
        )

        assertEquals(
            expected = "RECEPTIONIST",
            actual = response.role,
        )
        assertNull(response.customerId)
        verify { passwords.hash(raw = AuthFixtures.RAW_PASSWORD) }
        verify { users.save(user = any()) }
    }

    private fun stubOpenRegistration() {
        every { users.existsByEmail(email = any()) } returns false
        every { users.existsAny() } returns true
        every { users.existsByCustomerId(customerId = any()) } returns false
        every { passwords.hash(raw = any()) } returns AuthFixtures.hashedPassword()
        every { users.save(user = any()) } returns Unit
    }
}
