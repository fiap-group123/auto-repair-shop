package br.com.autorepairshop.api.controller.authentication

import br.com.autorepairshop.api.dto.authentication.LoginRequest
import br.com.autorepairshop.api.dto.authentication.RegisterUserRequest
import br.com.autorepairshop.api.withHttpRequest
import br.com.autorepairshop.authentication.AuthFixtures
import br.com.autorepairshop.authentication.application.dto.LoginCommand
import br.com.autorepairshop.authentication.application.dto.RegisterUserCommand
import br.com.autorepairshop.authentication.application.dto.TokenResponse
import br.com.autorepairshop.authentication.application.dto.toResponse
import br.com.autorepairshop.authentication.application.usecase.LoginUseCase
import br.com.autorepairshop.authentication.application.usecase.RegisterUserUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import kotlin.test.assertEquals

@Tag("unit")
class AuthControllerTest {
    private val login = mockk<LoginUseCase>()
    private val registerUser = mockk<RegisterUserUseCase>()
    private val controller = AuthController(
        login = login,
        registerUser = registerUser,
    )

    @Test
    fun `login returns the token from the use case`() {
        every { login.execute(input = any()) } returns TokenResponse(accessToken = "jwt")

        val response = controller.login(
            request = LoginRequest(
                email = AuthFixtures.MANAGER_EMAIL,
                password = AuthFixtures.RAW_PASSWORD,
            ),
        )

        assertEquals(
            expected = HttpStatus.OK,
            actual = response.statusCode,
        )
        assertEquals(
            expected = "jwt",
            actual = response.body?.accessToken,
        )
        verify {
            login.execute(
                input = LoginCommand(
                    email = AuthFixtures.MANAGER_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                ),
            )
        }
    }

    @Test
    fun `register maps the request and returns 201`() {
        val created = AuthFixtures.manager().toResponse()
        every { registerUser.execute(input = any()) } returns created

        withHttpRequest(requestUri = "/auth/users") {
            val response = controller.register(
                request = RegisterUserRequest(
                    email = AuthFixtures.MANAGER_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "MANAGER",
                ),
            )

            assertEquals(
                expected = HttpStatus.CREATED,
                actual = response.statusCode,
            )
            assertEquals(
                expected = created.id,
                actual = response.body?.id,
            )
        }
        verify {
            registerUser.execute(
                input = RegisterUserCommand(
                    email = AuthFixtures.MANAGER_EMAIL,
                    password = AuthFixtures.RAW_PASSWORD,
                    role = "MANAGER",
                    customerId = null,
                ),
            )
        }
    }
}
