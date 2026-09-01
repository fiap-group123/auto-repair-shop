package br.com.autorepairshop.accessidentity.application.usecase

import br.com.autorepairshop.accessidentity.AuthFixtures
import br.com.autorepairshop.accessidentity.application.dto.RegisterUserCommand
import br.com.autorepairshop.accessidentity.application.security.Actor
import br.com.autorepairshop.accessidentity.application.security.ActorProvider
import br.com.autorepairshop.accessidentity.application.security.PasswordHasher
import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.accessidentity.domain.repository.UserRepository
import br.com.autorepairshop.accessidentity.domain.valueobject.Role
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
    private val actors = mockk<ActorProvider>()
    private val useCase = RegisterUserUseCase(
        users = users,
        passwords = passwords,
        actors = actors,
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
    fun `rejects client registration`() {
        stubManagerRegistration()

        assertFailsWith<AuthenticationException.InvalidRole> {
            useCase.execute(
                input = RegisterUserCommand(
                    email = AuthFixtures.CLIENT_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "CLIENT",
                    customerId = UUID.randomUUID(),
                ),
            )
        }
        verify(exactly = 0) { users.save(user = any()) }
    }

    @Test
    fun `registers receptionist after the first user exists`() {
        stubManagerRegistration()

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

    @Test
    fun `staff registration without an actor fails`() {
        every { users.existsByEmail(email = any()) } returns false
        every { users.existsAny() } returns true
        every { actors.current() } returns null

        assertFailsWith<AuthenticationException.Unauthenticated> {
            useCase.execute(
                input = RegisterUserCommand(
                    email = "recepcao@oficina.com",
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "RECEPTIONIST",
                ),
            )
        }
    }

    @Test
    fun `non manager cannot register staff`() {
        every { users.existsByEmail(email = any()) } returns false
        every { users.existsAny() } returns true
        every { actors.current() } returns Actor(
            userId = UUID.randomUUID(),
            role = Role.RECEPTIONIST,
            customerId = null,
        )

        assertFailsWith<AuthenticationException.Forbidden> {
            useCase.execute(
                input = RegisterUserCommand(
                    email = "mecanico@oficina.com",
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "MECHANIC",
                ),
            )
        }
    }

    private fun stubManagerRegistration() {
        every { users.existsByEmail(email = any()) } returns false
        every { users.existsAny() } returns true
        every { users.existsByCustomerId(customerId = any()) } returns false
        every { actors.current() } returns Actor(
            userId = UUID.randomUUID(),
            role = Role.MANAGER,
            customerId = null,
        )
        every { passwords.hash(raw = any()) } returns AuthFixtures.hashedPassword()
        every { users.save(user = any()) } returns Unit
    }
}
