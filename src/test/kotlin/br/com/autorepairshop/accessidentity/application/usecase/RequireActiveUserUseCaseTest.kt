package br.com.autorepairshop.accessidentity.application.usecase

import br.com.autorepairshop.accessidentity.AuthFixtures
import br.com.autorepairshop.accessidentity.domain.exception.AuthenticationException
import br.com.autorepairshop.accessidentity.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFailsWith

@Tag("unit")
class RequireActiveUserUseCaseTest {
    private val users = mockk<UserRepository>()
    private val useCase = RequireActiveUserUseCase(users = users)

    @Test
    fun `throws when the user is missing`() {
        val userId = UUID.randomUUID()
        every { users.findById(id = any()) } returns null

        assertFailsWith<AuthenticationException.UserNotFound> {
            useCase.execute(input = userId)
        }
    }

    @Test
    fun `throws when the user is inactive`() {
        val user = AuthFixtures.inactiveManager()
        every { users.findById(id = user.id) } returns user

        assertFailsWith<AuthenticationException.UserInactive> {
            useCase.execute(input = user.id.value)
        }
    }

    @Test
    fun `accepts an active user`() {
        val user = AuthFixtures.manager()
        every { users.findById(id = user.id) } returns user

        useCase.execute(input = user.id.value)
    }
}
